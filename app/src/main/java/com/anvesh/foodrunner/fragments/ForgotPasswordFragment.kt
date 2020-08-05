package com.anvesh.foodrunner.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.activity.ForgotPasswordActivity
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.FORGOT_PASSWORD
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordFragment : Fragment() {

    private lateinit var imgPerson: ImageView
    lateinit var etMobileNo: EditText
    lateinit var etEmailAddress: EditText
    private lateinit var btnReqOTP: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        imgPerson = view.findViewById(R.id.imgPerson)
        etMobileNo = view.findViewById(R.id.etMobileNo)
        etEmailAddress = view.findViewById(R.id.etEmailAddress)
        btnReqOTP = view.findViewById(R.id.btnReqOTP)

        val mobileNumber: String? = arguments?.getString("mobileNo", "")
        etMobileNo.setText(mobileNumber)

        btnReqOTP.setOnClickListener {
            if (checkInternet()) {
                val mobileNo = etMobileNo.text.toString()
                val emailId = etEmailAddress.text.toString().trimEnd()
                if (mobileNo.length == 10) {
                    if (emailId.contains("@") && (emailId.substring(emailId.indexOf(".") + 1)).length > 1) {
                        sendOTP(activity as Context, mobileNo, emailId)
                    } else {
                        showError(etEmailAddress, "Invalid Email Id")
                    }
                } else {
                    showError(etMobileNo, "Invalid Mobile Number")
                }
            }
        }
        imgPerson.setOnClickListener {
            Toast.makeText(
                activity as Context,
                "Forgot your password? No problem we got your back. Reset it and order quickly",
                Toast.LENGTH_SHORT
            ).show()
        }
        return view
    }

    private fun showError(input: EditText, error: String) {
        input.error = error
        input.requestFocus()
    }

    private fun resetPassword(mobileNo: String, emailId: String) {
        val resetPasswordFragment = ResetPasswordFragment()
        val data = Bundle()
        data.putString("mobileNo", mobileNo)
        data.putString("emailId", emailId)

        resetPasswordFragment.arguments = data
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, resetPasswordFragment).commit()
    }

    private fun sendOTP(thisActivity: Context, mobileNo: String, emailId: String) {
        val queue = Volley.newRequestQueue(thisActivity)

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNo)
        jsonParams.put("email", emailId)

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, FORGOT_PASSWORD, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val firstTry = data.getBoolean("first_try")
                        if (firstTry) {
                            Toast.makeText(
                                thisActivity,
                                "An email with an OTP has been sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                thisActivity,
                                "Please check your mail, an OTP has been sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        resetPassword(mobileNo, emailId)
                    } else {
                        when (val error = data.getString("errorMessage")) {
                            "No user found!" -> {
                                showError(etMobileNo, error)
                                showError(etEmailAddress, error)
                            }
                            "Invalid data" -> {
                                showError(etMobileNo, error)
                                showError(etEmailAddress, error)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    Toast.makeText(
                        thisActivity,
                        "Some unexpected error occurred",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(
                    thisActivity,
                    "Some Volley Error Occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = appJson
                    headers["token"] = TOKEN
                    return headers
                }
            }
        queue.add(jsonObjectRequest)
    }

    private fun checkInternet(): Boolean {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(ForgotPasswordActivity())
            }
            dialog.create()
            dialog.show()
        }
        return false
    }
}