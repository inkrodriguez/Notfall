package com.inkrodriguez.notfall.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.inkrodriguez.notfall.data.User
import com.inkrodriguez.notfall.data.UserRepository
import com.inkrodriguez.notfall.data.Utilities
import com.inkrodriguez.notfall.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = UserRepository(this)
        val btnRegister = binding.btnRegister
        val utilities = Utilities(this)

        btnRegister.setOnClickListener {
            val editEmail = binding.editEmail.text
            val editPassword = binding.editPassword.text

            val user = User(email = editEmail.toString(), password = editPassword.toString(), category = "paciente")

            if(editEmail.isEmpty() || editPassword.isEmpty()){
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else if(editEmail.contains("@")) {
                Toast.makeText(this, "Digite um endereço de e-mail válido!", Toast.LENGTH_SHORT).show()
            } else {
                userRepository.createNewUser(user, this)
                utilities.iniciarActivity(MainActivity::class.java)
            }
        }

    }
}