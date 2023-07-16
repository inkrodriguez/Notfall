package com.inkrodriguez.notfall.data

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity

class Utilities(context: Context) {

    var context = context

    fun iniciarActivity(activity: Class<*>, value: String = "") {
        val intent = Intent(context, activity).putExtra("value", value)
        context.startActivity(intent)
    }

    fun iniciarDialogo(title: String, message: String, btnPositive: String, btnPositiveMessage: String = "",
                       btnNegative: String, btnNegativeMessage: String = ""){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(btnPositive) { dialog: DialogInterface, which: Int ->
            // Lógica a ser executada ao confirmar a ação
            Toast.makeText(context, btnPositiveMessage, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton(btnNegative) { dialog: DialogInterface, which: Int ->
            // Lógica a ser executada ao cancelar a ação
            Toast.makeText(context, btnNegativeMessage, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

}