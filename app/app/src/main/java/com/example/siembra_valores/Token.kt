package com.example.siembra_valores

import android.content.Context

fun saveToken(context: Context, token: String) {
  val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
  val editor = sharedPreferences.edit()
  editor.putString("token", token)
  editor.apply()
}

fun getToken(context: Context): String? {
  val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
  return sharedPreferences.getString("token", null)
}