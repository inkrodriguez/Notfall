package com.inkrodriguez.notfall.data

import java.util.*

class Message(
    var username: String = "",
    var sender: String = "",
    var addressee: String = "",
    var message: String? = "",
    var specialty: String? = "",
    var date: Date?,
    var attendant: String = ""
) {
    // Construtor sem argumentos necessário para a desserialização do Firestore
    constructor() : this("", "", "", null, null, null, "")
}

