package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.MainActivity
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.util.Utils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_voter_info.*
import java.util.*

class DetailFragment : Fragment() {

    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private val TAG = DetailFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var viewModel: RepresentativeViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Binding setup
        binding = FragmentRepresentativeBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // ViewModel setup
        val viewModelFactory = RepresentativeViewModelFactory((activity as MainActivity).repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RepresentativeViewModel::class.java)

        // Define and assign Representative adapter
        val adapter = RepresentativeListAdapter()
        binding.representativesRecyclerView.adapter = adapter

        // Populate representative list
        viewModel.representatives.observe(viewLifecycleOwner, androidx.lifecycle.Observer { list ->
            adapter.submitList(list)
        })

        // Binding buttons listeners (search with input address)
        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            val line1 = binding.addressLine1.text.toString()
            val line2 = binding.addressLine2.text.toString()
            val city = binding.city.text.toString()
            val state = binding.state.selectedItem.toString()
            val zip = binding.zip.text.toString()
            if(line1.isEmpty() || line2.isEmpty() || city.isEmpty() ||
                    state.isEmpty() || zip.isEmpty()){
                Toast.makeText(context, getString(R.string.address_empty_text), Toast.LENGTH_SHORT).show()
            }else{
                val address = Address(line1, line2, city, state, zip).toString()
                Log.d(TAG, "onButtonSearch, address: $address")
                if(Utils.checkInternetConnection(context!!)){
                    viewModel.fetchRepresentatives(address)
                }else{
                    Toast.makeText(context, getString(R.string.no_internet_connection_representatives_toast_text), Toast.LENGTH_LONG).show()
                }
            }
        }

        // Binding buttons listeners (search with current phone address)
        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            checkLocationPermissionsAndFetchRepresentatives()
        }

        // Handle unexpected errors showing a toast to the user
        viewModel.exception.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Toast.makeText(context, getString(R.string.representatives_error_toast_text), Toast.LENGTH_LONG).show()
        })

        return binding.root

    }

    private fun checkLocationPermissionsAndFetchRepresentatives() {
        if (isPermissionGranted()) {
            checkDeviceLocationSettingsAndGetLocation()
        } else {
            requestForegroundLocationPermission()
        }
    }

    /**
     * Check if location permission is granted
     */
    private fun isPermissionGranted(): Boolean {
        return (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
    }

    /**
     * Request location permission
     */
    private fun requestForegroundLocationPermission(){
        Log.d(TAG, "requestForegroundLocationPermission")
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(
            permissionsArray,
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")
        if (
            grantResults.isEmpty() ||
            (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED))
        {
            Log.i(TAG, "onRequestPermissionsResult: location permission denied")
            Snackbar.make(
                binding.root,
                getString(R.string.request_location_permission_snackbar_text),
                Snackbar.LENGTH_LONG
            )
                .setAction("Settings") {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            Log.d(TAG, "onRequestPermissionsResult: location permission approved")
            checkDeviceLocationSettingsAndGetLocation()
        }
    }

    /**
     * Check if location is enabled
     * - Location disable -> request to turn on
     * - Location enabled -> get current location
     */
    private fun checkDeviceLocationSettingsAndGetLocation() {
        Log.d(TAG, "checkDeviceLocationSettings")
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(context!!)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            Log.d(TAG, "checkDeviceLocationSettings: location is off")
            if (exception is ResolvableApiException){
                try {
                    startIntentSenderForResult(exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON, null,0,0,
                        0,null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.location_is_disable_snackbar_text), Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndGetLocation()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                Log.d(TAG, "checkDeviceLocationSettings: location is on")
                getLocationAndFetchRepresentatives()
            }
        }
    }

    /**
     * Get current location and fetch representatives
     */
    @SuppressLint("MissingPermission")
    private fun getLocationAndFetchRepresentatives() {
        Log.i(TAG, "getLocation")
        LocationServices.getFusedLocationProviderClient(context!!).lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if(location == null){
                    Log.i(TAG, "getLocation succeed. Location: null")
                    Toast.makeText(context, getString(R.string.location_null_toast_text), Toast.LENGTH_SHORT).show()
                }else{
                    Log.i(TAG, "getLocation succeed. Location: $location")
                    try{
                        val currentAddress = geoCodeLocation(location)
                        fillAddressFields(currentAddress)
                        if(Utils.checkInternetConnection(context!!)){
                            viewModel.fetchRepresentatives(currentAddress.toString())
                        }else{
                            Toast.makeText(context, getString(R.string.no_internet_connection_representatives_toast_text), Toast.LENGTH_LONG).show()
                        }
                    }catch (e: java.lang.Exception){
                        Toast.makeText(context,getString(R.string.unexpected_error_representatives_toast_text), Toast.LENGTH_SHORT).show()
                    }

                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Fail getting the location")
            }
    }

    /**
     * Transform lat and long to an address
     */
    @Suppress("DEPRECATION")
    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context!!, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            ?.map { address ->
                Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
            }
            ?.first()!!
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    /**
     * Fill all the address fields of the layout
     */
    private fun fillAddressFields(currentAddress: Address){
        binding.addressLine1.setText(currentAddress.line1)
        binding.addressLine2.setText(currentAddress.line2)
        binding.city.setText(currentAddress.city)
        binding.state.setSelection(resources.getStringArray(R.array.states).indexOf(currentAddress.state))
        binding.zip.setText(currentAddress.zip)
    }

}
