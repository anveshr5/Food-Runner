package com.anvesh.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.agrawalsuneet.dotsloader.loaders.AllianceLoader
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.adapter.RestaurantMenuAdapter
import com.anvesh.foodrunner.adapter.RestaurantRecyclerAdapter
import com.anvesh.foodrunner.database.OrderEntity
import com.anvesh.foodrunner.database.RestaurantDatabase
import com.anvesh.foodrunner.database.RestaurantEntity
import com.anvesh.foodrunner.model.FoodItemModel
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.FETCH_RESTAURANTS
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import com.google.gson.Gson

class MenuActivity : AppCompatActivity() {

    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    var resId: Int = 0
    private var resName: String? = ""

    lateinit var sharedPreferences: SharedPreferences

    private lateinit var recyclerMenu: RecyclerView
    private lateinit var restaurantMenuAdapter: RestaurantMenuAdapter

    private lateinit var recyclerRestaurant: RecyclerView
    private lateinit var restaurantCardAdapter: RestaurantRecyclerAdapter

    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: AllianceLoader

    lateinit var goToCart: Button

    private var menuList = arrayListOf<FoodItemModel>()
    private var orderList = arrayListOf<FoodItemModel>()
    private var resCard = arrayListOf<RestaurantEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        getViews()

        clearCart("MenuEntry")

        setUpToolBar()

        CartActivity.ClearDBAsync(applicationContext, resId.toString()).execute().get()

        setUpResCard(resCard)

        if (checkOnline()) {
            setUpMenu()
            progressLayout.visibility = View.GONE
        }

        goToCart.setOnClickListener {
            proceedToCart()
        }
    }


    private fun getViews() {
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE
        goToCart = findViewById(R.id.btnProceedToCart)
        goToCart.visibility = View.GONE

        sharedPreferences = getSharedPreferences(
            getString(R.string.restaurant_details_preference_file_name),
            Context.MODE_PRIVATE
        )
    }

    private fun setUpResCard(resCard: ArrayList<RestaurantEntity>) {
        resId = sharedPreferences.getInt("resId", 0)
        resName = sharedPreferences.getString("resName", "No restaurant")
        resCard.add(
            RestaurantEntity(
                resId,
                resName!!,
                sharedPreferences.getString("rating", "4.5").toString(),
                sharedPreferences.getInt("costForOne", 250),
                sharedPreferences.getString("imageUrl", "None").toString()
            )
        )
        recyclerRestaurant = findViewById(R.id.recyclerRestaurant)
        restaurantCardAdapter = RestaurantRecyclerAdapter(this@MenuActivity, resCard, 0)
        recyclerRestaurant.layoutManager = LinearLayoutManager(this@MenuActivity)
        recyclerRestaurant.adapter = restaurantCardAdapter
    }

    private fun proceedToCart() {
        val gson = Gson()
        val foodItems = gson.toJson(orderList)

        val async = ItemsOfCart(
            this@MenuActivity,
            resId.toString(),
            foodItems.toString(),
            1
        ).execute()
        val result = async.get()
        if (result) {
            val intent = Intent(this@MenuActivity, CartActivity::class.java)
            intent.putExtra("resId", resId.toString())
            intent.putExtra("resName", resName)
            startActivity(intent)
        } else {
            Toast.makeText((this@MenuActivity), "Some unexpected error", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setUpMenu() {
        recyclerMenu = findViewById(R.id.recyclerMenuItems)
        if (ConnectionManager().checkConnectivity(this@MenuActivity)) {
            val queue = Volley.newRequestQueue(this@MenuActivity)

            val jsonObjectRequest = object :
                JsonObjectRequest(Method.GET, FETCH_RESTAURANTS + resId, null, Response.Listener {
                    try {
                        progressLayout.visibility = View.GONE
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val menuObject = resArray.getJSONObject(i)
                                val foodItem = FoodItemModel(
                                    menuObject.getString("id"),
                                    menuObject.getString("name"),
                                    menuObject.getString("cost_for_one").toInt()
                                )
                                menuList.add(foodItem)
                                restaurantMenuAdapter = RestaurantMenuAdapter(
                                    this@MenuActivity,
                                    menuList,
                                    object : RestaurantMenuAdapter.OnItemClickListener {
                                        override fun onAddItemClick(foodItem: FoodItemModel) {
                                            orderList.add(foodItem)
                                            if (orderList.size > 0) {
                                                goToCart.visibility = View.VISIBLE
                                                RestaurantMenuAdapter.isCartEmpty = false
                                            }
                                        }

                                        override fun onRemoveItemClick(foodItem: FoodItemModel) {
                                            orderList.remove(foodItem)
                                            if (orderList.isEmpty()) {
                                                goToCart.visibility = View.GONE
                                                RestaurantMenuAdapter.isCartEmpty = true
                                            }
                                        }
                                    })
                                val mLayoutManager = LinearLayoutManager(this@MenuActivity)
                                recyclerMenu.layoutManager = mLayoutManager
                                recyclerMenu.itemAnimator = DefaultItemAnimator()
                                recyclerMenu.adapter = restaurantMenuAdapter
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(this@MenuActivity, it.message, Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = appJson
                    headers["token"] = TOKEN
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            Toast.makeText(this@MenuActivity, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpToolBar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        resName = sharedPreferences.getString("resName", "Restaurant")
        supportActionBar?.title = "$resName Menu"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        if (orderList.isNotEmpty()) {
            val dialog = AlertDialog.Builder(this@MenuActivity)
            dialog.setTitle("Do you wish to go back to Home Page?")
            dialog.setMessage("Your cart will be cleared if you navigate back to Home Page")
            dialog.setPositiveButton("Go Back") { _, _ ->
                clearCart("OnBackPressed")
                intent = Intent(this@MenuActivity, AllRestaurantsActivity::class.java)
                startActivity(intent)
            }
            dialog.setNegativeButton("Cancel") { _, _ ->

            }
            dialog.create()
            dialog.show()
        } else {
            intent = Intent(this@MenuActivity, AllRestaurantsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun clearCart(mode: String) {
        val clearCart = CartActivity.GetItemsFromDBAsync(applicationContext).execute().get()
        val len = clearCart.size - 1

        for (i in 0..len) {
            ItemsOfCart(
                applicationContext,
                clearCart[i].resId,
                clearCart[i].foodItems,
                2
            ).execute()
        }
        if (mode == "OnBackPressed") {
            sharedPreferences.edit().clear().apply()
        }
    }

    class ItemsOfCart(
        context: Context,
        private val restaurantId: String,
        private val foodItems: String,
        private val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "ordered-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(
                        OrderEntity(
                            restaurantId,
                            foodItems
                        )
                    )
                    db.close()
                    return true
                }
                2 -> {
                    db.orderDao().deleteOrder(
                        OrderEntity(
                            restaurantId,
                            foodItems
                        )
                    )
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun checkOnline(): Boolean {
        if (ConnectionManager().checkConnectivity(this@MenuActivity)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(this@MenuActivity)
            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                this.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@MenuActivity)
            }
            dialog.create()
            dialog.show()
        }
        return false
    }
}