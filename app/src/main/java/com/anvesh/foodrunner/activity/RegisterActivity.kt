package com.anvesh.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.REGISTER
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private var online = false

    lateinit var toolbar: Toolbar
    private lateinit var etName: EditText
    lateinit var etEmailId: EditText
    lateinit var etMobileNumber: EditText
    private lateinit var etDeliveryAddress: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        toolbar = findViewById(R.id.toolbar)
        setUpToolBar()
        etName = findViewById(R.id.etName)
        etEmailId = findViewById(R.id.etEmailId)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            online = checkInternet()
            if (online) {
                registerRequest()
            }
        }
    }

    private fun checkInternet(): Boolean {
        if (ConnectionManager().checkConnectivity(this@RegisterActivity)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(this@RegisterActivity)
            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@RegisterActivity)
            }
            dialog.create()
            dialog.show()
        }
        return false
    }

    private fun registerRequest() {

        var name = etName.text.toString().trimEnd()
        var email = etEmailId.text.toString().trimEnd()
        var mobileNo = etMobileNumber.text.toString()
        var address = etDeliveryAddress.text.toString()
        val password = etPassword.text.toString()

        if (checkFields()) {
            val queue = Volley.newRequestQueue(this@RegisterActivity)

            val jsonParams = JSONObject()
            jsonParams.put("name", name)
            jsonParams.put("mobile_number", mobileNo)
            jsonParams.put("password", password)
            jsonParams.put("email", email)
            jsonParams.put("address", address)

            val jsonObject = object :
                JsonObjectRequest(Method.POST, REGISTER, jsonParams, Response.Listener {
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val details = data.getJSONObject("data")
                            val userId = details.getString("user_id")
                            name = details.getString("name")
                            email = details.getString("email")
                            mobileNo = details.getString("mobile_number")
                            address = details.getString("address")
                            savePreferences(userId, name, email, mobileNo, address)

                            val intent =
                                Intent(this@RegisterActivity, AllRestaurantsActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = data.getString("errorMessage")
                            if (errorMessage.contains("Mobile number OR Email Id is already registered")) {
                                showError(etMobileNumber, errorMessage)
                                showError(etEmailId, errorMessage)
                            }
                            Toast.makeText(
                                this@RegisterActivity,
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Some unexpected error occurred",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = appJson
                    headers["token"] = TOKEN
                    return headers
                }
            }
            queue.add(jsonObject)

            Toast.makeText(
                this@RegisterActivity,
                "Your account is being prepared",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkFields(): Boolean {
        val name = etName.text.toString().trimEnd()
        val email = etEmailId.text.toString().trimEnd()
        val mobileNo = etMobileNumber.text.toString()
        val address = etDeliveryAddress.text.toString().trimEnd()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        if (name.length > 3) {
            if (email.length >= 5) {
                if (email.contains("@") && (email.substring(email.indexOf(".") + 1)).length >= 2) {
                    if (mobileNo.length == 10) {
                        if (address.length >= 10) {
                            if (password.length >= 4) {
                                if (password == confirmPassword) {
                                    return true
                                } else {
                                    showError(
                                        etConfirmPassword,
                                        "Password do not match"
                                    )
                                }
                            } else {
                                showError(
                                    etPassword,
                                    "Password can not be shorter than 4 characters"
                                )
                            }
                        } else {
                            showError(
                                etDeliveryAddress,
                                "Address must be greater than 10 chars"
                            )
                        }
                    } else {
                        showError(
                            etMobileNumber,
                            "Mobile number must be 10 digits"
                        )
                    }
                } else {
                    showError(etEmailId, "Invalid EmailId")
                }
            } else {
                showError(etEmailId, "EmailId cannot be left empty")
            }
        } else {
            showError(etName, "Name must be at least 3 characters long")
        }
        return false
    }

    private fun showError(input: EditText, errorMessage: String) {
        input.error = errorMessage
        input.requestFocus()
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun savePreferences(
        user_id: String,
        name: String,
        emailId: String,
        mobileNo: String,
        address: String
    ) {
        val sharedPreferences = getSharedPreferences(
            getString(R.string.account_details_preference_file_name),
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("user_id", user_id).apply()
        sharedPreferences.edit().putString("name", name).apply()
        sharedPreferences.edit().putString("emailId", emailId).apply()
        sharedPreferences.edit().putString("mobileNumber", mobileNo).apply()
        sharedPreferences.edit().putString("address", address).apply()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}