package com.example.android.politicalpreparedness.repository

import android.util.Log
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsHttpClient.Companion.API_KEY
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.*

class Repository constructor(
    private val dao: ElectionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
){

    companion object {
        private val TAG = Repository::class.java.simpleName
    }

    // Local
    // =============
    suspend fun getSavedElections(): List<Election> = withContext(ioDispatcher) {
        return@withContext dao.getElections()
    }

    suspend fun getSavedElectionById(electionId: Int): Election? = withContext(ioDispatcher) {
        return@withContext dao.getElectionById(electionId)
    }

    suspend fun saveElection(election: Election) = withContext(ioDispatcher) {
        dao.saveElection(election)
    }

    suspend fun deleteElectionById(electionId: Int): Int = withContext(ioDispatcher) {
        return@withContext dao.deleteElectionById(electionId)
    }

    // Remote
    // =============
    private lateinit var remoteElectionList: List<Election>

    suspend fun getRemoteElections(): List<Election> {
        withContext(Dispatchers.IO){
            remoteElectionList = CivicsApi.retrofitService.getElections(API_KEY).elections
            Log.i(TAG, "Fetching elections: $remoteElectionList")
        }
        return remoteElectionList
    }

    private lateinit var voterInfo: VoterInfoResponse

    suspend fun getVoterInfo(address: String, electionId: Int): VoterInfoResponse {
        withContext(Dispatchers.IO){
            voterInfo = CivicsApi.retrofitService.getVoterInfo(API_KEY, address, electionId)
            Log.i(TAG, "Fetching elections: $voterInfo")
        }
        return voterInfo
    }

    private lateinit var representatives: List<Representative>

    suspend fun getRepresentatives(address: String): List<Representative>{
        withContext(Dispatchers.IO) {
            val (offices, officials) = CivicsApi.retrofitService.getRepresentatives(
                API_KEY,
                address
            )
            representatives = offices.flatMap { office -> office.getRepresentatives(officials) }
            Log.i(TAG, "Representatives response: $representatives")
        }
        return representatives
    }

}