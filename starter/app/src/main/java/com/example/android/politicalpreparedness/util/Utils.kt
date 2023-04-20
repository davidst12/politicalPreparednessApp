package com.example.android.politicalpreparedness.util

import android.content.Context
import android.net.ConnectivityManager

object Utils {
    fun checkInternetConnection(context: Context): Boolean{
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            return true
        }
        return false
    }
}