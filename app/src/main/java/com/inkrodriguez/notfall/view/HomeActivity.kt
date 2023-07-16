package com.inkrodriguez.notfall.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.*
import com.google.firebase.firestore.Query
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.adapter.AdapterFila
import com.inkrodriguez.notfall.adapter.MyAdapter
import com.inkrodriguez.notfall.data.*
import com.inkrodriguez.notfall.databinding.ActivityHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow

import org.w3c.dom.Text

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val serviceRepository = ServiceRepository()
    private val consultaRepository: ConsultaRepository by lazy { ConsultaRepository() }
    private val utilities: Utilities by lazy { Utilities(this) }
    private val userRepository: UserRepository by lazy { UserRepository(this) }
    private val loginRepository: LoginRepository by lazy { LoginRepository(this) }
    private var mapFragment: SupportMapFragment? = null


    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                HospitaisProximosActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        mapFragment = null

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tvUser = binding.tvUser

        val btnChat = binding.btnChat
        val btnHospitaisProximos = binding.btnHospitaisProximos
        val btnPrimeirosSocorros = binding.btnPrimeirosSocorros
        val btnEmergency = binding.btnEmergency
        val btnPerguntasFrequentes = binding.btnPerguntasFrequentes
        val btnAgenda = binding.btnAgenda
        val loginRepository = LoginRepository(this) // Inicialize o LoginRepository aqui


        val cor = resources.getColor(R.color.standard)
        val linearLayoutPaciente = binding.linearLayoutPaciente
        val email = userRepository.recoverUserCheckBox().email

        tvUser.setTextColor(cor)
        tvUser.text = email


        lifecycleScope.launch(Dispatchers.IO) {
            val category = userRepository.getUser(email)?.category
            val user = category?.let { User(email = email, category = it) }

            runOnUiThread {
                if (user != null) {
                    if (user.category == "medico" || user.category == "assistente") {
                        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val layoutMedico = layoutInflater.inflate(R.layout.layout_home_medico, null)
                        binding.frameLayoutContainer.addView(layoutMedico)
                        linearLayoutPaciente.visibility = View.INVISIBLE

                        val btnAceitarAtendimentos = layoutMedico.findViewById<Button>(R.id.btnAceitarAtendimentos)
                        val btnUrgencia = layoutMedico.findViewById<Button>(R.id.btnUrgencia)
                        val btnConsultas = layoutMedico.findViewById<Button>(R.id.btnConsultas)

                        //CONSULTAS
                        lifecycleScope.launch {
                            consultaRepository.observeConsultasAndSize().collect { (consultas, count) ->
                                val tvInfoConsultas = layoutMedico.findViewById<TextView>(R.id.tvInfoConsultas)
                                tvInfoConsultas.text = count.toString()

                                btnConsultas.setOnClickListener {
                                    if (count > 0) {
                                        utilities.iniciarActivity(ConsultaActivity::class.java)
                                    } else {
                                        Toast.makeText(this@HomeActivity, "Não existem consultas ainda.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }


                        //URGÊNCIA
                        lifecycleScope.launch {
                            serviceRepository.observeSizeEmergency().collect { count ->
                                val tvInfoUrgency = layoutMedico.findViewById<TextView>(R.id.tvInfoUrgency)
                                tvInfoUrgency.text = count.toString()

                                btnUrgencia.setOnClickListener {
                                    if(count > 0) {
                                        utilities.iniciarActivity(UrgencyActivity::class.java)
                                    } else {
                                        runOnUiThread {
                                            Toast.makeText(this@HomeActivity, "Não existem chamados ainda.", Toast.LENGTH_SHORT).show()
                                        }}
                                }
                            }
                        }

                        //ATENDIMENTOS
                        lifecycleScope.launch {
                            val mySpecialty = loginRepository.recoverUserFirebase(email)?.specialty
                            if (mySpecialty != null) {
                                serviceRepository.observeSizeServices(mySpecialty).collect { count ->
                                    val tvSizeServices = layoutMedico.findViewById<TextView>(R.id.tvSizeServices)
                                    tvSizeServices.text = count.toString()

                                    btnAceitarAtendimentos.setOnClickListener {
                                        if(count > 0) {
                                            utilities.iniciarActivity(AtendimentosActivity::class.java)
                                        } else {
                                            runOnUiThread {
                                                Toast.makeText(
                                                    this@HomeActivity,
                                                    "Não existem chamados ainda.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }

                                }
                            }
                        }

                    }
                }
            }
        }

        btnAgenda.setOnClickListener {
            utilities.iniciarActivity(AgendaActivity::class.java)
        }


        btnChat.setOnClickListener {
            startEmergencyDiagnosticOrHospital(utilities)
        }

        btnEmergency.setOnClickListener {
            startEmergencyOneStep(utilities)
        }

        btnPrimeirosSocorros.setOnClickListener {
            utilities.iniciarActivity(PrimeirosSocorrosActivity::class.java)
        }

        btnPerguntasFrequentes.setOnClickListener {
            utilities.iniciarActivity(PerguntasFrequentesActivity::class.java)
        }

        btnHospitaisProximos.setOnClickListener {
            utilities.iniciarActivity(HospitaisProximosActivity::class.java)
        }

    }

    private fun startEmergencyDiagnosticOrHospital(utilities: Utilities) {
        val options = arrayOf("Diagnóstico Virtual", "Hospital mais próximo")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione uma opção:")

        builder.setItems(options) { dialog, which ->
            val selectedOption = options[which]

            if(selectedOption == options[0]){
                startChat(utilities)
            } else {
                utilities.iniciarActivity(HospitaisProximosActivity::class.java)
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun startEmergencyOneStep(utilities: Utilities) {
        val options = arrayOf("Cardíaca", "Respiratória", "Cerebral", "Acidente", "Outro")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione a causa:")

        builder.setItems(options) { dialog, which ->
            val selectedOption = options[which]
            utilities.iniciarActivity(EmergencyActivity::class.java, selectedOption)
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }

        builder.setPositiveButton(null, null) // Remover o botão de confirmação

        val dialog = builder.create()
        dialog.show()
    }

    private fun startChat(utilities: Utilities) {
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

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione o Especialista:")

        builder.setItems(options) { dialog, which ->
            val selectedOption = options[which]
            utilities.iniciarActivity(ChatActivity::class.java, selectedOption)
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }

        builder.setPositiveButton(null, null) // Remover o botão de confirmação

        val dialog = builder.create()
        dialog.show()
    }


}