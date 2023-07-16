package com.inkrodriguez.notfall.view

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.inkrodriguez.notfall.adapter.MyAdapter
import com.inkrodriguez.notfall.data.*
import com.inkrodriguez.notfall.databinding.ActivityChatBinding
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val firestoreManager = FirestoreManager()
    private val userRepository: UserRepository by lazy { UserRepository(this) }
    private val messagesList: MutableList<Message> = mutableListOf()
    private var messagesListener: ListenerRegistration? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = userRepository.recoverUserCheckBox().email
        val selectedOption = intent.getStringExtra("value").toString()

        // Inicializar o RecyclerView e o Adapter
        val recyclerViewMessages = binding.recyclerViewChat
        val messagesAdapter = MyAdapter(messagesList)
        recyclerViewMessages.adapter = messagesAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        readMessages(username, messagesAdapter, selectedOption)

        val message = binding.editMessage
        val btnEnviar = binding.btnEnviar

        btnEnviar.setOnClickListener {
            sendMessage(selectedOption, message.text)
            message.setText("")
        }

    }

    private fun readMessages(username: String, messagesAdapter: MyAdapter, specialty: String) {
        val collectionRef = firestoreManager.firestore.collection("messages")
        val query = collectionRef.whereEqualTo("username", username).whereEqualTo("specialty", specialty)
            .orderBy("date", Query.Direction.ASCENDING)

        messagesListener = query.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                // Erro ao recuperar as mensagens
                Toast.makeText(this, "Erro ao recuperar as mensagens: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            querySnapshot?.let { snapshot ->
                val messages = snapshot.documents.mapNotNull { document ->
                    document.toObject(Message::class.java)
                }

                // Limpar a lista de mensagens e adicionar as mensagens recuperadas
                messagesList.clear()
                messagesList.addAll(messages)
                messagesAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun sendMessage(specialty: String, message: Editable) {
        val collectionRef = firestoreManager.firestore.collection("messages")
        val username = userRepository.recoverUserCheckBox().email
        val timestamp = com.google.firebase.Timestamp.now()

        val data = hashMapOf(
            "sender" to username,
            "username" to username,
            "specialty" to specialty,
            "message" to message.toString(),
            "addressee" to "",
            "date" to timestamp
        )

        collectionRef.add(data)
            .addOnSuccessListener { documentReference ->
                // Sucesso ao adicionar os dados
                Toast.makeText(this, "mensagem enviada!", Toast.LENGTH_SHORT).show()
                checkIfDataExists(username, specialty)
            }
            .addOnFailureListener { e ->
                // Erro ao adicionar os dados
                Toast.makeText(this, "Erro ao enviar mensagem: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun checkIfDataExists(username: String, specialty: String) {
        val collectionRef = firestoreManager.firestore.collection("service")
        val query = collectionRef.whereEqualTo("username", username)
            .whereEqualTo("specialty", specialty)
            .whereIn("status", listOf("espera", "atendido")) // Verifica se o status é "espera" ou "atendido"
            .limit(1)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Os dados não existem, criar um novo
                    createNewData(username, specialty)
                    // Envia mensagem automática
                    sendAutomaticMessage(username)
                } else {
                    // Os dados já existem
                    Toast.makeText(this, "Os dados já existem.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Erro ao executar a consulta
                Toast.makeText(this, "Erro ao verificar os dados: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun createNewData(username: String, specialty: String) {
        val timestamp = com.google.firebase.Timestamp.now()
        val collectionRef = firestoreManager.firestore.collection("service")

        val data = hashMapOf(
            "username" to username,
            "date" to timestamp,
            "status" to "espera",
            "attendant" to "",
            "specialty" to specialty
            // Outros campos de dados que você deseja adicionar
        )

        collectionRef.add(data)
            .addOnSuccessListener { documentReference ->
                // Sucesso ao adicionar os dados
                Toast.makeText(this, "Novos dados criados!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Erro ao adicionar os dados
                Toast.makeText(this, "Erro ao criar novos dados: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private fun sendAutomaticMessage(username: String){
        val selectedOption = intent.getStringExtra("value").toString()
        val collectionRef = firestoreManager.firestore.collection("messages")
        val timestamp = com.google.firebase.Timestamp.now()

        val data = hashMapOf(
            "sender" to "Mensagem Automática",
            "username" to username,
            "addressee" to selectedOption,
            "message" to "Aguarde, você logo será atendido!",
            "specialty" to selectedOption,
            "date" to timestamp
        )

        collectionRef.add(data)
            .addOnSuccessListener { documentReference ->
                // Sucesso ao adicionar os dados
                Toast.makeText(this, "mensagem enviada!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Erro ao adicionar os dados
                Toast.makeText(this, "Erro ao enviar mensagem: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
