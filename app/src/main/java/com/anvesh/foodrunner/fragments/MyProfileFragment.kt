package com.anvesh.foodrunner.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.anvesh.foodrunner.R

class MyProfileFragment : Fragment() {

    private lateinit var txtName: TextView
    private lateinit var txtEmailId: TextView
    private lateinit var txtMobileNo: TextView
    private lateinit var txtDeliveryAddress: TextView

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        txtName = view.findViewById(R.id.txtName)
        txtMobileNo = view.findViewById(R.id.txtMobileNo)
        txtEmailId = view.findViewById(R.id.txtEmailId)
        txtDeliveryAddress = view.findViewById(R.id.txtDeliveryAddress)

        sharedPreferences = activity!!.getSharedPreferences(
            getString(R.string.account_details_preference_file_name),
            Context.MODE_PRIVATE
        )

        txtName.text = sharedPreferences.getString("name", "Name")
        txtMobileNo.text = sharedPreferences.getString("mobileNumber", "Mobile Number")
        txtEmailId.text = sharedPreferences.getString("emailId", "Email Id")
        txtDeliveryAddress.text = sharedPreferences.getString("address", "Address")

        return view
    }
}