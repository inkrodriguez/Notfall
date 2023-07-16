package com.inkrodriguez.notfall.data

import java.io.Serializable
import java.util.Date

class Service(
    var date: Date?,
    var status: String? = "",
    var username: String? = "",
    var specialty: String? = "",
    var attendant: String? = ""
) : Serializable {

    constructor() : this(null, "", "", "", "")

}
