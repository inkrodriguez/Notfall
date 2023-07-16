package com.inkrodriguez.notfall.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.adapter.AdapterConsultas
import com.inkrodriguez.notfall.adapter.AdapterMinhasConsultas
import com.inkrodriguez.notfall.data.Consulta
import com.inkrodriguez.notfall.data.ConsultaRepository
import com.inkrodriguez.notfall.data.LoginRepository
import com.inkrodriguez.notfall.data.Service
import com.inkrodriguez.notfall.data.User
import com.inkrodriguez.notfall.data.UserRepository
import kotlinx.coroutines.launch

class ConsultaFragment : Fragment() {

    private lateinit var adapter: AdapterMinhasConsultas
    private val consultaRepository: ConsultaRepository by lazy { ConsultaRepository() }
    private val userRepository: UserRepository by lazy { UserRepository(this.requireContext()) }
    private val consultaList: MutableList<Consulta> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_consulta, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMinhasConsultas)

        adapter = AdapterMinhasConsultas(consultaList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observar as alterações no LiveData das consultas
        val recoverUser = userRepository.recoverUserCheckBox()
        val email = recoverUser.email

        consultaRepository.getAllConsultationsForUser(email).observe(viewLifecycleOwner) { consultas ->
            // Atualizar a lista de consultas no adapter
            consultaList.clear()
            consultaList.addAll(consultas)
            adapter.notifyDataSetChanged()
        }

        return view
    }

}

