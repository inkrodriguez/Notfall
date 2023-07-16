package com.inkrodriguez.notfall.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.LoginRepository
import com.inkrodriguez.notfall.data.User
import com.inkrodriguez.notfall.data.UserRepository
import com.inkrodriguez.notfall.data.Utilities
import com.inkrodriguez.notfall.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val utilities = Utilities(this)
        val email = binding.editEmailLogin
        val password = binding.editPasswordLogin
        val checkBox = binding.checkBoxLogin
        val btnRegister = binding.btnRegister
        val userRepository = UserRepository(this)

        val recoverUserCheckBox = userRepository.recoverUserCheckBox()
        val user = User(recoverUserCheckBox.email, recoverUserCheckBox.password)

        if(user.email.isNotEmpty() && user.password.isNotEmpty()) {
            email.setText(user.email)
            password.setText(user.password)
            checkBox.isChecked = true
        } else {
            email.setText("")
            password.setText("")
            checkBox.isChecked = false
        }

        binding.btnEntrarLogin.setOnClickListener {
            val email = binding.editEmailLogin.text
            val password = binding.editPasswordLogin.text
            val checkBox = binding.checkBoxLogin
            val user = User(email.toString(), password.toString())

            if (checkBox.isChecked) {
                userRepository.rememberUserCheckBox(email, password)
            }

            userRepository.loginUser(user, this) { loggedIn ->
                if (loggedIn) {
                    utilities.iniciarActivity(HomeActivity::class.java)
                }
            }
        }

        btnRegister.setOnClickListener {
            utilities.iniciarActivity(RegisterActivity::class.java)
        }

    }
}