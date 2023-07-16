package com.inkrodriguez.notfall.data

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.CollectionReference

open class Repository {

    val firestoreManager = FirestoreManager()
    var collectionReference: CollectionReference? = null

    fun connectCollection(collectionRef: String) {
        collectionReference = firestoreManager.firestore.collection(collectionRef)
    }

}
