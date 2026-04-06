package presentation.repository

import com.google.firebase.firestore.FirebaseFirestore
import domain.model.Appointment
import domain.model.Block
import domain.model.Schedule
import kotlinx.coroutines.tasks.await

class AppointmentRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun getSchedule(date: String): Schedule? {
        val doc = db.collection("schedule").document(date).get().await()
        return if (doc.exists()) doc.toObject(Schedule::class.java) else null
    }

    suspend fun getBlocks(date: String): List<Block> {
        val snapshot = db.collection("blocks")
            .whereEqualTo("date", date)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Block::class.java) }
    }

    suspend fun getAppointments(date: String): List<Appointment> {
        val snapshot = db.collection("appointments")
            .whereEqualTo("date", date)
            .whereEqualTo("status", "active")
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Appointment::class.java) }
    }

    suspend fun createAppointment(appointment: Appointment) {
        val docRef = db.collection("appointments").document()
        val withId = appointment.copy(id = docRef.id)
        docRef.set(withId).await()
        // Автоматически обновляем историю клиента
        updateClientHistory(withId)
    }

    private suspend fun updateClientHistory(appointment: Appointment) {
        val histRef = db.collection("clients_history").document(appointment.clientId)
        val doc = histRef.get().await()
        val existing = if (doc.exists()) doc.toObject(domain.model.ClientHistory::class.java) else null
        val newNote = domain.model.VisitNote(
            date = appointment.date,
            serviceName = appointment.serviceName,
            note = ""
        )
        if (existing == null) {
            val history = domain.model.ClientHistory(
                clientId = appointment.clientId,
                clientName = appointment.clientName,
                clientPhone = appointment.clientPhone,
                totalVisits = 1,
                notes = listOf(newNote)
            )
            histRef.set(history).await()
        } else {
            val updatedNotes = existing.notes + newNote
            histRef.update(
                mapOf(
                    "totalVisits" to (existing.totalVisits + 1),
                    "notes" to updatedNotes
                )
            ).await()
        }
    }

    suspend fun getClientAppointments(clientId: String): List<Appointment> {
        val snapshot = db.collection("appointments")
            .whereEqualTo("clientId", clientId)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Appointment::class.java) }
    }

    suspend fun cancelAppointment(appointmentId: String) {
        db.collection("appointments")
            .document(appointmentId)
            .update("status", "cancelled")
            .await()
    }

    // ─── Расписание ──────────────────────────────────────────────────────────

    suspend fun saveSchedule(schedule: Schedule) {
        db.collection("schedule")
            .document(schedule.date)
            .set(schedule)
            .await()
    }

    // ─── Личные блоки ────────────────────────────────────────────────────────

    suspend fun saveBlock(block: Block) {
        val docRef = if (block.id.isEmpty())
            db.collection("blocks").document()
        else
            db.collection("blocks").document(block.id)
        val withId = block.copy(id = docRef.id)
        docRef.set(withId).await()
    }

    suspend fun deleteBlock(blockId: String) {
        db.collection("blocks").document(blockId).delete().await()
    }

    // ─── Все записи (для любой даты) ─────────────────────────────────────────

    suspend fun getAllAppointments(): List<Appointment> {
        val snapshot = db.collection("appointments").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Appointment::class.java) }
    }

    // ─── Управление услугами ─────────────────────────────────────────────────

    suspend fun saveService(service: domain.model.Service) {
        val docRef = if (service.id.isEmpty())
            db.collection("services").document()
        else
            db.collection("services").document(service.id)
        val withId = service.copy(id = docRef.id)
        docRef.set(withId).await()
    }

    suspend fun deleteService(serviceId: String) {
        db.collection("services").document(serviceId).delete().await()
    }

    // ─── История клиентов ────────────────────────────────────────────────────

    suspend fun getAllClientHistories(): List<domain.model.ClientHistory> {
        val snapshot = db.collection("clients_history").get().await()
        return snapshot.documents.mapNotNull { it.toObject(domain.model.ClientHistory::class.java) }
    }

    suspend fun saveClientHistory(history: domain.model.ClientHistory) {
        db.collection("clients_history")
            .document(history.clientId)
            .set(history)
            .await()
    }

    /** Вычисляет доступные слоты на дату для услуги с указанной длительностью */
    suspend fun getAvailableSlots(date: String, serviceDuration: Int): List<String> {
        val schedule = getSchedule(date) ?: return emptyList()
        if (!schedule.isWorkingDay) return emptyList()

        val startTime = schedule.startTime ?: return emptyList()
        val endTime = schedule.endTime ?: return emptyList()

        val allSlots = generateSlots(startTime, endTime, 30)

        // Убираем перерыв
        val breakSlots: Set<String> = if (schedule.hasBreak &&
            schedule.breakStart != null && schedule.breakEnd != null
        ) {
            generateSlots(schedule.breakStart, schedule.breakEnd, 30).toSet()
        } else emptySet()

        // Убираем личные блоки
        val blocks = getBlocks(date)
        val blockedSlots: Set<String> = blocks.flatMap { block ->
            generateSlots(block.startTime, block.endTime, 30)
        }.toSet()

        // Убираем занятые записями слоты
        val appointments = getAppointments(date)
        val appointmentSlots: Set<String> = appointments.flatMap { appt ->
            generateSlots(appt.time, addMinutes(appt.time, appt.duration), 30)
        }.toSet()

        val busySlots = breakSlots + blockedSlots + appointmentSlots

        // Оставляем только слоты, куда целиком помещается услуга
        val workEnd = toMinutes(endTime)
        return allSlots.filter { slot ->
            val slotStart = toMinutes(slot)
            val slotEnd = slotStart + serviceDuration
            slotEnd <= workEnd && !busySlots.contains(slot) &&
                // все 30-минутные подслоты внутри услуги должны быть свободны
                generateSlots(slot, addMinutes(slot, serviceDuration), 30).none { it in busySlots }
        }
    }

    private fun generateSlots(from: String, to: String, stepMinutes: Int): List<String> {
        val result = mutableListOf<String>()
        var current = toMinutes(from)
        val end = toMinutes(to)
        while (current < end) {
            result.add(fromMinutes(current))
            current += stepMinutes
        }
        return result
    }

    private fun toMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun fromMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }

    private fun addMinutes(time: String, minutes: Int): String {
        return fromMinutes(toMinutes(time) + minutes)
    }
}

