package com.inkrodriguez.notfall.data

import com.google.firebase.Timestamp

class Consulta {
    var email: String? = ""
    var specialty: String? = ""
    var status: String? = ""
    var date: Timestamp? = null
    var doctor: String? = ""


    constructor() {
        // Construtor sem argumentos necessário para a desserialização do Firestore
    }

    constructor(email: String?, specialty: String?, status: String?, date: Timestamp?, doctor: String?) {
        this.email = email
        this.specialty = specialty
        this.status = status
        this.date = date
        this.doctor = doctor
    }
}
