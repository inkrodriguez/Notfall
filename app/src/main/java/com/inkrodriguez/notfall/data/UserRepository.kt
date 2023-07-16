package com.inkrodriguez.notfall.data

import android.content.Context
import android.text.Editable
import android.widget.Toast
import kotlinx.coroutines.tasks.await

class UserRepository(context: Context) : Repository() {

    val sharedPreferences = context.getSharedPreferences("infoUser", Context.MODE_PRIVATE)

    public fun createNewUser(user: User, context: Context){

        val data = hashMapOf(
            "email" to user.email,
            "password" to user.password,
            "category" to user.category,
            "specialty" to user.specialty
        )

        connectCollection("users")
        collectionReference?.add(data)?.addOnSuccessListener {
            Toast.makeText(context, "Registrado com sucesso!", Toast.LENGTH_SHORT).show()
        }?.addOnFailureListener {
            Toast.makeText(context, "Erro ao efetuar registro!", Toast.LENGTH_SHORT).show()
        }

    }

    public suspend fun getUser(email: String): User? {
        connectCollection("users")

        val querySnapshot = collectionReference?.whereEqualTo("email", email)?.get()?.await()

        if (querySnapshot != null && !querySnapshot.isEmpty) {
            val document = querySnapshot.documents[0]
            val username = document.getString("username").toString()
            val password = document.getString("password").toString()
            val categoria = document.getString("categoria").toString()
            val specialty = document.getString("specialty").toString()
            return User(username, password, categoria, specialty)
        } else {
            return null
        }
    }

    fun getMedicalUsers(onComplete: (MutableList<String>) -> Unit) {
        val listMedicals: MutableList<String> = mutableListOf()

        connectCollection("users")
        collectionReference?.whereEqualTo("categoria", "medico")?.get()?.addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    val fullname = document.getString("fullname").toString()
                    listMedicals.add(fullname)
                }
            }
            // Chamar o callback onComplete e passar a lista de fullnames
            onComplete.invoke(listMedicals)
        }
    }






    public fun loginUser(user: User, context: Context, callback: (Boolean) -> Unit) {
        connectCollection("users")

        collectionReference?.whereEqualTo("email", user.email)?.whereEqualTo("password", user.password)
            ?.get()?.addOnSuccessListener { querySnapshot ->
                val loggedIn = !querySnapshot.isEmpty

                if (loggedIn) {
                    Toast.makeText(context, "Você fez login", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Usuário não encontrado!", Toast.LENGTH_SHORT).show()
                }

                callback(loggedIn)
            }?.addOnFailureListener {
                Toast.makeText(context, "Erro ao efetuar login!", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }


    fun rememberUserCheckBox(editUsername: Editable, editPassword: Editable) {
        val editor = sharedPreferences.edit()
        editor.putString("email", editUsername.toString())
        editor.putString("password", editPassword.toString())
        editor.apply()
    }

    fun recoverUserCheckBox(): User {
        val email = sharedPreferences.getString("email", "Usuário").toString()
        val password = sharedPreferences.getString("password", "Senha").toString()
        return User(email, password)
    }

}