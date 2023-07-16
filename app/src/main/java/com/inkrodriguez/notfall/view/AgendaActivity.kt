package com.inkrodriguez.notfall.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.adapter.ViewPagerAdapter
import com.inkrodriguez.notfall.data.*
import com.inkrodriguez.notfall.databinding.ActivityAgendaBinding
import com.inkrodriguez.notfall.fragments.AgendaFragment
import com.inkrodriguez.notfall.fragments.ConsultaFragment
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class AgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendaBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_Agenda -> {
                    val agendaFragment = AgendaFragment() // substitua HomeFragment pelo seu próprio fragmento
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, agendaFragment)
                        .commit()
                    true
                }
                R.id.navigation_Consultas -> {
                    val consultaFragment = ConsultaFragment() // substitua HomeFragment pelo seu próprio fragmento
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, consultaFragment)
                        .commit()
                    true
                }
                else -> false
            }
        }

    }



}
