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
 * 형식 예시:
 *   [홍길동] [오전 10:05] 안녕하세요
 *   [김민준] [오후 3:22] 반갑습니다
 *
 * 감정 분석 공식 (v0.0.2):
 *   rawScore = 긍정 키워드 수 / (긍정 + 부정) * 100  (중의어 제외)
 *   level    = 90+→5, 70+→4, 50+→3, 30+→2, <30→1
 */
object KakaoParser {

    private val MESSAGE_PATTERN = Regex("""^\[(.+?)\] \[(.+?)\] (.+)$""")
    private val DATE_PATTERN = Regex("""(\d{4})년 (\d{1,2})월 (\d{1,2})일 .+""")

    // 긍정 키워드 사전
    private val POSITIVE_WORDS = setOf(
        "좋아", "최고", "행복", "기쁘", "사랑", "감사", "고마워", "잘됐", "축하",
        "ㅎㅎ", "ㅋㅋ", "ㅋㅋㅋ", "😊", "😄", "❤", "🥰", "😍", "👍", "🎉",
        "설레", "신나", "재미", "즐거", "멋있", "대단", "훌륭", "완벽", "최고야",
        "보고싶어", "사랑해", "잘자", "굿밤", "좋겠다", "힘내", "응원"
    )

    // 부정 키워드 사전
    private val NEGATIVE_WORDS = setOf(
        "싫어", "힘들", "슬프", "우울", "화나", "짜증", "최악", "실망", "미워",
        "ㅠㅠ", "ㅜㅜ", "ㅠ", "😢", "😭", "😡", "😤", "💔", "😞",
        "지쳐", "아파", "걱정", "불안", "무서", "후회", "억울", "외로", "포기",
        "절망", "허무", "허전", "두렵", "괴롭"
    )

    // 중의어 - 긍정·부정 모두 해석될 수 있어 감정 분석에서 제외
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
     *  90+ → 5, 70+ → 4, 50+ → 3, 30+ → 2, 미만 → 1
     */
    fun scoreToLevel(rawScore: Int): Int = when {
        rawScore >= 90 -> 5
        rawScore >= 70 -> 4
        rawScore >= 50 -> 3
        rawScore >= 30 -> 2
        else           -> 1
    }

    // ── Legacy single-result parse (backward compat) ──────────────────────────

    suspend fun parseAndAnalyze(context: Context, uri: Uri, fileName: String): ChatAnalysisResult =
        parseAndAnalyzeFull(context, uri, fileName) { _, _ -> }.chatAnalysis

    // ── Full date-based parse with progress callback ───────────────────────────

    /**
     * 날짜별로 파싱하여 전체 분석 + 일별 분석 모두 반환
     * @param onProgress (currentDay: Int, totalDays: Int) 진행 콜백
     */
    suspend fun parseAndAnalyzeFull(
        context: Context,
        uri: Uri,
        fileName: String,
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): FullParseResult {
        val lines = readLines(context, uri)

        // ── 1단계: 날짜별로 메시지 그룹화 ─────────────────────────────────────
        val messagesByDate = linkedMapOf<String, MutableList<ParsedMessage>>()
        var currentDate = ""
        val keywordFreq = mutableMapOf<String, Int>()

        for (line in lines) {
            val dateMatch = DATE_PATTERN.matchEntire(line.trim())
            if (dateMatch != null) {
                val (year, month, day) = dateMatch.destructured
                currentDate = "%04d-%02d-%02d".format(year.toInt(), month.toInt(), day.toInt())
                messagesByDate.getOrPut(currentDate) { mutableListOf() }
                continue
            }

            val msgMatch = MESSAGE_PATTERN.matchEntire(line.trim()) ?: continue
            val content = msgMatch.groupValues[3]
            if (content.startsWith("사진") || content.startsWith("동영상") ||
                content.startsWith("이모티콘") || content.startsWith("파일")) continue
            if (currentDate.isEmpty()) continue

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

        // ── 2단계: 날짜별 분석 + 전체 집계 ───────────────────────────────────
        val sortedDates = messagesByDate.keys.sorted()
        val totalDays = sortedDates.size
        val dailyResults = mutableListOf<DailyChatResult>()

        var totalPos = 0; var totalNeg = 0; var totalNeutral = 0; var totalMsgs = 0

        sortedDates.forEachIndexed { idx, date ->
            onProgress(idx + 1, totalDays)

            val messages = messagesByDate[date] ?: emptyList()
            val pos = messages.count { it.sentiment == Sentiment.POSITIVE }
            val neg = messages.count { it.sentiment == Sentiment.NEGATIVE }
            val neu = messages.size - pos - neg

            totalPos += pos; totalNeg += neg; totalNeutral += neu
            totalMsgs += messages.size

            val sentTotal = (pos + neg).coerceAtLeast(1)
            val dayTemp = (pos.toFloat() / sentTotal.toFloat()) * 100f

            val totalDay = messages.size.coerceAtLeast(1)
            val relScore = ((pos.toFloat() / totalDay) * 100f +
                           (neu.toFloat() / totalDay) * 50f -
                           (neg.toFloat() / totalDay) * 50f).coerceIn(0f, 100f)

            dailyResults.add(DailyChatResult(
                chatAnalysisId = 0,
                date = date,
                totalMessages = messages.size,
                positiveCount = pos,
                negativeCount = neg,
                neutralCount = neu,
                temperature = dayTemp,
                relationshipScore = relScore
            ))
        }

        // ── 3단계: 전체 집계 → ChatAnalysisResult ─────────────────────────────
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

        val periodStart = sortedDates.firstOrNull()
            ?: LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val periodEnd = sortedDates.lastOrNull()
            ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val allMessages = messagesByDate.values.flatten()
        val draft = buildAutoDiary(allMessages, topKeywords, tempLevel)

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

        return FullParseResult(chatAnalysis, dailyResults)
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun readLines(context: Context, uri: Uri): List<String> {
        return try {
            val inputStream = when (uri.scheme) {
                "file" -> {
                    val path = uri.path ?: throw SecurityException("파일 경로가 올바르지 않습니다.")
                    java.io.File(path).inputStream()
                }
                else -> {
                    ensureUriPermission(context, uri)
                    context.contentResolver.openInputStream(uri)
                        ?: throw SecurityException("파일 접근 권한이 없습니다. 파일을 다시 선택해주세요.")
                }
            }
            inputStream.bufferedReader(Charsets.UTF_8).use { it.readLines() }
        } catch (e: SecurityException) {
            throw SecurityException("파일 접근 권한이 없습니다. 파일을 다시 선택해주세요.", e)
        }
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
        var positiveScore = 0
        var negativeScore = 0
        POSITIVE_WORDS.forEach { word ->
            if (word !in AMBIGUOUS_WORDS && text.contains(word)) positiveScore++
        }
        NEGATIVE_WORDS.forEach { word ->
            if (word !in AMBIGUOUS_WORDS && text.contains(word)) negativeScore++
        }
        return when {
            positiveScore > negativeScore -> Sentiment.POSITIVE
            negativeScore > positiveScore -> Sentiment.NEGATIVE
            else                          -> Sentiment.NEUTRAL
        }
    }

    private fun buildAutoDiary(
        messages: List<ParsedMessage>,
        keywords: List<String>,
        tempLevel: TemperatureLevel
    ): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
        val topPositive = messages
            .filter { it.sentiment == Sentiment.POSITIVE }
            .map { it.content }
            .take(3)

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
