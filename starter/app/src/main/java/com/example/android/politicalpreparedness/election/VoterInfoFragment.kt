package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.MainActivity
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.util.Utils
import kotlinx.android.synthetic.main.fragment_voter_info.view.*

class VoterInfoFragment : Fragment() {

    companion object {
        private val TAG = VoterInfoFragment::class.java.simpleName
    }

    private lateinit var viewModel: VoterInfoViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // ViewModel setup
        val viewModelFactory = VoterInfoViewModelFactory((activity as MainActivity).repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(VoterInfoViewModel::class.java)
        binding.viewmodel = viewModel

        // Arguments received by navigation
        val electionId = VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId
        val electionDivision = VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision
        viewModel.electionId.value = electionId

        // Update view with voter info
        if(Utils.checkInternetConnection(context!!)){
            viewModel.getVoterInfo(electionDivision)
        }else{
            Toast.makeText(context, getString(R.string.no_internet_connection_elections_toast_text), Toast.LENGTH_LONG).show()
        }
        viewModel.checkIfElectionIsSaved()

        // Handle loading of URLs
        viewModel.voterInfo.observe(viewLifecycleOwner, Observer { voterInfo ->
            if(!voterInfo.state?.get(0)?.electionAdministrationBody?.electionInfoUrl.isNullOrEmpty()){
                enableLink(binding.stateHeader, voterInfo.state?.get(0)?.electionAdministrationBody?.electionInfoUrl!!)
            }
            if(!voterInfo.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl.isNullOrEmpty()){
                enableLink(binding.stateLocations, voterInfo.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl!!)
            }

            if(!voterInfo.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl.isNullOrEmpty()){
                enableLink(binding.stateBallot, voterInfo.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl!!)
            }

            if(voterInfo.state?.get(0)?.electionAdministrationBody?.correspondenceAddress == null){
                binding.addressGroup.visibility = View.INVISIBLE
            }else{
                binding.stateCorrespondenceHeader.setTextColor(Color.GRAY)
            }
        })

        // Handle save button UI state
        viewModel.electionIsSaved.observe(viewLifecycleOwner, Observer { isSaved ->
            if(isSaved){
                binding.saveRemoveElectionButton.text = getString(R.string.unfollow_election_text_button)
            }else{
                binding.saveRemoveElectionButton.text = getString(R.string.follow_election_text_button)
            }
        })

        // Handle save button clicks
        viewModel.navigateBack.observe(viewLifecycleOwner, Observer { isButtonPressed ->
            if(isButtonPressed){
                findNavController().popBackStack()
            }
        })

        // Handle unexpected errors showing a toast to the user
        viewModel.exception.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Toast.makeText(context, getString(R.string.voter_info_error_toast_text), Toast.LENGTH_LONG).show()
        })

        return binding.root
    }

    // Handle URL intents
    private fun enableLink(view: TextView, link: String){
        view.visibility = View.VISIBLE
        view.setTextColor(Color.GRAY)
        view.paintFlags = view.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        view.setOnClickListener { setIntent(link) }
    }

    private fun setIntent(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivity(intent)
    }

}