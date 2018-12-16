package com.example.viratkumar.testproject

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Context.log(tag: String, message: String) {
    Log.d(tag, message)
}

fun Context.toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}