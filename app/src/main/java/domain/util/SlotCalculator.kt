package domain.util

import domain.model.Appointment
import domain.model.Block
import domain.model.Schedule

object SlotCalculator {

    fun calculateAvailableSlots(
        schedule: Schedule?,
        blocks: List<Block>,
        appointments: List<Appointment>,
        serviceDuration: Int,
        slotStepMinutes: Int = 30
    ): List<String> {
        if (schedule == null || !schedule.isWorkingDay) return emptyList()

        val startTime = schedule.startTime ?: return emptyList()
        val endTime = schedule.endTime ?: return emptyList()

        val allSlots = generateSlots(startTime, endTime, slotStepMinutes)

        val breakSlots: Set<String> = if (schedule.hasBreak &&
            schedule.breakStart != null && schedule.breakEnd != null
        ) {
            generateSlots(schedule.breakStart, schedule.breakEnd, slotStepMinutes).toSet()
        } else {
            emptySet()
        }

        val blockedSlots: Set<String> = blocks.flatMap { block ->
            generateSlots(block.startTime, block.endTime, slotStepMinutes)
        }.toSet()

        val appointmentSlots: Set<String> = appointments.flatMap { appt ->
            generateSlots(appt.time, addMinutes(appt.time, appt.duration), slotStepMinutes)
        }.toSet()

        val busySlots = breakSlots + blockedSlots + appointmentSlots
        val workEnd = toMinutes(endTime)

        return allSlots.filter { slot ->
            val slotStart = toMinutes(slot)
            val slotEnd = slotStart + serviceDuration
            slotEnd <= workEnd &&
                !busySlots.contains(slot) &&
                generateSlots(slot, addMinutes(slot, serviceDuration), slotStepMinutes)
                    .none { it in busySlots }
        }
    }

    fun generateSlots(from: String, to: String, stepMinutes: Int): List<String> {
        val result = mutableListOf<String>()
        var current = toMinutes(from)
        val end = toMinutes(to)
        while (current < end) {
            result.add(fromMinutes(current))
            current += stepMinutes
        }
        return result
    }

    fun toMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun fromMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }

    fun addMinutes(time: String, minutes: Int): String {
        return fromMinutes(toMinutes(time) + minutes)
    }
}
