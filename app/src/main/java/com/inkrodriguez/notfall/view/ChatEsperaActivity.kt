package com.inkrodriguez.notfall.view

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.inkrodriguez.notfall.adapter.MyAdapter
import com.inkrodriguez.notfall.data.*
import com.inkrodriguez.notfall.databinding.ActivityChatEsperaBinding
import kotlinx.coroutines.launch

class ChatEsperaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatEsperaBinding
    private val firestoreManager = FirestoreManager()
    private val userRepository: UserRepository by lazy { UserRepository(this) }
    private val loginRepository: LoginRepository by lazy { LoginRepository(this) }
    private lateinit var adapter: MyAdapter
    private val messagesList: MutableList<Message> = mutableListOf()
    private lateinit var service: Service
    private var messagesListener: ListenerRegistration? = null

    override fun onStart() {
        super.onStart()
        // Registrar o listener para novas mensagens
        messagesListener = firestoreManager.firestore.collection("messages")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    // Tratar o erro, se necessário
                    return@addSnapshotListener
                }

                // Limpar a lista de mensagens
                messagesList.clear()

                // Adicionar as novas mensagens à lista
                querySnapshot?.documents?.forEach { document ->
                    val message = document.toObject(Message::class.java)
                    if (message != null && (message.addressee == service.username || message.sender == service.username)) {
                        messagesList.add(message)
                    }
                }

                // Notificar o Adapter sobre as mudanças na lista de mensagens
                adapter.notifyDataSetChanged()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatEsperaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnEnviar = binding.btnEnviarChatEspera
        val editMessage = binding.editMessageChatEspera

        service = intent.getSerializableExtra("service") as Service

        binding.tvinfoUsername.text = service.username.toString()

        // Configurar o RecyclerView e o Adapter
        val recyclerViewMessages = binding.recyclerViewChatEspera
        adapter = MyAdapter(messagesList)
        recyclerViewMessages.adapter = adapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this@ChatEsperaActivity)

        // Carregar todas as mensagens do usuário service.username
        lifecycleScope.launch {
            service.username?.let { loadAllMessages(it) }

            btnEnviar.setOnClickListener {
                lifecycleScope.launch {
                    val specialty =
                        service.username?.let { it1 -> loginRepository.recoverUserFirebase(it1)?.specialty.toString() }
                    val message = editMessage.text.toString()
                    if (specialty != null) {
                        sendMessage(specialty, message)
                    }
                    editMessage.text.clear()
                }
            }

        }
        scrollFollowTheKeyboard(editMessage)
    }


    private fun sendMessage(specialty: String, message: String) {
        val collectionRef = firestoreManager.firestore.collection("messages")
        val username = userRepository.recoverUserCheckBox().email
        val timestamp = com.google.firebase.Timestamp.now()
        val patient = service.username

        val data = hashMapOf(
            "sender" to username,
            "username" to patient,
            "specialty" to specialty,
            "message" to message,
            "addressee" to patient,
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

    private fun scrollFollowTheKeyboard(editMessage: EditText) {
        // Adicionar ouvinte para acompanhar o scroll quando o teclado for aberto
        val recyclerView = binding.recyclerViewChatEspera
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private val r = Rect()
            private var wasOpened = false

            override fun onGlobalLayout() {
                recyclerView.getWindowVisibleDisplayFrame(r)
                val screenHeight = recyclerView.rootView.height

                // Calcula a diferença de altura entre o teclado e a tela
                val keypadHeight = screenHeight - r.bottom

                // Se o teclado estiver aberto, role para a posição mais recente
                if (keypadHeight > screenHeight * 0.15) {
                    if (!wasOpened) {
                        recyclerView.postDelayed({
                            recyclerView.scrollToPosition(adapter.itemCount - 1)
                        }, 100)
                    }
                    wasOpened = true
                } else {
                    wasOpened = false
                }
            }
        })
    }

    private fun loadAllMessages(username: String) {
        firestoreManager.firestore.collection("messages")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    // Tratar o erro, se necessário
                    return@addSnapshotListener
                }

                // Limpar a lista de mensagens
                messagesList.clear()

                // Adicionar as mensagens do usuário à lista
                querySnapshot?.documents?.forEach { document ->
                    val message = document.toObject(Message::class.java)
                    if (message != null && (message.addressee == username || message.sender == username)) {
                        messagesList.add(message)
                    }
                }

                // Notificar o Adapter sobre as mudanças na lista de mensagens
                adapter.notifyDataSetChanged()

                // Rolagem para a posição mais recente
                binding.recyclerViewChatEspera.scrollToPosition(adapter.itemCount - 1)
            }
    }
}

