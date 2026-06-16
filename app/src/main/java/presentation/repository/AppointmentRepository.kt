package presentation.repository

import com.google.firebase.firestore.FirebaseFirestore
import domain.model.Appointment
import domain.model.Block
import domain.model.Schedule
import domain.util.SlotCalculator
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

    /** Все записи на дату (любой статус) — для экрана админки */
    suspend fun getAppointmentsForAdmin(date: String): List<Appointment> {
        val snapshot = db.collection("appointments")
            .whereEqualTo("date", date)
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

    suspend fun completeAppointment(appointmentId: String) {
        db.collection("appointments")
            .document(appointmentId)
            .update("status", "completed")
            .await()
    }

    /** Автоматически завершает все активные записи, дата которых уже прошла */
    suspend fun autoCompleteOldAppointments(clientId: String) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val snapshot = db.collection("appointments")
            .whereEqualTo("clientId", clientId)
            .whereEqualTo("status", "active")
            .get().await()
        snapshot.documents.forEach { doc ->
            val date = doc.getString("date") ?: return@forEach
            if (date < today) {
                doc.reference.update("status", "completed")
            }
        }
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
        val schedule = getSchedule(date)
        val blocks = getBlocks(date)
        val appointments = getAppointments(date)
        return SlotCalculator.calculateAvailableSlots(
            schedule = schedule,
            blocks = blocks,
            appointments = appointments,
            serviceDuration = serviceDuration
        )
    }
}

