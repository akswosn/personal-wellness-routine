package com.forlks.personal_wellness_routine.util

import android.content.Context
import android.net.Uri
import com.forlks.personal_wellness_routine.domain.model.ChatAnalysisResult
import com.forlks.personal_wellness_routine.domain.model.TemperatureLevel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 카카오톡 .txt 내보내기 파일 파싱 및 온디바이스 감정 분석
 *
 * 형식 예시:
 *   [홍길동] [오전 10:05] 안녕하세요
 *   [김민준] [오후 3:22] 반갑습니다
 */
object KakaoParser {

    private val MESSAGE_PATTERN = Regex("""^\[(.+?)\] \[(.+?)\] (.+)$""")
    private val DATE_PATTERN = Regex("""(\d{4})년 (\d{1,2})월 (\d{1,2})일 .+""")

    // 긍정 키워드 사전 (기본 샘플)
    private val POSITIVE_WORDS = setOf(
        "좋아", "최고", "행복", "기쁘", "사랑", "감사", "고마워", "잘됐", "축하",
        "ㅎㅎ", "ㅋㅋ", "ㅋㅋㅋ", "😊", "😄", "❤", "🥰", "😍", "👍", "🎉",
        "설레", "신나", "재미", "즐거", "멋있", "대단", "훌륭", "완벽", "최고야",
        "보고싶어", "사랑해", "잘자", "굿밤", "좋겠다", "같이", "도움"
    )

    // 부정 키워드 사전 (기본 샘플)
    private val NEGATIVE_WORDS = setOf(
        "싫어", "힘들", "슬프", "우울", "화나", "짜증", "최악", "실망", "미워",
        "ㅠㅠ", "ㅜㅜ", "ㅠ", "😢", "😭", "😡", "😤", "💔", "😞",
        "지쳐", "아파", "힘내", "걱정", "불안", "무서", "후회", "억울", "외로"
    )

    // 불용어
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

    suspend fun parseAndAnalyze(context: Context, uri: Uri, fileName: String): ChatAnalysisResult {
        val lines = context.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.readLines()
            ?: emptyList()

        val messages = mutableListOf<ParsedMessage>()
        var periodStart = ""
        var periodEnd = ""
        val keywordFreq = mutableMapOf<String, Int>()

        for (line in lines) {
            // 날짜 라인 파싱
            val dateMatch = DATE_PATTERN.matchEntire(line.trim())
            if (dateMatch != null) {
                val (year, month, day) = dateMatch.destructured
                val dateStr = "%04d-%02d-%02d".format(year.toInt(), month.toInt(), day.toInt())
                if (periodStart.isEmpty()) periodStart = dateStr
                periodEnd = dateStr
                continue
            }

            // 메시지 파싱
            val msgMatch = MESSAGE_PATTERN.matchEntire(line.trim()) ?: continue
            val (_, _, content) = msgMatch.destructured
            if (content.startsWith("사진") || content.startsWith("동영상") ||
                content.startsWith("이모티콘") || content.startsWith("파일")) continue

            val sentiment = classifySentiment(content)
            messages.add(ParsedMessage(msgMatch.groupValues[1], content, sentiment))

            // 키워드 빈도 수집 (2글자 이상 단어)
            content.split(" ", "　").forEach { word ->
                val cleaned = word.replace(Regex("[^가-힣a-zA-Z0-9ㅋㅎㅠㅜ]"), "")
                if (cleaned.length >= 2 && cleaned !in STOPWORDS) {
                    keywordFreq[cleaned] = (keywordFreq[cleaned] ?: 0) + 1
                }
            }
        }

        val total = messages.size.coerceAtLeast(1)
        val positive = messages.count { it.sentiment == Sentiment.POSITIVE }
        val negative = messages.count { it.sentiment == Sentiment.NEGATIVE }
        val neutral = total - positive - negative

        val positiveRatio = positive.toFloat() / total
        val neutralRatio = neutral.toFloat() / total
        val negativeRatio = negative.toFloat() / total
        val temperature = positiveRatio * 100f

        val tempLevel = when {
            temperature >= 65f -> TemperatureLevel.WARM
            temperature >= 50f -> TemperatureLevel.NORMAL
            temperature >= 35f -> TemperatureLevel.COOL
            else -> TemperatureLevel.COLD
        }

        val relationshipScore = (positiveRatio * 100f + neutralRatio * 50f - negativeRatio * 50f).coerceIn(0f, 100f)

        val topKeywords = keywordFreq.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }

        val draft = buildAutoDiary(messages, topKeywords, tempLevel)

        return ChatAnalysisResult(
            fileName = fileName,
            periodStart = periodStart.ifEmpty { LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
            periodEnd = periodEnd.ifEmpty { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
            totalMessages = total,
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
    }

    private fun classifySentiment(text: String): Sentiment {
        var positiveScore = 0
        var negativeScore = 0
        POSITIVE_WORDS.forEach { if (text.contains(it)) positiveScore++ }
        NEGATIVE_WORDS.forEach { if (text.contains(it)) negativeScore++ }
        return when {
            positiveScore > negativeScore -> Sentiment.POSITIVE
            negativeScore > positiveScore -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
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
