package domain.model

data class Portfolio(
    val id: String = "",
    val imageUrl: String = "",
    val serviceId: String? = null,
    val caption: String = "",
    val uploadedAt: Long = 0L
)

