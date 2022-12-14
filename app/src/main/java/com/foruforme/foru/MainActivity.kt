package com.foruforme.foru

import ChatFragment
import OverviewFragment
import SettingFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.foruforme.foru.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

private lateinit var overviewFragment: OverviewFragment
private lateinit var chatFragment: ChatFragment
private lateinit var settingFragment: SettingFragment

private lateinit var binding: ActivityMainBinding
private val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener(this)
        initBottomNavigation()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, "onBackBtn")
        finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        print(item.itemId)
        when (item.itemId) {
            R.id.menu_overview -> {
                overviewFragment = OverviewFragment.newInstance()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.framelayout, overviewFragment).commit();
            }
            R.id.menu_chat -> {
                chatFragment = ChatFragment.newInstance()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.framelayout, chatFragment).commit();
            }
            R.id.menu_setting -> {
                settingFragment = SettingFragment.newInstance()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.framelayout, settingFragment).commit();
            }
        }
        return true
    }

    private fun initBottomNavigation() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.framelayout, OverviewFragment())
            .commitAllowingStateLoss()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.framelayout -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.framelayout, OverviewFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                else -> {return@setOnItemSelectedListener true}
            }
        }
    }
}