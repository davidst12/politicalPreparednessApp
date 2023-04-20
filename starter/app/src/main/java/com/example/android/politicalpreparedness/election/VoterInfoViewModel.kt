package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.repository.Repository
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val repository: Repository) : ViewModel() {

    companion object {
        private val TAG = VoterInfoViewModel::class.java.simpleName
    }

    // Live data to hold voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse> = _voterInfo

    // Live data for election status
    private val _electionIsSaved = MutableLiveData<Boolean>()
    val electionIsSaved: LiveData<Boolean> = _electionIsSaved

    // Live data for navigation
    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    // Live data for exception
    private val _exception = MutableLiveData<Exception>()
    val exception: LiveData<Exception> = _exception

    // Hold electionId (useful when there are no internet connection)
    var electionId = MutableLiveData<Int>()

    init {
        _navigateBack.value = false
    }

    // Get voter info
    fun getVoterInfo(electionDivision: Division){
        viewModelScope.launch {
            val address = if(electionDivision.state.isEmpty()){
                "DC "+electionDivision.country
            }else{
                electionDivision.state+" "+electionDivision.country
            }
            try{
                _voterInfo.value = repository.getVoterInfo(address, electionId.value!!)
            }catch (e: java.lang.Exception){
                _exception.value = e
                Log.e(TAG, "Exception while fetching elections info. Exception: $e")
            }
        }
    }

    // Save or remove elections to local database
    fun onButtonClicked() {
        viewModelScope.launch {
            try{
                if(_electionIsSaved.value == true){
                    repository.deleteElectionById(electionId.value!!)
                }else{
                    repository.saveElection(_voterInfo.value!!.election)
                }
                _navigateBack.value = true
            }catch (e: java.lang.Exception){
                _exception.value = e
                Log.e(TAG, "Exception while saving/removing an election. Exception: $e")
            }
        }
    }

    // Check if the election is currently in the database
    fun checkIfElectionIsSaved(){
        viewModelScope.launch {
            val election = repository.getSavedElectionById(electionId.value!!)
            _electionIsSaved.value = election != null
        }
    }

}