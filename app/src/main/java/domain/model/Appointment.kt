package domain.model

data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val date: String = "",
    val time: String = "",
    val duration: Int = 0,
    val status: String = "active",
    val createdAt: Long = 0L
)
