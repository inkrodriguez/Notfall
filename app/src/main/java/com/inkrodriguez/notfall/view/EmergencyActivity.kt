package com.inkrodriguez.notfall.view
import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.*
import com.inkrodriguez.notfall.databinding.ActivityEmergencyBinding

class EmergencyActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var binding: ActivityEmergencyBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation: LatLng
    private val userRepository: UserRepository by lazy { UserRepository(this) }
    private val loginRepository: LoginRepository by lazy { LoginRepository(this) }
    private val firestoreManager: FirestoreManager = FirestoreManager()

    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment_emergency) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val utilities = Utilities(this)

        val intent = intent.extras

        val tvCausaSelecionada = binding.tvCausaSelecionada

        tvCausaSelecionada.text = intent?.getString("value")



        binding.btnConfirmarLocalizacao.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmar Ação")
            builder.setMessage("Você confirma que está em estado de URGÊNCIA?")
            builder.setPositiveButton("Confirmar") { dialog: DialogInterface, which: Int ->
                // Lógica a ser executada ao confirmar a ação
                Toast.makeText(this, "Aguarde, uma ambulância será enviada até o local!", Toast.LENGTH_LONG).show()

                lifecycleScope.launchWhenCreated {
                    registerEmergency()
                }
                dialog.dismiss()
                finish()
            }
            builder.setNegativeButton("Cancelar") { dialog: DialogInterface, which: Int ->
                // Lógica a ser executada ao cancelar a ação
                Toast.makeText(this, "Cancelado!", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Habilitar o botão de localização
        googleMap.isMyLocationEnabled = true

        // Adicionar o listener de clique no mapa
        googleMap.setOnMapClickListener(this)

        // Obter a localização atual
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    val markerOptions = MarkerOptions()
                        .position(currentLocation)
                        .title("Localização Atual")
                    googleMap.addMarker(markerOptions)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }
            }
        }
    }

    override fun onMapClick(latLng: LatLng) {
        // Atualizar a localização atual com a nova localização selecionada pelo usuário
        currentLocation = latLng

        // Limpar o marcador existente e adicionar um novo marcador com a nova localização
        googleMap.clear()
        val markerOptions = MarkerOptions()
            .position(currentLocation)
            .title("Nova Localização")
        googleMap.addMarker(markerOptions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(googleMap)
            }
        }
    }

    private fun registerEmergency() {
        val username = userRepository.recoverUserCheckBox().email
        val timestamp = com.google.firebase.Timestamp.now()
        val localizacao = Location(currentLocation.latitude, currentLocation.longitude)
        val collectionRef = firestoreManager.firestore.collection("emergency")
        val data = hashMapOf(
            "username" to username,
            "date" to timestamp,
            "status" to "espera",
            "localizacao" to localizacao
        )

        collectionRef.document(username).set(data)
            .addOnSuccessListener {
                Log.d("EMERGENCY", "Registrado no banco com sucesso!")
            }
            .addOnFailureListener { e ->
                Log.e("EMERGENCY", "Erro ao registrar no banco: ${e.message}", e)
            }
    }



    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}