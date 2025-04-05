import com.google.gson.annotations.SerializedName

data class Person(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val year: Int,
    val yearOfAdmission: Int,
    val specialtyTitle: String? = null
)