import com.google.gson.annotations.SerializedName

data class Person(
    val id: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    val year: Int,
    @SerializedName("yearOfAdmission")
    val yearOfAdmission: Int,
    val specialtyTitle: String? = null
)