package com.example.myapplication_3.Frameworks.ui
import com.example.myapplication_3.Utils.BaseMenu
import com.example.myapplication_3.R
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

@AndroidEntryPoint
class MainActivity : BaseMenu() {
    override fun getLayoutResId(): Int {
        return R.layout.activity_finance
    }
}