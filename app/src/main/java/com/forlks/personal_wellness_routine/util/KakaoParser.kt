package com.forlks.personal_wellness_routine.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.forlks.personal_wellness_routine.domain.model.ChatAnalysisResult
import com.forlks.personal_wellness_routine.domain.model.DailyChatResult
import com.forlks.personal_wellness_routine.domain.model.TemperatureLevel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 카카오톡 .txt 내보내기 파일 파싱 및 온디바이스 감정 분석
 *
 * 지원 형식:
 *   ─ 날짜 구분선: "2024년 3월 5일 화요일"  또는
 *                  "--------------- 2024년 3월 5일 화요일 ---------------"
 *   ─ 메시지     : "[홍길동] [오전 10:05] 안녕하세요"
 *
 * 감정 분석 공식 (v0.0.2):
 *   rawScore = 긍정 키워드 수 / (긍정 + 부정) * 100  (중의어 제외)
 *   level    = 90+→5, 70+→4, 50+→3, 30+→2, <30→1
 *
 * [분석 정책]
 *   - 당일(오늘) 날짜 메시지만 분석합니다
 *   - 월 최대 2회(21일 주기) 분석을 권장합니다
 */
object KakaoParser {

    /**
     * 날짜 감지 — 라인 전체가 아닌 부분 match로 더 유연하게 처리
     *   "2024년 3월 5일 화요일" / "--- 2024년 3월 5일 --- " 등 모두 캐치
     */
    private val DATE_FIND   = Regex("""(\d{4})년\s+(\d{1,2})월\s+(\d{1,2})일""")

    /**
     * 메시지 패턴 — matchEntire → find 기반으로 변경
     *   "[이름] [오전/오후 HH:MM] 내용" — 이름·시각에 다양한 문자 허용
     */
    private val MESSAGE_FIND = Regex("""^\[([^\]]+)\]\s*\[([^\]]+)\]\s*(.+)$""")

    // ── 감정 사전 ─────────────────────────────────────────────────────────────

    private val POSITIVE_WORDS = setOf(
        "좋아", "최고", "행복", "기쁘", "사랑", "감사", "고마워", "잘됐", "축하",
        "ㅎㅎ", "ㅋㅋ", "ㅋㅋㅋ", "😊", "😄", "❤", "🥰", "😍", "👍", "🎉",
        "설레", "신나", "재미", "즐거", "멋있", "대단", "훌륭", "완벽", "최고야",
        "보고싶어", "사랑해", "잘자", "굿밤", "좋겠다", "힘내", "응원"
    )

    private val NEGATIVE_WORDS = setOf(
        "싫어", "힘들", "슬프", "우울", "화나", "짜증", "최악", "실망", "미워",
        "ㅠㅠ", "ㅜㅜ", "ㅠ", "😢", "😭", "😡", "😤", "💔", "😞",
        "지쳐", "아파", "걱정", "불안", "무서", "후회", "억울", "외로", "포기",
        "절망", "허무", "허전", "두렵", "괴롭"
    )

    // 중의어 — 감정 분석에서 제외
    private val AMBIGUOUS_WORDS = setOf(
        "같이", "도움", "그립", "괜찮", "웃음", "보람", "눈물"
    )

    // 불용어 (키워드 빈도 수집에서만 제외)
    private val STOPWORDS = setOf(
        "이", "가", "을", "를", "은", "는", "의", "에", "와", "과",
        "도", "로", "으로", "에서", "부터", "까지", "만", "이랑", "하고",
        "그", "저", "이것", "그것", "저것", "이거", "그거", "저거",
        "나", "너", "우리", "저는", "나는", "응", "어", "음", "네", "예",
        "아", "오", "ㅇㅇ", "그래", "그냥", "진짜", "정말", "너무", "되게"
    )

    data class ParsedMessage(
        val sender: String,
        val content: String,
        val sentiment: Sentiment
    )

    enum class Sentiment { POSITIVE, NEUTRAL, NEGATIVE }

    data class FullParseResult(
        val chatAnalysis: ChatAnalysisResult,
        val dailyResults: List<DailyChatResult>
    )

    // ── Public utility ─────────────────────────────────────────────────────────

    /**
     * 텍스트에서 긍정/(긍정+부정)*100 원점수 계산 (0~100, 중의어 제외)
     * 감정 단어가 없으면 50(중립) 반환
     */
    fun calcRawScore(text: String): Int {
        var pos = 0; var neg = 0
        POSITIVE_WORDS.forEach { word ->
            if (word !in AMBIGUOUS_WORDS && text.contains(word)) pos++
        }
        NEGATIVE_WORDS.forEach { word ->
            if (word !in AMBIGUOUS_WORDS && text.contains(word)) neg++
        }
        val total = pos + neg
        return if (total == 0) 50
        else ((pos.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * 원점수(0~100) → 1~5 레벨 변환
     */
    fun scoreToLevel(rawScore: Int): Int = when {
        rawScore >= 90 -> 5
        rawScore >= 70 -> 4
        rawScore >= 50 -> 3
        rawScore >= 30 -> 2
        else           -> 1
    }

    // ── 핵심 파싱 ── 오늘 날짜만 분석 ─────────────────────────────────────────

    /**
     * [당일 전용] 파일에서 오늘 날짜 메시지만 파싱·분석합니다.
     *
     * @param onProgress (processedLines, totalLines) 진행 콜백
     * @return FullParseResult (dailyResults 는 오늘 하루치 1건)
     *         메시지가 없으면 빈 result 반환
     */
    suspend fun parseTodayOnly(
        context: Context,
        uri: Uri,
        fileName: String,
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): FullParseResult {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return parseForDate(context, uri, fileName, todayStr, onProgress)
    }

    /**
     * 특정 날짜 메시지만 파싱·분석 (당일 외 날짜 지정 가능)
     */
    suspend fun parseForDate(
        context: Context,
        uri: Uri,
        fileName: String,
        targetDate: String,           // "yyyy-MM-dd"
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): FullParseResult {
        val lines = readLines(context, uri)
        val totalLines = lines.size

        val messages = mutableListOf<ParsedMessage>()
        val keywordFreq = mutableMapOf<String, Int>()
        var inTargetSection = false

        lines.forEachIndexed { idx, rawLine ->
            if (idx % 20 == 0) onProgress(idx + 1, totalLines)  // 20줄마다 업데이트 (부드러운 프로그레스)

            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachIndexed

            // 날짜 라인 감지 (부분 match)
            val dateMatch = DATE_FIND.find(line)
            if (dateMatch != null) {
                val y = dateMatch.groupValues[1].toInt()
                val m = dateMatch.groupValues[2].toInt()
                val d = dateMatch.groupValues[3].toInt()
                val parsed = "%04d-%02d-%02d".format(y, m, d)
                inTargetSection = (parsed == targetDate)
                return@forEachIndexed
            }

            if (!inTargetSection) return@forEachIndexed

            // 메시지 라인 감지 (부분 match)
            val msgMatch = MESSAGE_FIND.find(line) ?: return@forEachIndexed
            val content = msgMatch.groupValues[3].trim()

            // 미디어 필터
            if (isMediaMessage(content)) return@forEachIndexed

            val sentiment = classifySentiment(content)
            messages.add(ParsedMessage(msgMatch.groupValues[1], content, sentiment))

            content.split(" ", "　").forEach { word ->
                val cleaned = word.replace(Regex("[^가-힣a-zA-Z0-9ㅋㅎㅠㅜ]"), "")
                if (cleaned.length >= 2 && cleaned !in STOPWORDS) {
                    keywordFreq[cleaned] = (keywordFreq[cleaned] ?: 0) + 1
                }
            }
        }

        onProgress(totalLines, totalLines)

        return buildResult(fileName, targetDate, targetDate, messages, keywordFreq)
    }

    // ── 전체 날짜 파싱 (캘린더용, 하위 호환) ─────────────────────────────────

    suspend fun parseAndAnalyze(context: Context, uri: Uri, fileName: String): ChatAnalysisResult =
        parseAndAnalyzeFull(context, uri, fileName) { _, _ -> }.chatAnalysis

    /**
     * 파일 전체 날짜별 파싱 — 캘린더 뷰 등 전체 이력이 필요한 경우에만 사용
     */
    suspend fun parseAndAnalyzeFull(
        context: Context,
        uri: Uri,
        fileName: String,
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): FullParseResult {
        val lines = readLines(context, uri)

        val messagesByDate = linkedMapOf<String, MutableList<ParsedMessage>>()
        var currentDate = ""
        val keywordFreq = mutableMapOf<String, Int>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val dateMatch = DATE_FIND.find(trimmed)
            if (dateMatch != null) {
                val y = dateMatch.groupValues[1].toInt()
                val m = dateMatch.groupValues[2].toInt()
                val d = dateMatch.groupValues[3].toInt()
                currentDate = "%04d-%02d-%02d".format(y, m, d)
                messagesByDate.getOrPut(currentDate) { mutableListOf() }
                continue
            }

            if (currentDate.isEmpty()) continue

            val msgMatch = MESSAGE_FIND.find(trimmed) ?: continue
            val content = msgMatch.groupValues[3].trim()
            if (isMediaMessage(content)) continue

            val sentiment = classifySentiment(content)
            messagesByDate.getOrPut(currentDate) { mutableListOf() }
                .add(ParsedMessage(msgMatch.groupValues[1], content, sentiment))

            content.split(" ", "　").forEach { word ->
                val cleaned = word.replace(Regex("[^가-힣a-zA-Z0-9ㅋㅎㅠㅜ]"), "")
                if (cleaned.length >= 2 && cleaned !in STOPWORDS) {
                    keywordFreq[cleaned] = (keywordFreq[cleaned] ?: 0) + 1
                }
            }
        }

        val sortedDates = messagesByDate.keys.sorted()
        val totalDays = sortedDates.size
        val dailyResults = mutableListOf<DailyChatResult>()

        var totalPos = 0; var totalNeg = 0; var totalNeutral = 0; var totalMsgs = 0

        sortedDates.forEachIndexed { idx, date ->
            onProgress(idx + 1, totalDays)
            val msgs = messagesByDate[date] ?: emptyList()
            val pos = msgs.count { it.sentiment == Sentiment.POSITIVE }
            val neg = msgs.count { it.sentiment == Sentiment.NEGATIVE }
            val neu = msgs.size - pos - neg

            totalPos += pos; totalNeg += neg; totalNeutral += neu
            totalMsgs += msgs.size

            val sentTotal = (pos + neg).coerceAtLeast(1)
            val dayTemp = (pos.toFloat() / sentTotal.toFloat()) * 100f
            val totalDay = msgs.size.coerceAtLeast(1)
            val relScore = ((pos.toFloat() / totalDay) * 100f +
                           (neu.toFloat() / totalDay) * 50f -
                           (neg.toFloat() / totalDay) * 50f).coerceIn(0f, 100f)

            dailyResults.add(DailyChatResult(
                chatAnalysisId = 0, date = date,
                totalMessages = msgs.size,
                positiveCount = pos, negativeCount = neg, neutralCount = neu,
                temperature = dayTemp, relationshipScore = relScore
            ))
        }

        val allMessages = messagesByDate.values.flatten()
        val periodStart = sortedDates.firstOrNull()
            ?: LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val periodEnd = sortedDates.lastOrNull()
            ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        return buildResult(
            fileName, periodStart, periodEnd,
            allMessages, keywordFreq,
            overrideDailyResults = dailyResults,
            totalPos = totalPos, totalNeg = totalNeg,
            totalNeutral = totalNeutral, totalMsgs = totalMsgs
        )
    }

    // ── 공통 결과 빌더 ─────────────────────────────────────────────────────────

    private fun buildResult(
        fileName: String,
        periodStart: String,
        periodEnd: String,
        messages: List<ParsedMessage>,
        keywordFreq: Map<String, Int>,
        overrideDailyResults: List<DailyChatResult>? = null,
        totalPos: Int = messages.count { it.sentiment == Sentiment.POSITIVE },
        totalNeg: Int = messages.count { it.sentiment == Sentiment.NEGATIVE },
        totalNeutral: Int = messages.count { it.sentiment == Sentiment.NEUTRAL },
        totalMsgs: Int = messages.size
    ): FullParseResult {
        val grandTotal = totalMsgs.coerceAtLeast(1)
        val positiveRatio = totalPos.toFloat() / grandTotal
        val neutralRatio = totalNeutral.toFloat() / grandTotal
        val negativeRatio = totalNeg.toFloat() / grandTotal

        val sentTotal = (totalPos + totalNeg).coerceAtLeast(1)
        val temperature = (totalPos.toFloat() / sentTotal.toFloat()) * 100f

        val tempLevel = when {
            temperature >= 70f -> TemperatureLevel.WARM
            temperature >= 50f -> TemperatureLevel.NORMAL
            temperature >= 30f -> TemperatureLevel.COOL
            else               -> TemperatureLevel.COLD
        }

        val relationshipScore = (positiveRatio * 100f + neutralRatio * 50f - negativeRatio * 50f)
            .coerceIn(0f, 100f)

        val topKeywords = keywordFreq.entries.sortedByDescending { it.value }.take(10).map { it.key }
        val draft = buildAutoDiary(messages, topKeywords, tempLevel)

        val chatAnalysis = ChatAnalysisResult(
            fileName = fileName,
            periodStart = periodStart,
            periodEnd = periodEnd,
            totalMessages = grandTotal,
            positiveRatio = positiveRatio,
            neutralRatio = neutralRatio,
            negativeRatio = negativeRatio,
            temperature = temperature,
            temperatureLabel = tempLevel.label,
            temperatureEmoji = tempLevel.emoji,
            relationshipScore = relationshipScore,
            topKeywords = topKeywords,
            autoDiaryDraft = draft,
            analyzedAt = System.currentTimeMillis()
        )

        val dailyResults = overrideDailyResults ?: listOf(
            DailyChatResult(
                chatAnalysisId = 0,
                date = periodStart,
                totalMessages = totalMsgs,
                positiveCount = totalPos,
                negativeCount = totalNeg,
                neutralCount = totalNeutral,
                temperature = temperature,
                relationshipScore = relationshipScore
            )
        )

        return FullParseResult(chatAnalysis, dailyResults)
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun isMediaMessage(content: String): Boolean =
        content.startsWith("사진") || content.startsWith("동영상") ||
        content.startsWith("이모티콘") || content.startsWith("파일") ||
        content.startsWith("GIF") || content.startsWith("연락처") ||
        content == "삭제된 메시지입니다."

    /**
     * 파일 읽기 — UTF-8 → EUC-KR 순으로 인코딩 시도
     */
    private fun readLines(context: Context, uri: Uri): List<String> {
        return try {
            val bytes = readBytes(context, uri)
            // UTF-8로 먼저 시도, 깨지면 EUC-KR
            val textUtf8 = bytes.toString(Charsets.UTF_8)
            if (textUtf8.contains("년") || textUtf8.contains("[")) {
                textUtf8.lines()
            } else {
                bytes.toString(charset("EUC-KR")).lines()
            }
        } catch (e: SecurityException) {
            throw SecurityException("파일 접근 권한이 없습니다. 파일을 다시 선택해주세요.", e)
        }
    }

    private fun readBytes(context: Context, uri: Uri): ByteArray {
        val inputStream = when (uri.scheme) {
            "file" -> {
                val path = uri.path ?: throw SecurityException("파일 경로가 올바르지 않습니다.")
                java.io.File(path).inputStream()
            }
            else -> {
                ensureUriPermission(context, uri)
                context.contentResolver.openInputStream(uri)
                    ?: throw SecurityException("파일 접근 권한이 없습니다.")
            }
        }
        return inputStream.use { it.readBytes() }
    }

    private fun ensureUriPermission(context: Context, uri: Uri) {
        if (uri.scheme != "content") return
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) { }
    }

    private fun classifySentiment(text: String): Sentiment {
        var pos = 0; var neg = 0
        POSITIVE_WORDS.forEach { word ->
            if (word !in AMBIGUOUS_WORDS && text.contains(word)) pos++
        }
        NEGATIVE_WORDS.forEach { word ->
            if (word !in AMBIGUOUS_WORDS && text.contains(word)) neg++
        }
        return when {
            pos > neg -> Sentiment.POSITIVE
            neg > pos -> Sentiment.NEGATIVE
            else      -> Sentiment.NEUTRAL
        }
    }

    private fun buildAutoDiary(
        messages: List<ParsedMessage>,
        keywords: List<String>,
        tempLevel: TemperatureLevel
    ): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
        val topPositive = messages.filter { it.sentiment == Sentiment.POSITIVE }
            .map { it.content }.take(3)

        val template = when (tempLevel) {
            TemperatureLevel.WARM ->
                "오늘 대화는 따뜻하고 긍정적이었다. " +
                (if (topPositive.isNotEmpty()) "\"${topPositive.first()}\"라는 말이 특히 마음에 남았다. " else "") +
                (if (keywords.isNotEmpty()) "${keywords.take(3).joinToString(", ")} 같은 이야기를 나눴고 " else "") +
                "서로에게 힘이 되는 대화였던 것 같아 감사하다."
            TemperatureLevel.NORMAL ->
                "오늘 대화는 평범하지만 소소한 일상을 나눴다. " +
                (if (keywords.isNotEmpty()) "${keywords.take(3).joinToString(", ")} 이야기를 했고, " else "") +
                "일상적인 대화 속에서도 연결감을 느꼈다."
            TemperatureLevel.COOL ->
                "오늘 대화는 조금 짧고 차가웠던 것 같다. " +
                "서로 바쁜 것 같지만, 그래도 연락이 되고 있다는 것에 감사해야겠다."
            TemperatureLevel.COLD ->
                "오늘은 대화가 많지 않았다. 상대방이 바쁜지, 아니면 내가 뭔가 잘못한 건 아닐까 생각이 들었다. " +
                "시간 내서 먼저 연락해봐야겠다."
        }

        return "$today\n\n$template"
    }
}
