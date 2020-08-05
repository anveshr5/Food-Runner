package com.anvesh.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.anvesh.foodrunner.R

class SplashPageActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_page)

        sharedPreferences =
            getSharedPreferences(
                getString(R.string.account_details_preference_file_name),
                Context.MODE_PRIVATE
            )
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val intent = if (!isLoggedIn) {
            Intent(this@SplashPageActivity, LoginActivity::class.java)
        } else {
            Intent(this@SplashPageActivity, AllRestaurantsActivity::class.java)
        }
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, 1000)
    }
}