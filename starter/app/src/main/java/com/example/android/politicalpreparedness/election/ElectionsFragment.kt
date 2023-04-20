package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.MainActivity
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.util.Utils


class ElectionsFragment: Fragment() {

    companion object {
        private val TAG = ElectionsFragment::class.java.simpleName
    }

    private lateinit var viewModel: ElectionsViewModel
    private var internetConnection = false

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // Setup ViewModel
        val viewModelFactory = ElectionsViewModelFactory((activity as MainActivity).repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)
        binding.viewmodel = viewModel

        // Initiate recycler adapters
        val remoteElectionAdapter = ElectionListAdapter(ElectionListener { election ->
            viewModel.onElectionClicked(election)
        })
        binding.upcomingElectionList.adapter = remoteElectionAdapter
        val savedElectionAdapter = ElectionListAdapter(ElectionListener { election ->
            viewModel.onElectionClicked(election)
        })
        binding.savedElectionList.adapter = savedElectionAdapter

        // Populate recycler adapters
        viewModel.remoteElections.observe(viewLifecycleOwner, androidx.lifecycle.Observer { remoteElectionList ->
            remoteElectionAdapter.submitList(remoteElectionList)
        })
        viewModel.savedElections.observe(viewLifecycleOwner, androidx.lifecycle.Observer { savedElectionList ->
            savedElectionAdapter.submitList(savedElectionList)
        })

        // Navigation to voter info
        viewModel.navigateToVoterInfoFragment.observe(viewLifecycleOwner, androidx.lifecycle.Observer { election ->
            election?.let {
                this.findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id, election.division))
                viewModel.onDetailNavigate()
            }
        })

        // Handle unexpected errors showing a toast to the user
        viewModel.exception.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Toast.makeText(context, getString(R.string.elections_error_toast_text), Toast.LENGTH_LONG).show()
        })

        // Getting elections
        internetConnection = Utils.checkInternetConnection(context!!)
        if(Utils.checkInternetConnection(context!!)){
            viewModel.fetchRemoteElections()
        }else{
            Toast.makeText(context, getString(R.string.no_internet_connection_elections_toast_text), Toast.LENGTH_LONG).show()
        }
        viewModel.fetchSavedElections()

        return binding.root
    }

}