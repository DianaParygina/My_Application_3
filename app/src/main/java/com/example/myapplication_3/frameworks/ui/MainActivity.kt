package com.example.myapplication_3.frameworks.ui
import com.example.myapplication_3.utils.BaseMenu
import com.example.myapplication_3.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseMenu() {
    override fun getLayoutResId(): Int {
        return R.layout.activity_finance
    }
}