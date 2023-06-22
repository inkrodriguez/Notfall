package com.inkrodriguez.notfall.data

import android.content.Context
import android.text.Editable
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginRepository(context: Context) {

    val firestoreManager = FirestoreManager()
    val collectionUsers = firestoreManager.firestore.collection("users")
    val sharedPreferences = context.getSharedPreferences("infoUser", Context.MODE_PRIVATE)
    val context = context


    fun rememberUser(editUsername: Editable, editPassword: Editable) {
        val editor = sharedPreferences.edit()
        editor.putString("username", editUsername.toString())
        editor.putString("password", editPassword.toString())
        editor.apply()
    }

    suspend fun loginUser(editUsername: Editable, editPassword: Editable): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = collectionUsers.get().await()
                querySnapshot.forEach {
                    val username = it.getString("username")
                    val password = it.getString("password")
                    val categoria = it.getString("categoria")

                    if (editUsername.toString() == username && editPassword.toString() == password) {
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }


    fun createUser(username: String, password: String, categoria: Int){

    }
}