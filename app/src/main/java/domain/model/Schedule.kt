package domain.model

data class Schedule(
    val date: String = "",
    val isWorkingDay: Boolean = false,
    val startTime: String? = null,
    val endTime: String? = null,
    val hasBreak: Boolean = false,
    val breakStart: String? = null,
    val breakEnd: String? = null
)

