import com.example.myapplication_3.person.Person
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_3.R


class PersonAdapter(private val persons: List<Person>) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameTextView)
        private val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameTextView)
        private val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        private val yearOfAdmissionTextView: TextView = itemView.findViewById(R.id.yearOfAdmissionTextView)
        private val specialtyTitleTextView: TextView = itemView.findViewById(R.id.specialtyTitleTextView)

        fun bind(person: Person) {
            firstNameTextView.text = person.firstName
            lastNameTextView.text = person.lastName
            yearTextView.text = person.year.toString()
            yearOfAdmissionTextView.text = person.yearOfAdmission.toString()
            specialtyTitleTextView.text = person.specialtyTitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_item, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = persons[position]
        holder.bind(person)
    }

    override fun getItemCount(): Int = persons.size
}