package com.inkrodriguez.notfall.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inkrodriguez.notfall.adapter.AdapterFila
import com.google.firebase.firestore.ListenerRegistration
import com.inkrodriguez.notfall.data.*
import com.inkrodriguez.notfall.databinding.ActivityAtendimentosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AtendimentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAtendimentosBinding
    private val firestoreManager = FirestoreManager()
    private val servicesListOnHold: MutableList<Service> = mutableListOf()
    private val servicesLoading: MutableList<Service> = mutableListOf()
    private lateinit var adapter_servicesListOnHold: AdapterFila
    private lateinit var adapter_servicesLoading: AdapterFila
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
        binding = ActivityAtendimentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewFila = binding.recyclerViewFilaEmEspera
        val recyclerViewFilaAtendidos = binding.recyclerViewFilaAtendidos

        adapter_servicesListOnHold = AdapterFila(servicesListOnHold)
        recyclerViewFila.adapter = adapter_servicesListOnHold
        recyclerViewFila.layoutManager = LinearLayoutManager(this)

        adapter_servicesLoading = AdapterFila(servicesLoading)
        recyclerViewFilaAtendidos.adapter = adapter_servicesLoading
        recyclerViewFilaAtendidos.layoutManager = LinearLayoutManager(this)

        // Registrar os listeners para atualizar em tempo real
        lifecycleScope.launch {
            registerServicesListOnHoldSnapshotListener()
            registerServicesLoadingSnapshotListener()

        }
    }

    private suspend fun registerServicesListOnHoldSnapshotListener() {
        val userRepository = UserRepository(this)
        val loginRepository = LoginRepository(this)
        val username = userRepository.recoverUserCheckBox().email
        val mySpecialty = loginRepository.recoverUserFirebase(username)?.specialty
        val serviceCollectionRef = firestoreManager.firestore.collection("service")
            .whereEqualTo("specialty", mySpecialty)
            .whereEqualTo("status", "espera")
        onHoldSnapshotListener = serviceCollectionRef
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Lidar com o erro
                    return@addSnapshotListener
                }

                val services = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Service::class.java)
                } ?: emptyList()

                servicesListOnHold.clear()
                servicesListOnHold.addAll(services)

                adapter_servicesListOnHold.notifyDataSetChanged()
            }
    }

    private suspend fun registerServicesLoadingSnapshotListener() {
        val userRepository = UserRepository(this)
        val loginRepository = LoginRepository(this)
        val username = userRepository.recoverUserCheckBox().email
        val mySpecialty = loginRepository.recoverUserFirebase(username)?.specialty
        val serviceCollectionRef = firestoreManager.firestore.collection("service")
            .whereEqualTo("specialty", mySpecialty)
            .whereEqualTo("status", "atendido")
        loadingSnapshotListener = serviceCollectionRef
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Lidar com o erro
                    return@addSnapshotListener
                }

                val services = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Service::class.java)
                } ?: emptyList()

                servicesLoading.clear()
                servicesLoading.addAll(services)

                adapter_servicesLoading.notifyDataSetChanged()
            }
    }
}
