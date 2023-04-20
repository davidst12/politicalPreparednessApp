package com.example.android.politicalpreparedness.util

import android.content.Context
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.repository.Repository

object ServiceLocator {

    private var database: ElectionDatabase? = null
    @Volatile
    var repository: Repository? = null

    // Provide the same repository to every viewModel
    fun provideRepository(context: Context): Repository{
        synchronized(this){
            return repository ?: createRepository(context)
        }
    }

    private fun createRepository(context: Context): Repository {
        val database = database ?: createDataBase(context)
        val newRepo = Repository(database.electionDao)
        repository = newRepo
        return newRepo

    }

    private fun createDataBase(context: Context): ElectionDatabase {
        return ElectionDatabase.getInstance(context)
    }

}