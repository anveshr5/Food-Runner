package com.anvesh.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.LOGIN
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var imgLogo: ImageView
    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    lateinit var txtForgotPassword: TextView
    private lateinit var txtRegister: TextView

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences(
            getString(R.string.account_details_preference_file_name),
            Context.MODE_PRIVATE
        )
        getViews()

        if (intent != null) {
            etMobileNumber.setText(intent.getStringExtra("mobileNo"))
        }

        btnLogin.setOnClickListener {
            val mobileNo = etMobileNumber.text.toString()
            val password = etPassword.text.toString()

            if (checkInternet()) {
                if (mobileNo.length == 10) {
                    if (password.length >= 4) {
                        logIn(mobileNo, password)
                    } else {
                        txtForgotPassword.visibility = View.VISIBLE
                        showError(
                            etPassword,
                            "Invalid Credentials?"
                        )
                    }
                } else {
                    txtForgotPassword.visibility = View.VISIBLE
                    showError(
                        etMobileNumber,
                        "Looks like someone is really hungry, stop eating the numbers!"
                    )
                }
            }
        }

        txtForgotPassword.setOnClickListener {
            val mobileNo = etMobileNumber.text.toString()
            if (mobileNo.length == 10) {
                Toast.makeText(
                    this@LoginActivity,
                    "Looks like the previous password wasn't good enough",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                intent.putExtra("mobileNo", mobileNo)
                startActivity(intent)
            } else {
                val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }
        }

        txtRegister.setOnClickListener {
            if (checkInternet()) {
                Toast.makeText(
                    this@LoginActivity,
                    "Making your account, Hot and Fresh!",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun getViews() {
        imgLogo = findViewById(R.id.imgMyProfile)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtForgotPassword.visibility = View.GONE
        txtRegister = findViewById(R.id.txtRegister)
        imgLogo.setOnClickListener {
            Toast.makeText(
                this@LoginActivity,
                getString(R.string.loginlogo),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showError(input: EditText, error: String) {
        input.error = error
        input.requestFocus()
    }

    private fun checkInternet(): Boolean {
        if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(this@LoginActivity)

            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@LoginActivity)
            }
            dialog.create()
            dialog.show()
        }
        return false
    }

    private fun logIn(mobileNo: String, password: String) {
        val queue = Volley.newRequestQueue(this@LoginActivity)

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNo)
        jsonParams.put("password", password)

        val jsonObject =
            object : JsonObjectRequest(Method.POST, LOGIN, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        Toast.makeText(this@LoginActivity, "Logged in", Toast.LENGTH_SHORT).show()

                        val details = data.getJSONObject("data")

                        savePreferences(
                            details.getString("user_id"),
                            details.getString("name"),
                            details.getString("email"),
                            details.getString("mobile_number"),
                            details.getString("address")
                        )

                        val intent = Intent(this@LoginActivity, AllRestaurantsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        when (val error = data.getString("errorMessage")) {
                            "Mobile Number not registered" -> {
                                showError(
                                    etMobileNumber,
                                    "$error. You can register below!"
                                )
                            }
                            "Incorrect password" -> {
                                showError(
                                    etPassword,
                                    error
                                )

                                txtForgotPassword.visibility = View.VISIBLE
                            }
                            else -> Toast.makeText(
                                this@LoginActivity,
                                "Error occurred ${data.getString("errorMessage")}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: JSONException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Some unexpected error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = appJson
                    headers["token"] = TOKEN

                    return headers
                }
            }
        queue.add(jsonObject)
    }

    fun savePreferences(
        user_id: String,
        name: String,
        emailId: String,
        mobileNo: String,
        address: String
    ) {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("user_id", user_id).apply()
        sharedPreferences.edit().putString("name", name).apply()
        sharedPreferences.edit().putString("emailId", emailId).apply()
        sharedPreferences.edit().putString("mobileNumber", mobileNo).apply()
        sharedPreferences.edit().putString("address", address).apply()
    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this@LoginActivity)
    }
}