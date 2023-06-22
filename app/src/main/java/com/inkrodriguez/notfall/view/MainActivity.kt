package com.inkrodriguez.notfall.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.LoginRepository
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

        val loginRepository = LoginRepository(this.applicationContext)

        binding.btnEntrarLogin.setOnClickListener {
            val username = binding.editUserLogin.text
            val password = binding.editPasswordLogin.text
            val checkBox = binding.checkBoxLogin

            GlobalScope.launch(Dispatchers.Main) {
                val loggedIn = loginRepository.loginUser(username, password)
                if (loggedIn) {
                    if (checkBox.isChecked) {
                        loginRepository.rememberUser(username, password)
                        Toast.makeText(this@MainActivity, "Suas informações foram salvas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Checkbox não está marcado ou usuário inválido", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}