package com.anvesh.foodrunner.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.fragments.ForgotPasswordFragment

class ForgotPasswordActivity : AppCompatActivity() {

    private var message: String? = null
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        setUpToolBar()
        openForgotPassword()
    }

    private fun openForgotPassword() {
        if (intent != null) {
            message = intent.getStringExtra("mobileNo")
        }
        val forgotPasswordFragment = ForgotPasswordFragment()
        val mobileNo = Bundle()
        mobileNo.putString("mobileNo", message)
        forgotPasswordFragment.arguments = mobileNo
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, forgotPasswordFragment)
            .commit()
    }

    private fun setUpToolBar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}