package com.forlks.personal_wellness_routine.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.forlks.personal_wellness_routine.data.db.dao.RoutineDao
import com.forlks.personal_wellness_routine.data.db.entity.RoutineEntity
import com.forlks.personal_wellness_routine.data.db.entity.RoutineHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutineDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: RoutineDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.routineDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // ── insert / getAllActiveRoutines ────────────────────────────────────────

    @Test
    fun insertRoutine_and_getAllActiveRoutines_returnsInserted() = runBlocking {
        val routine = RoutineEntity(
            name = "아침 스트레칭",
            category = "HEALTH",
            emoji = "🧘",
            scheduledTime = "07:00",
            durationMinutes = 10,
            isActive = true
        )
        val id = dao.insertRoutine(routine)
        assertTrue("insert 후 id > 0 이어야 함", id > 0)

        val all = dao.getAllActiveRoutines().first()
        assertEquals(1, all.size)
        assertEquals("아침 스트레칭", all[0].name)
    }

    @Test
    fun insertMultipleRoutines_getAllReturnsAll() = runBlocking {
        repeat(3) { i ->
            dao.insertRoutine(RoutineEntity(
                name = "루틴$i",
                category = "HEALTH",
                emoji = "⭐",
                scheduledTime = "0$i:00",
                durationMinutes = 10
            ))
        }
        val all = dao.getAllActiveRoutines().first()
        assertEquals(3, all.size)
    }

    // ── softDelete ────────────────────────────────────────────────────────────

    @Test
    fun softDeleteRoutine_isNotReturnedInActiveQuery() = runBlocking {
        val id = dao.insertRoutine(RoutineEntity(
            name = "삭제될 루틴",
            category = "MIND",
            emoji = "📝",
            scheduledTime = "09:00",
            durationMinutes = 5
        ))
        dao.softDeleteRoutine(id)

        val active = dao.getAllActiveRoutines().first()
        assertTrue("소프트삭제 후 active 목록에 없어야 함", active.none { it.id == id })
    }

    @Test
    fun softDeleteRoutine_doesNotPhysicallyDelete() = runBlocking {
        val id = dao.insertRoutine(RoutineEntity(
            name = "물리삭제 안됨",
            category = "HEALTH",
            emoji = "💪",
            scheduledTime = "08:00",
            durationMinutes = 15
        ))
        dao.softDeleteRoutine(id)

        // getTotalActiveCount 로 active=0 임을 확인
        val activeCount = dao.getTotalActiveCount()
        assertEquals(0, activeCount)
    }

    // ── getRoutinesByCategory ─────────────────────────────────────────────────

    @Test
    fun getRoutinesByCategory_returnsOnlyMatchingCategory() = runBlocking {
        dao.insertRoutine(RoutineEntity(name = "헬스", category = "HEALTH", emoji = "🏃", scheduledTime = "06:00", durationMinutes = 30))
        dao.insertRoutine(RoutineEntity(name = "독서", category = "MIND", emoji = "📖", scheduledTime = "22:00", durationMinutes = 20))
        dao.insertRoutine(RoutineEntity(name = "명상", category = "MIND", emoji = "🧘", scheduledTime = "07:00", durationMinutes = 10))

        val mindRoutines = dao.getRoutinesByCategory("MIND").first()
        assertEquals(2, mindRoutines.size)
        assertTrue(mindRoutines.all { it.category == "MIND" })
    }

    // ── history: insertHistory / getCompletedRoutinesForDate ─────────────────

    @Test
    fun insertHistory_and_getCompletedRoutinesForDate_returnsForDate() = runBlocking {
        val routineId = dao.insertRoutine(RoutineEntity(
            name = "운동",
            category = "HEALTH",
            emoji = "🏋",
            scheduledTime = "07:00",
            durationMinutes = 30
        ))
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-14"))

        val completed = dao.getCompletedRoutinesForDate("2026-04-14").first()
        assertEquals(1, completed.size)
        assertEquals(routineId, completed[0].routineId)
    }

    @Test
    fun getCompletedRoutinesForDate_otherDate_returnsEmpty() = runBlocking {
        val routineId = dao.insertRoutine(RoutineEntity(
            name = "독서",
            category = "MIND",
            emoji = "📚",
            scheduledTime = "22:00",
            durationMinutes = 20
        ))
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-14"))

        val other = dao.getCompletedRoutinesForDate("2026-04-15").first()
        assertTrue("다른 날짜 히스토리는 비어있어야 함", other.isEmpty())
    }

    // ── deleteHistory ─────────────────────────────────────────────────────────

    @Test
    fun deleteHistory_removesEntry() = runBlocking {
        val routineId = dao.insertRoutine(RoutineEntity(
            name = "스트레칭",
            category = "HEALTH",
            emoji = "🤸",
            scheduledTime = "07:30",
            durationMinutes = 10
        ))
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-14"))
        dao.deleteHistory(routineId, "2026-04-14")

        val completed = dao.getCompletedRoutinesForDate("2026-04-14").first()
        assertTrue("히스토리 삭제 후 빈 목록이어야 함", completed.isEmpty())
    }

    // ── getCompletedCountForDate ───────────────────────────────────────────────

    @Test
    fun getCompletedCountForDate_returnsCorrectCount() = runBlocking {
        val r1 = dao.insertRoutine(RoutineEntity(name = "루틴A", category = "HEALTH", emoji = "A", scheduledTime = "07:00", durationMinutes = 5))
        val r2 = dao.insertRoutine(RoutineEntity(name = "루틴B", category = "HEALTH", emoji = "B", scheduledTime = "08:00", durationMinutes = 5))

        dao.insertHistory(RoutineHistoryEntity(routineId = r1, completedDate = "2026-04-14"))
        dao.insertHistory(RoutineHistoryEntity(routineId = r2, completedDate = "2026-04-14"))

        val count = dao.getCompletedCountForDate("2026-04-14")
        assertEquals(2, count)
    }

    @Test
    fun getCompletedCountForDate_emptyDate_returnsZero() = runBlocking {
        val count = dao.getCompletedCountForDate("2026-04-14")
        assertEquals(0, count)
    }

    // ── getCompletedDates / streak ────────────────────────────────────────────

    @Test
    fun getCompletedDates_returnsDistinctDatesDescending() = runBlocking {
        val routineId = dao.insertRoutine(RoutineEntity(name = "아침루틴", category = "HEALTH", emoji = "☀", scheduledTime = "07:00", durationMinutes = 10))
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-12"))
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-13"))
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-14"))
        // 같은 날 중복
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-14"))

        val dates = dao.getCompletedDates()
        assertEquals("GROUP BY로 중복 제거 후 3개여야 함", 3, dates.size)
        assertEquals("내림차순 첫번째 날짜", "2026-04-14", dates[0])
    }

    // ── weeklyStats ────────────────────────────────────────────────────────────

    @Test
    fun getWeeklyStats_returnsCountPerDay() = runBlocking {
        val r1 = dao.insertRoutine(RoutineEntity(name = "루틴1", category = "HEALTH", emoji = "1", scheduledTime = "07:00", durationMinutes = 5))
        val r2 = dao.insertRoutine(RoutineEntity(name = "루틴2", category = "HEALTH", emoji = "2", scheduledTime = "08:00", durationMinutes = 5))

        dao.insertHistory(RoutineHistoryEntity(routineId = r1, completedDate = "2026-04-10"))
        dao.insertHistory(RoutineHistoryEntity(routineId = r2, completedDate = "2026-04-10"))
        dao.insertHistory(RoutineHistoryEntity(routineId = r1, completedDate = "2026-04-12"))

        val stats = dao.getWeeklyStats("2026-04-10", "2026-04-14")
        assertEquals(2, stats.size)

        val apr10 = stats.first { it.completedDate == "2026-04-10" }
        assertEquals(2, apr10.count)

        val apr12 = stats.first { it.completedDate == "2026-04-12" }
        assertEquals(1, apr12.count)
    }

    @Test
    fun getWeeklyStats_outsideRange_notIncluded() = runBlocking {
        val routineId = dao.insertRoutine(RoutineEntity(name = "루틴", category = "HEALTH", emoji = "⭐", scheduledTime = "07:00", durationMinutes = 5))
        dao.insertHistory(RoutineHistoryEntity(routineId = routineId, completedDate = "2026-04-01"))

        val stats = dao.getWeeklyStats("2026-04-10", "2026-04-14")
        assertTrue("범위 외 날짜는 포함되지 않아야 함", stats.isEmpty())
    }
}
