package com.inkrodriguez.notfall.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.firestore.ListenerRegistration
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.adapter.AdapterFila
import com.inkrodriguez.notfall.data.FirestoreManager
import com.inkrodriguez.notfall.data.LoginRepository
import com.inkrodriguez.notfall.data.Service
import com.inkrodriguez.notfall.data.ServiceRepository
import com.inkrodriguez.notfall.databinding.ActivityAtendimentosBinding
import com.inkrodriguez.notfall.databinding.ActivityHomeBinding
import com.inkrodriguez.notfall.databinding.ActivityUrgencyBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UrgencyActivity : AppCompatActivity() {

    private val firestoreManager = FirestoreManager()
    private val servicesListOnHold: MutableList<Service> = mutableListOf()
    private val servicesLoading: MutableList<Service> = mutableListOf()
    private lateinit var binding: ActivityUrgencyBinding
    private lateinit var adapter_servicesListOnHold: AdapterUrgency
    private lateinit var adapter_servicesLoading: AdapterUrgency
    private lateinit var onHoldSnapshotListener: ListenerRegistration
    private lateinit var loadingSnapshotListener: ListenerRegistration

    override fun onDestroy() {
        super.onDestroy()
        // Remover os listeners quando a atividade for destruída
        onHoldSnapshotListener.remove()
        loadingSnapshotListener.remove()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrgencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewFila = binding.recyclerViewFilaEmEsperaUrgency
        val recyclerViewFilaAtendidos = binding.recyclerViewFilaAtendidosUrgency

        adapter_servicesListOnHold = AdapterUrgency(servicesListOnHold)
        recyclerViewFila.adapter = adapter_servicesListOnHold
        recyclerViewFila.layoutManager = LinearLayoutManager(this)

        adapter_servicesLoading = AdapterUrgency(servicesLoading)
        recyclerViewFilaAtendidos.adapter = adapter_servicesLoading
        recyclerViewFilaAtendidos.layoutManager = LinearLayoutManager(this)

        // Registrar os listeners para atualizar em tempo real
        registerServicesListOnHoldSnapshotListener()
        registerServicesLoadingSnapshotListener()

    }

    private fun registerServicesListOnHoldSnapshotListener() {
        val serviceCollectionRef = firestoreManager.firestore.collection("emergency")
        onHoldSnapshotListener = serviceCollectionRef
            .whereEqualTo("status", "espera")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Lidar com o erro
                    return@addSnapshotListener
                }

                // Limpar a lista antes de adicionar os novos dados
                servicesListOnHold.clear()

                // Iterar pelos documentos do snapshot e converter para objetos Service
                val emergencies = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Service::class.java)
                } ?: emptyList()

                // Adicionar os serviços à lista
                servicesListOnHold.addAll(emergencies)

                // Notificar o adapter sobre as mudanças
                adapter_servicesListOnHold.notifyDataSetChanged()
            }
    }

    private fun registerServicesLoadingSnapshotListener() {
        val serviceCollectionRef = firestoreManager.firestore.collection("emergency")
        loadingSnapshotListener = serviceCollectionRef
            .whereEqualTo("status", "atendido")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Lidar com o erro
                    return@addSnapshotListener
                }

                // Limpar a lista antes de adicionar os novos dados
                servicesLoading.clear()

                // Iterar pelos documentos do snapshot e converter para objetos Service
                val emergencies = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Service::class.java)
                } ?: emptyList()

                // Adicionar os serviços à lista
                servicesLoading.addAll(emergencies)

                // Notificar o adapter sobre as mudanças
                adapter_servicesLoading.notifyDataSetChanged()
            }
    }
}
