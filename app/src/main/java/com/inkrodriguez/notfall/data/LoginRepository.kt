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

    suspend fun recoverUserFirebase(username: String): User? {
        val collectionRef = firestoreManager.firestore.collection("users")

        val querySnapshot = collectionRef.whereEqualTo("username", username).get().await()
        for (document in querySnapshot) {
            val username = document.getString("username")
            val password = document.getString("password")
            val categoria = document.getString("categoria")
            val specialty = document.getString("specialty")
            val user = User(username.toString(), password.toString(), categoria.toString(), specialty.toString())
            return user
        }
        return null
    }
}