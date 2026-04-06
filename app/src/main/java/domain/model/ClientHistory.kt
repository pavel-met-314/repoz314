package domain.model

data class ClientHistory(
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val totalVisits: Int = 0,
    val notes: List<VisitNote> = emptyList()
)

data class VisitNote(
    val date: String = "",
    val serviceName: String = "",
    val note: String = ""
)

