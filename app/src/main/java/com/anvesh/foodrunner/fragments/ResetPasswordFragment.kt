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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.activity.ForgotPasswordActivity
import com.anvesh.foodrunner.activity.LoginActivity
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.RESET_PASSWORD
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import org.json.JSONException
import org.json.JSONObject

class ResetPasswordFragment : Fragment() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmPassword: EditText
    private lateinit var btnReset: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)

        etOTP = view.findViewById(R.id.etOTP)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnReset = view.findViewById(R.id.btnReset)

        val mobileNo = arguments?.getString("mobileNo", "Error Mobile")

        btnReset.setOnClickListener {
            if (checkInternet()) {
                val otp = etOTP.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()
                if (otp.length == 4) {
                    if (newPassword.length > 5) {
                        if (newPassword == confirmPassword) {
                            if (mobileNo != null) {
                                resetPasswordRequest(mobileNo, otp, newPassword)
                            } else {
                                Toast.makeText(
                                    activity as Context,
                                    "Some unknown error occurred",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            showError(
                                etConfirmPassword,
                                "Password don't match"
                            )
                        }
                    } else {
                        showError(
                            etNewPassword,
                            "Password not strong enough!"
                        )
                    }
                } else {
                    showError(etOTP, "Invalid OTP!")
                }
            }
        }
        return view
    }

    private fun showError(input: EditText, error: String) {
        input.error = error
        input.requestFocus()
    }

    private fun resetPasswordRequest(mobileNo: String, otp: String, newPassword: String) {
        val queue = Volley.newRequestQueue(activity as Context)
        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNo)
        jsonParams.put("password", newPassword)
        jsonParams.put("otp", otp)

        val jsonObjectRequest =
            object : JsonObjectRequest(
                Method.POST,
                RESET_PASSWORD,
                jsonParams,
                Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            Toast.makeText(
                                activity as Context,
                                data.getString("successMessage"),
                                Toast.LENGTH_SHORT
                            ).show()
                            goToLogin(mobileNo)
                        } else {
                            when (val error = data.getString("errorMessage")) {
                                "Invalid OTP" -> {
                                    showError(etOTP, error)
                                }
                                "Invalid Data" -> {
                                    showError(etNewPassword, "Password not strong enough")
                                    etConfirmPassword.error
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Response.ErrorListener {
                    if (activity != null) {
                        Toast.makeText(
                            activity as Context,
                            "Some Volley Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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

    private fun goToLogin(mobileNo: String) {
        val intent = Intent(activity as Context, LoginActivity::class.java)
        intent.putExtra("mobileNo", mobileNo)
        startActivity(intent)
        activity?.finish()
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