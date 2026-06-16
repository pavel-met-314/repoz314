package domain.util

import domain.model.Appointment
import domain.model.Block
import domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SlotCalculatorTest {

    private val workingSchedule = Schedule(
        date = "2024-05-20",
        isWorkingDay = true,
        startTime = "10:00",
        endTime = "19:00",
        hasBreak = true,
        breakStart = "13:00",
        breakEnd = "14:00"
    )

    @Test
    fun `returns empty when not working day`() {
        val schedule = workingSchedule.copy(isWorkingDay = false)
        val slots = SlotCalculator.calculateAvailableSlots(schedule, emptyList(), emptyList(), 30)
        assertTrue(slots.isEmpty())
    }

    @Test
    fun `returns empty when schedule is null`() {
        val slots = SlotCalculator.calculateAvailableSlots(null, emptyList(), emptyList(), 30)
        assertTrue(slots.isEmpty())
    }

    @Test
    fun `excludes break slots`() {
        val slots = SlotCalculator.calculateAvailableSlots(workingSchedule, emptyList(), emptyList(), 30)
        assertTrue("13:00" !in slots)
        assertTrue("13:30" !in slots)
        assertTrue("10:00" in slots)
        assertTrue("12:30" in slots)
        assertTrue("14:00" in slots)
    }

    @Test
    fun `excludes personal blocks`() {
        val blocks = listOf(
            Block(id = "1", date = "2024-05-20", startTime = "15:00", endTime = "16:00", reason = "Врач")
        )
        val slots = SlotCalculator.calculateAvailableSlots(workingSchedule, blocks, emptyList(), 30)
        assertTrue("15:00" !in slots)
        assertTrue("15:30" !in slots)
        assertTrue("14:30" in slots)
        assertTrue("16:00" in slots)
    }

    @Test
    fun `excludes active appointments by duration`() {
        val appointments = listOf(
            Appointment(
                id = "1",
                clientId = "c1",
                clientName = "Test",
                clientPhone = "123",
                serviceId = "s1",
                serviceName = "Стрижка",
                date = "2024-05-20",
                time = "10:00",
                duration = 60,
                status = "active"
            )
        )
        val slots = SlotCalculator.calculateAvailableSlots(workingSchedule, emptyList(), appointments, 30)
        assertTrue("10:00" !in slots)
        assertTrue("10:30" !in slots)
        assertTrue("11:00" in slots)
    }

    @Test
    fun `service duration 60 requires consecutive free slots`() {
        val appointments = listOf(
            Appointment(
                id = "1",
                clientId = "c1",
                clientName = "Test",
                clientPhone = "123",
                serviceId = "s1",
                serviceName = "Стрижка",
                date = "2024-05-20",
                time = "10:00",
                duration = 60,
                status = "active"
            )
        )
        val slots30 = SlotCalculator.calculateAvailableSlots(workingSchedule, emptyList(), appointments, 30)
        val slots60 = SlotCalculator.calculateAvailableSlots(workingSchedule, emptyList(), appointments, 60)

        assertTrue("10:00" !in slots30)
        assertTrue("10:30" !in slots30)
        assertTrue("10:00" !in slots60)
        assertTrue("10:30" !in slots60)
        assertTrue("11:00" in slots60)
    }

    @Test
    fun `slot must fit before end of work day`() {
        val slots = SlotCalculator.calculateAvailableSlots(workingSchedule, emptyList(), emptyList(), 60)
        assertTrue("18:30" !in slots)
        assertTrue("18:00" in slots)
    }

    @Test
    fun generateSlots_producesCorrectSteps() {
        assertEquals(listOf("10:00", "10:30", "11:00"), SlotCalculator.generateSlots("10:00", "11:30", 30))
    }
}
