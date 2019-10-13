package com.jqk.wifitest

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.jqk.wifitest.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @SuppressLint("InvalidWakeLockTag")
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
        setWifiDormancy(this)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag")
        wl.acquire()

    }

    fun setWifiDormancy(context: Context) {
        val value = Settings.System.getInt(
            context.contentResolver,
            Settings.System.WIFI_SLEEP_POLICY,
            Settings.System.WIFI_SLEEP_POLICY_DEFAULT
        )
        Log.d("", "setWifiDormancy() returned: $value")
        val prefs = context.getSharedPreferences("wifi_sleep_policy", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(Settings.System.WIFI_SLEEP_POLICY, value)
        editor.commit()

        if (Settings.System.WIFI_SLEEP_POLICY_NEVER !== value) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.WIFI_SLEEP_POLICY,
                Settings.System.WIFI_SLEEP_POLICY_NEVER
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
