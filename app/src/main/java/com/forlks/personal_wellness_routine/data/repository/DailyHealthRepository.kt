package com.forlks.personal_wellness_routine.data.repository

import com.forlks.personal_wellness_routine.data.db.dao.DailyHealthScoreDao
import com.forlks.personal_wellness_routine.data.db.entity.DailyHealthScoreEntity
import com.forlks.personal_wellness_routine.util.DailyHealthCalculator
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyHealthRepository @Inject constructor(
    private val dao: DailyHealthScoreDao
) {
    fun observeToday(): Flow<DailyHealthScoreEntity?> =
        dao.observeByDate(LocalDate.now().toString())

    fun getLast7Days(): Flow<List<DailyHealthScoreEntity>> = dao.getLast7Days()

    fun getLast30Days(): Flow<List<DailyHealthScoreEntity>> = dao.getLast30Days()

    suspend fun getToday(): DailyHealthScoreEntity? =
        dao.getByDate(LocalDate.now().toString())

    suspend fun getLatest(): DailyHealthScoreEntity? = dao.getLatest()

    /**
     * 오늘 건강도 업데이트 (기존 row merge 방식)
     * 전달된 항목만 갱신하고 나머지는 기존 값 유지
     */
    suspend fun updateToday(
        moodScore: Float? = null,
        routineScore: Float? = null,
        diaryScore: Float? = null,
        chatTempScore: Float? = null,
        relationScore: Float? = null
    ) {
        val today = LocalDate.now().toString()
        val existing = dao.getByDate(today)

        val newMood     = moodScore     ?: existing?.moodScore
        val newRoutine  = routineScore  ?: existing?.routineScore
        val newDiary    = diaryScore    ?: existing?.diaryScore
        val newChat     = chatTempScore ?: existing?.chatTempScore
        val newRelation = relationScore ?: existing?.relationScore

        val entity = DailyHealthCalculator.buildEntity(
            date          = today,
            moodScore     = newMood,
            routineScore  = newRoutine,
            diaryScore    = newDiary,
            chatTempScore = newChat,
            relationScore = newRelation
        )
        dao.upsert(entity)
    }

    suspend fun upsert(entity: DailyHealthScoreEntity) = dao.upsert(entity)
}
