    package com.inkrodriguez.notfall.adapter

    import android.annotation.SuppressLint
    import android.app.AlertDialog
    import android.content.ContentValues.TAG
    import android.content.Context
    import android.os.Build
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.Spinner
    import android.widget.TextView
    import android.widget.TimePicker
    import android.widget.Toast
    import androidx.annotation.RequiresApi
    import androidx.recyclerview.widget.RecyclerView
    import com.github.sundeepk.compactcalendarview.CompactCalendarView
    import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
    import com.github.sundeepk.compactcalendarview.domain.Event
    import com.google.firebase.Timestamp
    import com.inkrodriguez.notfall.R
    import com.inkrodriguez.notfall.data.Consulta
    import com.inkrodriguez.notfall.data.ConsultaRepository
    import com.inkrodriguez.notfall.data.UserRepository
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import java.util.Calendar
    import java.util.Date


    class AdapterConsultas(private val consultaList: MutableList<Consulta>) : RecyclerView.Adapter<AdapterConsultas.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fila_consultas, parent, false)
            return ViewHolder(view)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val consulta = consultaList[position]
            holder.bind(consulta)

        }

        override fun getItemCount(): Int {
            return consultaList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
            private val btnAgendar: Button = itemView.findViewById(R.id.btnAgendar)

            @RequiresApi(Build.VERSION_CODES.M)
            fun bind(consulta: Consulta) {

                val currentPosition = adapterPosition

                // Atualizar as views do item do RecyclerView com os dados da consulta
                tvUsername.text = consulta.email
                val email = consulta.email
                val doctor = consulta.doctor
                val date = consulta.date
                val specialty = consulta.specialty
                val status = consulta.status

                val consultaItem = Consulta(email, specialty, status, date, doctor)

                // Configurar o OnClickListener do botão "btnAgendar"
                btnAgendar.setOnClickListener {
                    showDialog(itemView.context, consulta, consultaItem, currentPosition)
                }
            }

            @SuppressLint("MissingInflatedId")
            @RequiresApi(Build.VERSION_CODES.M)
            private fun showDialog(context: Context, consulta: Consulta, consultaItem: Consulta, currentPosition: Int) {
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_agendar_consulta_1, null)
                val dialogBuilder = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setTitle("Agendar Consulta")

                val alertDialog = dialogBuilder.show()

                val calendarView = dialogView.findViewById<CompactCalendarView>(R.id.calendarView)
                val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
                val spinner = dialogView.findViewById<Spinner>(R.id.spinner)
                val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

                //PEGA TODOS OS USUÁRIOS MÉDICOS E COLOCA DENTRO DO SPINNER.
                val listMedicals: MutableList<String> = mutableListOf()
                val userRepository = UserRepository(context)
                userRepository.getMedicalUsers { medicalUsers ->
                    // Aqui você pode usar a lista de usuários médicos retornada
                    for (medicalUser in medicalUsers) {
                        // Faça algo com cada usuário médico
                        medicalUser?.let {
                            val fullname = it
                            fullname?.let { name -> listMedicals.add(name) }
                        }
                    }

                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listMedicals)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
                ///


                calendarView.setListener(object : CompactCalendarViewListener {
                    override fun onDayClick(dateClicked: Date) {
                        val dateSelected = Timestamp(dateClicked)
                        val consultaRepository = ConsultaRepository()
                        val selectedDoctor = spinner.selectedItem.toString()

                        CoroutineScope(Dispatchers.Main).launch {
                            consulta.email?.let {

                                val objectConsulta = Consulta(
                                    consultaItem.email,
                                    consultaItem.specialty,
                                    consulta.status,
                                    dateSelected,
                                    selectedDoctor
                                )
                                consultaRepository.updateConsultation(objectConsulta, context)
                            }
                        }
                    }

                    override fun onMonthScroll(firstDayOfNewMonth: Date) {
                    }
                })

                btnConfirmar.setOnClickListener {
                    val selectedDoctor = spinner.selectedItem.toString()
                    val consultaRepository = ConsultaRepository()

                    val hour = timePicker.hour
                    val minute = timePicker.minute

                    // Obter a consulta original
                    CoroutineScope(Dispatchers.Main).launch {
                        val consulta = consultaItem.email?.let { it1 ->
                            consultaItem.specialty?.let { it2 ->
                                consultaRepository.getConsultationsByEmail(
                                    it1, it2
                                )
                            }
                        }

                        // Verificar se a consulta não é nula e se possui a data
                        if (consulta?.date != null) {
                            val originalTimestamp = consulta.date

                            // Converter o timestamp para uma data
                            val originalDate = Date((originalTimestamp?.seconds ?: 0) * 1000)

                            // Criar um objeto Calendar com a data original
                            val calendar = Calendar.getInstance()
                            calendar.time = originalDate

                            // Atualizar apenas a hora e o minuto
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)

                            // Criar um objeto Timestamp com a nova data e hora
                            val newTimestamp = Timestamp(calendar.timeInMillis / 1000, 0)

                            val objectConsulta = Consulta(
                                consulta.email,
                                consulta.specialty,
                                "agendado",
                                newTimestamp,
                                selectedDoctor
                            )
                            consultaRepository.updateConsultation(objectConsulta, context)

                            // Notificar o adaptador sobre as mudanças
                            consultaList[adapterPosition] = objectConsulta
                            Log.d("AdapterConsultas", "Item atualizado: $objectConsulta")
                            notifyItemChanged(adapterPosition)
                            consultaList.removeAt(position)
                            notifyDataSetChanged()

                        }
                    }
                    alertDialog.dismiss()
                }
            }
        }
    }
