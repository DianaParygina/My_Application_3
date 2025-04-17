import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.myapplication_3.MyApplication
import com.example.myapplication_3.R
import com.example.myapplication_3.SharedFinanceViewModel

class BalanceFragment : Fragment() {

    private lateinit var tvBalance: TextView
    private lateinit var sharedFinanceViewModel: SharedFinanceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_balance, container, false)

        tvBalance = view.findViewById(R.id.tvBalance)
        sharedFinanceViewModel = (requireActivity().application as MyApplication).sharedFinanceViewModel

        sharedFinanceViewModel.totalBalance.observe(viewLifecycleOwner, Observer { newBalance ->
            tvBalance.text = "Ваш баланс: ${newBalance} руб"
        })

        return view
    }
}