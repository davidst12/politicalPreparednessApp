package com.example.android.politicalpreparedness

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.util.ServiceLocator

class MainActivity : AppCompatActivity() {

    val repository: Repository
        get() = ServiceLocator.provideRepository(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
