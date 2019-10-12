package com.jqk.wifitest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jqk.wifitest.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.view = this

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = TCPFragment()
        fragmentTransaction.add(R.id.fragment, fragment)
        fragmentTransaction.commit()

//        val fragmentTransaction2 = fragmentManager.beginTransaction()
//        val fragment2 = TCPFragment()
//        fragmentTransaction2.add(R.id.fragment2, fragment2)
//        fragmentTransaction2.commit()

    }
}
