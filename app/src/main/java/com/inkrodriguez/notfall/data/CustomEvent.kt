package com.inkrodriguez.notfall.data

import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.Timestamp
import java.util.Date

class CustomEvent(
    val date: Date,
    val email: String,
    val specialty: String,
    val status: String,
    color: Int = 0,
    timeInMillis: Long = 0
) : Event(color, timeInMillis) {
    // Adicione as propriedades extras e métodos que você precisa aqui
}
