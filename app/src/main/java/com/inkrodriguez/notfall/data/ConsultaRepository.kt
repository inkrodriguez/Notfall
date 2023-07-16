package com.inkrodriguez.notfall.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class ConsultaRepository: Repository() {

    init {
        connectCollection("queries")
    }

    private val consultations = MutableLiveData<List<Consulta>>()

    public fun createNewConsultation(consulta: Consulta, context: Context){

        val data = hashMapOf(
            "email" to consulta.email,
            "specialty" to consulta.specialty,
            "status" to consulta.status,
            "date" to consulta.date,
            "doctor" to ""
        )

        collectionReference?.add(data)?.addOnSuccessListener {
            Toast.makeText(context, "Registrado com sucesso!", Toast.LENGTH_SHORT).show()
        }?.addOnFailureListener {
            Toast.makeText(context, "Erro ao efetuar registro!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateConsultation(consulta: Consulta, context: Context) {
        collectionReference?.whereEqualTo("email", consulta.email)
            ?.whereEqualTo("specialty", consulta.specialty)
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val documentId = document.id
                        val data = hashMapOf(
                            "email" to consulta.email,
                            "specialty" to consulta.specialty,
                            "status" to consulta.status,
                            "date" to consulta.date,
                            "doctor" to consulta.doctor
                        )
                        collectionReference?.document(documentId)?.set(data)
                            ?.addOnSuccessListener {
                                Toast.makeText(context, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            }?.addOnFailureListener {
                                Toast.makeText(context, "Erro ao atualizar!", Toast.LENGTH_SHORT).show()
                            }
                        return@addOnSuccessListener
                    }
                } else {
                    Toast.makeText(context, "Registro não encontrado!", Toast.LENGTH_SHORT).show()
                }
            }
            ?.addOnFailureListener { error ->
                Toast.makeText(context, "Erro ao consultar o registro!", Toast.LENGTH_SHORT).show() }
        }



    fun getAllConsultationsForUser(email: String): LiveData<List<Consulta>> {
        val consultations = MutableLiveData<List<Consulta>>()

        collectionReference?.whereEqualTo("email", email)?.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                // Lidar com o erro, se necessário
                return@addSnapshotListener
            }

            val consultationList = mutableListOf<Consulta>()

            for (document in querySnapshot?.documents.orEmpty()) {
                val specialty = document.getString("specialty")
                val status = document.getString("status")
                val date = document.getTimestamp("date")
                val doctor = document.getString("doctor")

                val consultation = Consulta(email, specialty, status, date, doctor)
                consultationList.add(consultation)
            }

            consultations.value = consultationList
        }

        return consultations
    }


    fun observeConsultasAndSize(): Flow<Pair<List<Consulta>, Int>> = callbackFlow {
        val collectionRef = firestoreManager.firestore.collection("queries")
        val list = listOf("espera", "atendido")
        val listener = collectionRef.whereIn("status", list)
            .addSnapshotListener { querySnapshot, _ ->
                val consultas = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(Consulta::class.java)
                } ?: emptyList()

                val count = if (querySnapshot?.documents?.size == 0) 0 else querySnapshot?.documentChanges?.size ?: 0

                try {
                    trySend(Pair(consultas, count)).isSuccess // Oferece a lista de consultas e o tamanho para o fluxo combinado
                } catch (e: Exception) {
                    // Trate a exceção, se necessário
                }
            }

        // Cancela o listener quando o fluxo é cancelado
        awaitClose {
            listener.remove()
        }
    }


    suspend fun getConsultationsByEmail(email: String, specialty: String): Consulta? {
        val collectionRef = firestoreManager.firestore.collection("queries")
        val querySnapshot = collectionRef.whereEqualTo("email", email).whereEqualTo("specialty", specialty).get().await()
        val document = querySnapshot.documents.firstOrNull()
        val consulta = document?.toObject(Consulta::class.java)

        return consulta
    }


}
