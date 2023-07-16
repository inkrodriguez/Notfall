package com.inkrodriguez.notfall.data

import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred

class MessageRepository {

    private val firestoreManager: FirestoreManager = FirestoreManager()

    suspend fun messageData(): MutableList<Message>? {
        val collectionRef = firestoreManager.firestore.collection("messages")
            .orderBy("date", Query.Direction.ASCENDING)
        val deferred = CompletableDeferred<List<Message>?>()

        collectionRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                deferred.complete(null)
                return@addSnapshotListener
            }

            val messages = mutableListOf<Message>()
            for (document in snapshot!!) {
                val message = Message()
                message.addressee = document.getString("addressee").toString()
                message.sender = document.getString("sender").toString()
                message.message = document.getString("message")
                message.username = document.getString("username").toString()
                message.date = document.getDate("date")
                message.specialty = document.getString("specialty")
                messages.add(message)
            }
            deferred.complete(messages)
        }

        return deferred.await() as MutableList<Message>?
    }
}
