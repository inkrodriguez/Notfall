package com.inkrodriguez.notfall.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.Consulta
import com.inkrodriguez.notfall.data.LoginRepository
import com.inkrodriguez.notfall.view.ChatEsperaActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterMinhasConsultas(private val consultaList: MutableList<Consulta>) : RecyclerView.Adapter<AdapterMinhasConsultas.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.minhas_consultas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val consulta = consultaList[position]
        holder.bind(consulta)
    }

    override fun getItemCount(): Int {
        return consultaList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInfo: TextView = itemView.findViewById(R.id.tvInfo)
        private val tvSpecialty: TextView = itemView.findViewById(R.id.tvSpecialty)
        private val btnExibir: Button = itemView.findViewById(R.id.btnExibirInfoMinhasConsultas)

        fun bind(consulta: Consulta) {
            // Atualizar as views do item do RecyclerView com os dados da consulta
            tvInfo.text = consulta.status
            tvSpecialty.text = consulta.specialty


            val yellow = ContextCompat.getColor(itemView.context, R.color.yellow)
            val red = ContextCompat.getColor(itemView.context, R.color.red)
            val green = ContextCompat.getColor(itemView.context, R.color.green)

            when (consulta.status) {
                "espera" -> {
                    btnExibir.visibility = View.GONE
                    tvInfo.setTextColor(yellow)
                }

                "agendado" -> {
                    tvInfo.setTextColor(green)
                    btnExibir.setBackgroundColor(green)
                    btnExibir.visibility = View.VISIBLE
                }

                else -> {
                    tvInfo.setTextColor(red)
                    btnExibir.setBackgroundColor(red)
                    btnExibir.visibility = View.VISIBLE
                }
            }

            btnExibir.setOnClickListener {
                showConsultaDialog(consulta)
            }
        }

            private fun showConsultaDialog(consulta: Consulta) {
                val context: Context = itemView.context
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDate = consulta.date?.toDate()?.let { dateFormat.format(it) }

                // Crie um AlertDialog com as informações da consulta
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder.setTitle("Informações da Consulta")
                dialogBuilder.setMessage("Especialidade: ${consulta.specialty}\nStatus: ${consulta.status}\nData: $formattedDate")

                // Defina o botão "Fechar" do diálogo
                dialogBuilder.setPositiveButton("Fechar") { dialog, _ ->
                    dialog.dismiss()
                }

                // Crie e exiba o diálogo
                val dialog = dialogBuilder.create()
                dialog.show()
            }
        }
    }
