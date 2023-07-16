package com.inkrodriguez.notfall.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.inkrodriguez.notfall.adapter.AdapterConsultas
import com.inkrodriguez.notfall.data.Consulta
import com.inkrodriguez.notfall.data.FirestoreManager
import com.inkrodriguez.notfall.data.Service
import com.inkrodriguez.notfall.databinding.ActivityConsultaBinding

class ConsultaActivity : AppCompatActivity() {

    private val firestoreManager = FirestoreManager()
    private val listConsultas: MutableList<Consulta> = mutableListOf()
    private lateinit var binding: ActivityConsultaBinding
    private lateinit var adapter_consultas: AdapterConsultas
    private lateinit var loadingSnapshotListener: ListenerRegistration

    override fun onDestroy() {
        super.onDestroy()
        // Remover os listeners quando a atividade for destruída
        loadingSnapshotListener.remove()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewFilaConsultas = binding.recyclerViewFilaDeConsultasMedicos

        adapter_consultas = AdapterConsultas(listConsultas)
        recyclerViewFilaConsultas.adapter = adapter_consultas
        recyclerViewFilaConsultas.layoutManager = LinearLayoutManager(this)

        // Registrar os listeners para atualizar em tempo real
        findConsultas()

    }

    private fun findConsultas() {
        val serviceCollectionRef = firestoreManager.firestore.collection("queries")
        loadingSnapshotListener = serviceCollectionRef
            .whereEqualTo("status", "espera")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Lidar com o erro
                    return@addSnapshotListener
                }

                // Limpar a lista antes de adicionar os novos dados
                listConsultas.clear()

                // Iterar pelos documentos do snapshot e converter para objetos Consulta
                val consultas = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Consulta::class.java)
                } ?: emptyList()

                // Adicionar as consultas à lista
                listConsultas.addAll(consultas)

                Log.d("AdapterConsultas", "Tamanho da lista: ${listConsultas.size}") // Adicione este log

                // Notificar o adapter sobre as mudanças
                adapter_consultas.notifyDataSetChanged()

            }
    }

}