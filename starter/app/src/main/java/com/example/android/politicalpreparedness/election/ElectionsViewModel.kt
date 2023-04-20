package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.Repository
import kotlinx.coroutines.launch

class ElectionsViewModel(private val repository: Repository): ViewModel() {

    companion object {
        private val TAG = ElectionsViewModel::class.java.simpleName
    }

    // Live data for saved elections
    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>> = _savedElections

    // Live data for remote elections
    private val _remoteElections = MutableLiveData<List<Election>>()
    val remoteElections: LiveData<List<Election>> = _remoteElections

    // Live data for navigation
    private val _navigateToVoterInfoFragment = MutableLiveData<Election?>()
    val navigateToVoterInfoFragment: LiveData<Election?> = _navigateToVoterInfoFragment

    // Live data for exceptions
    private val _exception = MutableLiveData<Exception>()
    val exception: LiveData<Exception> = _exception


    fun fetchRemoteElections(){
        viewModelScope.launch {
            try{
                _remoteElections.value = repository.getRemoteElections()
            }catch (e: java.lang.Exception){
                _exception.value = e
                Log.e(TAG, "Exception while fetching elections. Exception: $e")
            }
        }
    }

    fun fetchSavedElections(){
        viewModelScope.launch {
            try{
                _savedElections.value = repository.getSavedElections()
            }catch (e: java.lang.Exception){
                Log.e(TAG, "Exception while fetching elections. Exception: $e")
                _exception.value = e
            }
        }
    }

    fun onElectionClicked(election: Election) {
        _navigateToVoterInfoFragment.value = election
    }

    fun onDetailNavigate() {
        _navigateToVoterInfoFragment.value = null
    }

}