package com.inkrodriguez.notfall.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.Service
import com.inkrodriguez.notfall.view.ChatEsperaActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.inkrodriguez.notfall.data.LoginRepository
import com.inkrodriguez.notfall.data.UserRepository

class AdapterFila(private val serviceList: MutableList<Service>) : RecyclerView.Adapter<AdapterFila.ViewHolder>() {

    private lateinit var snapshotListener: ListenerRegistration

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fila_users, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = serviceList[position]
        holder.bind(service)
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        // Registrar o Firestore Snapshot Listener quando o Adapter estiver anexado ao RecyclerView
        registerSnapshotListener()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        // Remover o Firestore Snapshot Listener quando o Adapter for removido do RecyclerView
        unregisterSnapshotListener()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val btnAtender: Button = itemView.findViewById(R.id.btnExibirDados)

        init {
            btnAtender.setOnClickListener {
                val context = itemView.context
                val service = serviceList[adapterPosition]

                // Atualizar o status do serviço para "atendido"
                updateServiceStatus(service, context)

                // Abrir a atividade de chat
                val intent = Intent(context, ChatEsperaActivity::class.java).putExtra("service", service)
                context.startActivity(intent)
            }
        }

        fun bind(service: Service) {
            // Atualize as views do item do RecyclerView com os dados do serviço
            tvUsername.text = service.username
        }

        private fun updateServiceStatus(service: Service, context: Context) {
            val firestore = FirebaseFirestore.getInstance()
            val serviceCollectionRef = firestore.collection("service")
            val userRepository = UserRepository(context)

            // Consulte os documentos que correspondem ao nome de usuário
            serviceCollectionRef
                .whereEqualTo("username", service.username)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val updates = hashMapOf<String, Any>(
                        "status" to "atendido",
                        "attendant" to userRepository.recoverUserCheckBox().email
                    )

                    // Atualize o status para cada documento retornado na consulta
                    querySnapshot.documents.forEach { document ->
                        document.reference.update(updates)
                            .addOnSuccessListener {
                                // Sucesso ao atualizar o status do serviço
                            }
                            .addOnFailureListener { e ->
                                // Erro ao atualizar o status do serviço
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Erro ao consultar os documentos
                }
        }
    }

    private fun registerSnapshotListener() {
        val firestore = FirebaseFirestore.getInstance()
        val serviceCollectionRef = firestore.collection("service")

        snapshotListener = serviceCollectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Lidar com o erro
                return@addSnapshotListener
            }

            val services = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Service::class.java)
            } ?: emptyList()

            serviceList.clear()
            serviceList.addAll(services)

            notifyDataSetChanged()
        }
    }

    private fun unregisterSnapshotListener() {
        snapshotListener.remove()
    }
}
