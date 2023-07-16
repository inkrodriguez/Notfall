package com.inkrodriguez.notfall.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.FirestoreManager
import com.inkrodriguez.notfall.data.Service
import java.text.SimpleDateFormat
import java.util.*
import android.location.Geocoder
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog

class AdapterUrgency(private val serviceList: List<Service>) : RecyclerView.Adapter<AdapterUrgency.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fila_urgency, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = serviceList[position]
        holder.bind(service)
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val btnExibir: Button = itemView.findViewById(R.id.btnExibirDados)
        private val geocoder: Geocoder = Geocoder(itemView.context, Locale.getDefault())

        init {
            btnExibir.setOnClickListener {
                val context = itemView.context
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val service = serviceList[position]
                    showBottomSheetDialog(context, service)
                }
            }
        }

        @SuppressLint("MissingInflatedId")
        private fun showBottomSheetDialog(context: Context, service: Service) {
            val bottomSheetDialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog, null)
            bottomSheetDialog.setContentView(view)

            val firestoreManager = FirestoreManager()
            val collectionRef = firestoreManager.firestore.collection("emergency")
            val documentRef = service.username?.let { collectionRef.document(it) }

            if (documentRef != null) {
                documentRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // O documento existe, você pode acessar os dados dele aqui
                            val data = document.data

                            val tvUsername: TextView = view.findViewById(R.id.tvUsername_BSD)
                            val tvStatus: TextView = view.findViewById(R.id.tvStatus_BSD)
                            val tvDate: TextView = view.findViewById(R.id.tvDate_BSD)
                            val tvAddress_BSD: TextView = view.findViewById(R.id.tvAdress_BSD)
                            val btnEnviarAmbulancia: Button = view.findViewById(R.id.btnEnviarAmbulancia)

                            btnEnviarAmbulancia.setOnClickListener {
                                Toast.makeText(
                                    context,
                                    "Você atendeu à este chamado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                bottomSheetDialog.dismiss()
                                val updatedStatus = "atendido" // Insira o novo status aqui

                                documentRef.update("status", updatedStatus)
                                    .addOnSuccessListener {
                                        // Atualização bem-sucedida
                                        Toast.makeText(
                                            context,
                                            "Status atualizado com sucesso!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        // Ocorreu um erro ao atualizar o status
                                        Toast.makeText(
                                            context,
                                            "Erro ao atualizar o status: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }

                            tvUsername.text = "Usuário: ${service.username}"
                            tvStatus.text = "Status: ${service.status}"

                            // Formate a data antes de exibi-la
                            val dateFormat =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val formattedDate = dateFormat.format(service.date as Date)
                            tvDate.text = "Data: $formattedDate"

                            val latitude = (data?.get("localizacao") as? Map<*, *>)?.get("latitude") as? Double
                            val longitude = (data?.get("localizacao") as? Map<*, *>)?.get("longitude") as? Double

                            if (latitude != null && longitude != null) {
                                // Obtenha o endereço a partir da latitude e longitude
                                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                                if (addresses != null) {
                                    if (addresses.isNotEmpty()) {
                                        val address = addresses[0]
                                        val fullAddress = address.getAddressLine(0)

                                        // Exiba o endereço no TextView
                                        tvAddress_BSD.text = "Endereço: $fullAddress"
                                    }
                                }
                            } else {
                                // Trate o caso em que os valores são nulos
                            }

                        }
                    }
                    .addOnFailureListener { exception ->
                        // Ocorreu um erro ao buscar o documento
                    }
            }

            bottomSheetDialog.show()
        }

        fun bind(service: Service) {
            // Atualize as views do item do RecyclerView com os dados do serviço
            tvUsername.text = service.username
        }

    }
}
