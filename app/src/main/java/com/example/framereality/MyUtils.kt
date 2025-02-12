package com.example.framereality

import android.content.Context
import android.icu.text.DateFormat
import java.util.Calendar
import android.os.Message
import android.widget.Toast
import com.google.firebase.Timestamp

object MyUtils {

    const val USER_TYPE_GOOGLE = "Google"
    const val USER_TYPE_EMAIL = "Email"
    const val USER_TYPE_PHONE = "Phone"

    fun toast(context: Context,message: String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    fun timestamp(): Long{
        return System.currentTimeMillis()
    }

    fun formatTimestampDate(timestamp: Long) : String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return android.text.format.DateFormat.format("dd/MM/yyyy",calendar).toString()
    }
}