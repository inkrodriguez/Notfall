package com.inkrodriguez.notfall.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class ServiceRepository {

    private val firestoreManager: FirestoreManager = FirestoreManager()

    suspend fun serviceData(): Service? {
        val collectionRef = firestoreManager.firestore.collection("service")
        val deferred = CompletableDeferred<Service?>()

        collectionRef.document().addSnapshotListener { value, error ->
            if (value != null) {
                val service = Service()
                service.attendant = value.getString("attendant")
                service.username = value.getString("username")
                service.date = value.getDate("date")
                service.specialty = value.getString("specialty")
                service.status = value.getString("status")
                deferred.complete(service)
            } else {
                deferred.complete(null)
            }
        }

        return deferred.await()
    }
    suspend fun observeSizeEmergency(): Flow<Int> = callbackFlow {
        val collectionRef = firestoreManager.firestore.collection("emergency")
        val list = listOf("espera", "atendido")
        val listener = collectionRef.whereIn("status", list)
            .addSnapshotListener { querySnapshot, _ ->
                val count = querySnapshot?.documentChanges?.size ?: 0
                try {
                    trySend(count).isSuccess // Oferece o valor do contador para o fluxo
                } catch (e: Exception) {
                    // Trate a exceção, se necessário
                }
            }

        // Cancela o listener quando o fluxo é cancelado
        awaitClose {
            listener.remove()
        }
    }

    suspend fun observeSizeServices(specialty: String): Flow<Int> = callbackFlow {
        val collectionRef = firestoreManager.firestore.collection("service")
        val list = listOf("espera", "atendido")
        val listener = collectionRef.whereIn("status", list)
            .whereEqualTo("specialty", specialty)
            .addSnapshotListener { querySnapshot, _ ->
                val count = querySnapshot?.documentChanges?.size ?: 0
                try {
                    trySend(count).isSuccess // Oferece o valor do contador para o fluxo
                } catch (e: Exception) {
                    // Trate a exceção, se necessário
                }
            }

        // Cancela o listener quando o fluxo é cancelado
        awaitClose {
            listener.remove()
        }
    }

    suspend fun getServiceByUsername(username: String): Service? {
        val collectionRef = firestoreManager.firestore.collection("service")
        val querySnapshot = collectionRef.whereEqualTo("username", username).get().await()
        val document = querySnapshot.documents.firstOrNull()
        val service = document?.toObject(Service::class.java)

        if (service != null && (service.status == "espera" || service.status == "atendido")) {
            return service
        }
            return service
    }





}
