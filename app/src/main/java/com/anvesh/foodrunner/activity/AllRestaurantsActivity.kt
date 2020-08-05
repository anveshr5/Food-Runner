package com.anvesh.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.database.RestaurantEntity
import com.anvesh.foodrunner.fragments.*
import com.google.android.material.navigation.NavigationView

class AllRestaurantsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var frameLayout: FrameLayout
    private lateinit var navigationView: NavigationView

    var name = "!"
    private lateinit var txtDrawerName: TextView

    lateinit var sharedPreferences: SharedPreferences
    private var restaurant = listOf<RestaurantEntity>()
    private var previousMenuItem: MenuItem? = null

    private var exit = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_restaurants)

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frameLayout)
        navigationView = findViewById(R.id.navigationView)
        setUpToolBar()

        sharedPreferences = getSharedPreferences(
            getString(R.string.account_details_preference_file_name),
            Context.MODE_PRIVATE
        )

        val drawerHeader = navigationView.getHeaderView(0)
        name = sharedPreferences.getString("name", "1").toString()

        openRestaurantList()

        txtDrawerName = drawerHeader.findViewById(R.id.txtName)
        val userName = "Hey! $name"
        txtDrawerName.text = userName

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@AllRestaurantsActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.home -> openRestaurantList()

                R.id.myProfile -> openMyProfile()

                R.id.favouriteRestaurants -> openFavouriteRestaurant()

                R.id.orderHistory -> openOrderHistory()

                R.id.faqs -> openFaqs()

                R.id.logOut -> {
                    logOut(it)
                    it.isChecked = true
                }
            }
            previousMenuItem = it
            return@setNavigationItemSelectedListener true
        }
    }

    private fun logOut(logOutMenuItem: MenuItem) {
        val dialog = AlertDialog.Builder(this@AllRestaurantsActivity)
        dialog.setTitle("Do you want to log out?")
        dialog.setMessage("If you log out you will lose your favourite restaurants list.")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Logout") { _, _ ->
            restaurant =
                FavouriteRestaurantFragment.RetrieveFavourites(applicationContext).execute().get()

            val len = restaurant.size - 1

            for (i in 0..len) {
                AllRestaurantsFragment.DBAsyncTask(applicationContext, restaurant[i], 3)
                    .execute().get()
            }
            logout()
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            logOutMenuItem.isChecked = false
            drawerLayout.closeDrawers()
        }
        dialog.create()
        dialog.show()
        dialog.setOnCancelListener { }
    }

    private fun openOrderHistory() {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, OrderHistoryFragment())
            .commit()
        drawerLayout.closeDrawers()
        navigationView.setCheckedItem(R.id.orderHistory)
        supportActionBar?.title = "Order History"
    }

    private fun logout() {
        clearPreferences()
        val intent = Intent(this@AllRestaurantsActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openFaqs() {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, FaqsFragment()).commit()
        drawerLayout.closeDrawers()
        navigationView.setCheckedItem(R.id.faqs)
        supportActionBar?.title = "FAQ's"
    }

    private fun openFavouriteRestaurant() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, FavouriteRestaurantFragment()).commit()
        Toast.makeText(this, "Favourites", Toast.LENGTH_SHORT).show()
        drawerLayout.closeDrawers()
        navigationView.setCheckedItem(R.id.favouriteRestaurants)
        supportActionBar?.title = "Favourites"
    }

    private fun openMyProfile() {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, MyProfileFragment())
            .commit()
        drawerLayout.closeDrawers()
        navigationView.setCheckedItem(R.id.myProfile)
        supportActionBar?.title = "Your Profile"
    }

    private fun openRestaurantList() {
        supportFragmentManager.beginTransaction().replace(
            R.id.frameLayout,
            AllRestaurantsFragment()
        ).commit()
        drawerLayout.closeDrawers()
        supportActionBar?.title = "Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.foodster)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            !is AllRestaurantsFragment -> openRestaurantList()
            else -> {
                when (exit) {
                    1 -> {
                        openRestaurantList()
                        exit = 0
                        Toast.makeText(
                            this@AllRestaurantsActivity,
                            "Press again to exit",
                            Toast.LENGTH_SHORT
                        ).show()
                        Handler().postDelayed({
                            exit = 1
                        }, 5000)
                    }
                    0 -> {
                        super.onBackPressed()
                        ActivityCompat.finishAffinity(this@AllRestaurantsActivity)
                    }
                }
            }
        }
    }
}