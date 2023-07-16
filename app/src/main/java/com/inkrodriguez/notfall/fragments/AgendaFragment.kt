package com.inkrodriguez.notfall.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.CustomEvent
import com.inkrodriguez.notfall.data.FirestoreManager
import com.inkrodriguez.notfall.data.UserRepository
import com.inkrodriguez.notfall.data.Utilities
import io.grpc.okhttp.internal.Util
import java.text.SimpleDateFormat
import java.util.*


class AgendaFragment : Fragment() {

    private val eventDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_agenda, container, false)
        val utilities = Utilities(this.requireContext())

        val monthTextView = view.findViewById<TextView>(R.id.monthTextView)
        val currentDate = Calendar.getInstance().time
        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val currentMonthText = monthFormat.format(currentDate).capitalize()
        monthTextView.text = currentMonthText

        val btnAgendarConsulta = view.findViewById<Button>(R.id.btnAgendarConsulta)
        btnAgendarConsulta.setOnClickListener {
            startNewQuery(utilities)
        }

        val compactCalendarView = view.findViewById<CompactCalendarView>(R.id.compactcalendar_view)
        val eventListView = view.findViewById<ListView>(R.id.event_listview)

        val listStatus = listOf("agendado", "encerrado")

        val firestoreManager = FirestoreManager()
        val db = firestoreManager.firestore
        val collectionRef = db.collection("queries").whereIn("status", listStatus)

        val customEvents = mutableListOf<CustomEvent>()

        collectionRef.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val email = document.getString("email")
                val specialty = document.getString("specialty")
                val status = document.getString("status")
                val date = document.getTimestamp("date")?.toDate()

                if (email != null && specialty != null && status != null && date != null) {
                    val customEvent = CustomEvent(date, email, specialty, status)
                    customEvents.add(customEvent)
                }
            }

            val events = customEvents.map { customEvent ->
                Event(Color.BLACK, customEvent.date.time, customEvent)
            }

            compactCalendarView.removeAllEvents()
            compactCalendarView.addEvents(events)

            compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
                override fun onDayClick(dateClicked: Date) {
                    val selectedDate = eventDateFormat.format(dateClicked)

                    val filteredEvents = customEvents.filter { customEvent ->
                        val eventDate = eventDateFormat.format(customEvent.date)
                        eventDate == selectedDate
                    }

                    val eventInfo = filteredEvents.map { customEvent ->
                        val eventDateTime = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(customEvent.date)
                        "Paciente: ${customEvent.email}\nData: ${eventDateTime}h\nEspecialista: ${customEvent.specialty}"
                    }


                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        eventInfo
                    )

                    eventListView.adapter = adapter
                }

                override fun onMonthScroll(firstDayOfNewMonth: Date) {
                    // Adicione a lógica de rolagem do mês aqui
                }
            })
        }

        return view
    }

    private fun startNewQuery(utilities: Utilities) {
        val options = arrayOf(
            "Clínico Geral",
            "Cardiologista",
            "Ortopedista",
            "Ginecologista",
            "Dermatologista",
            "Oftalmologista",
            "Pediatra",
            "Psiquiatra",
            "Endocrinologista ",
            "Gastroenterologista"
        )

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecione o Especialista:")

        builder.setItems(options) { dialog, which ->
            val selectedOption = options[which]
            showConfirmationDialog(selectedOption)
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }

        builder.setPositiveButton(null, null) // Remover o botão de confirmação

        val dialog = builder.create()
        dialog.show()
    }

    private fun showConfirmationDialog(selectedOption: String) {
        val userRepository = UserRepository(requireContext())
        val recoverUser = userRepository.recoverUserCheckBox()
        val username = recoverUser.email

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Confirmação")
        alertDialogBuilder.setMessage("Você deseja marcar uma consulta?\nVocê selecionou: $selectedOption")
        alertDialogBuilder.setPositiveButton("Sim") { dialog, which ->
            createQuery(username, selectedOption)
        }
        alertDialogBuilder.setNegativeButton("Não") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun createQuery(username: String, selectedOption: String) {
        val firestoreManager = FirestoreManager()
        val db = firestoreManager.firestore
        val timestamp = com.google.firebase.Timestamp.now()

        val data = hashMapOf(
            "email" to username,
            "specialty" to selectedOption,
            "status" to "espera",
            "date" to timestamp
        )

        db.collection("queries").add(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Sua solicitação foi enviada, aguarde!", Toast.LENGTH_SHORT).show()
            }
    }
}