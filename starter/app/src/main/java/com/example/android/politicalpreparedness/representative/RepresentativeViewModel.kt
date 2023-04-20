package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel(private val repository: Repository): ViewModel() {

    companion object {
        private val TAG = RepresentativeViewModel::class.java.simpleName
    }

    // Live data for representatives
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>> = _representatives

    // Live data for exceptions
    private val _exception = MutableLiveData<Exception>()
    val exception: LiveData<Exception> = _exception

    // Function to fetch representatives from API from a provided address
    fun fetchRepresentatives(address: String){
        viewModelScope.launch {
            try {
                _representatives.value = repository.getRepresentatives(address)
                Log.d(TAG, "Representatives: ${representatives.value}")
            }catch (e: java.lang.Exception){
                Log.e(TAG, "Exception while fetching representatives. Exception: $e")
                _exception.value = e
            }
        }
    }

}
