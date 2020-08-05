package com.anvesh.foodrunner.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings.ACTION_WIRELESS_SETTINGS
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.adapter.CartItemAdapter
import com.anvesh.foodrunner.adapter.RestaurantMenuAdapter
import com.anvesh.foodrunner.adapter.RestaurantRecyclerAdapter
import com.anvesh.foodrunner.database.OrderEntity
import com.anvesh.foodrunner.database.RestaurantDatabase
import com.anvesh.foodrunner.database.RestaurantEntity
import com.anvesh.foodrunner.model.FoodItemModel
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.PLACE_ORDER
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerCart: RecyclerView
    private lateinit var cartItemAdapter: CartItemAdapter
    private lateinit var recyclerRestaurant: RecyclerView
    private lateinit var restaurantCardAdapter: RestaurantRecyclerAdapter

    private var orderList = ArrayList<FoodItemModel>()

    private lateinit var progressLayout: RelativeLayout
    private lateinit var rlCart: RelativeLayout
    private lateinit var btnPlaceOrder: Button
    private lateinit var txtTotalCost: TextView

    lateinit var sharedPreferences: SharedPreferences

    private var resCard = arrayListOf<RestaurantEntity>()
    private var resId: Int = 1
    private var resName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        progressLayout = findViewById(R.id.progressLayout)
        rlCart = findViewById(R.id.rlCart)

        getResCard()

        setupToolbar()

        setUpCartList()

        placeOrder()
    }

    private fun getResCard() {
        sharedPreferences = getSharedPreferences(
            getString(R.string.restaurant_details_preference_file_name),
            Context.MODE_PRIVATE
        )

        resId = sharedPreferences.getInt("resId", 0)
        resName = sharedPreferences.getString("resName", "No restaurant").toString()
        resCard.add(
            RestaurantEntity(
                resId,
                resName,
                sharedPreferences.getString("rating", "4.5").toString(),
                sharedPreferences.getInt("costForOne", 250),
                sharedPreferences.getString("imageUrl", "None").toString()
            )
        )
        setUpResCard(resCard)
    }

    private fun setUpResCard(resCard: java.util.ArrayList<RestaurantEntity>) {
        recyclerRestaurant = findViewById(R.id.recyclerRestaurant)
        restaurantCardAdapter = RestaurantRecyclerAdapter(this@CartActivity, resCard, 0)
        recyclerRestaurant.layoutManager = LinearLayoutManager(this@CartActivity)
        recyclerRestaurant.adapter = restaurantCardAdapter
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpCartList() {
        recyclerCart = findViewById(R.id.recyclerCartItems)
        val dbList = GetItemsFromDBAsync(applicationContext).execute().get()

        for (element in dbList) {
            orderList.addAll(
                Gson().fromJson(element.foodItems, Array<FoodItemModel>::class.java).asList()
            )
        }

        if (orderList.isEmpty()) {
            rlCart.visibility = View.GONE
            progressLayout.visibility = View.VISIBLE
        } else {
            rlCart.visibility = View.VISIBLE
            progressLayout.visibility = View.GONE
        }

        cartItemAdapter = CartItemAdapter(orderList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this@CartActivity)
        recyclerCart.layoutManager = mLayoutManager
        recyclerCart.adapter = cartItemAdapter
    }

    private fun placeOrder() {
        btnPlaceOrder = findViewById(R.id.btnConfirmOrder)
        txtTotalCost = findViewById(R.id.txtTotalCost)

        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost
        }
        val totalCost = "₹${sum}"
        txtTotalCost.text = totalCost

        btnPlaceOrder.setOnClickListener {
            if (online()) {
                progressLayout.visibility = View.VISIBLE
                btnPlaceOrder.visibility = View.GONE
                sendServerRequest()
            }
        }
    }

    private fun online(): Boolean {
        if (ConnectionManager().checkConnectivity(this@CartActivity)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(this@CartActivity)

            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@CartActivity)
            }
            dialog.create()
            dialog.show()
        }
        return false
    }

    private fun sendServerRequest() {
        val queue = Volley.newRequestQueue(this)

        val jsonParams = JSONObject()
        jsonParams.put("user_id", getUserId())
        jsonParams.put("restaurant_id", resId.toString())
        jsonParams.put("total_cost", totalPrice().toString())
        jsonParams.put("food", getFoodJSONArray())

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, PLACE_ORDER, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if (success) {
                        clearCart("ordered")

                        RestaurantMenuAdapter.isCartEmpty = true

                        val dialog = Dialog(
                            this@CartActivity,
                            android.R.style.Theme_Black_NoTitleBar_Fullscreen
                        )
                        dialog.setContentView(R.layout.order_placed_dialog)
                        dialog.show()
                        dialog.setCancelable(false)
                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                        btnOk.setOnClickListener {
                            dialog.dismiss()
                            startActivity(
                                Intent(
                                    this@CartActivity,
                                    AllRestaurantsActivity::class.java
                                )
                            )
                            ActivityCompat.finishAffinity(this@CartActivity)
                        }
                    } else {
                        rlCart.visibility = View.VISIBLE
                        Toast.makeText(
                            this@CartActivity,
                            "Some Error occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    rlCart.visibility = View.VISIBLE
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                rlCart.visibility = View.VISIBLE
                Toast.makeText(this@CartActivity, it.message, Toast.LENGTH_SHORT).show()
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

    private fun getFoodJSONArray(): JSONArray {
        val foodArray = JSONArray()
        for (i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].foodId)
            foodArray.put(i, foodId)
        }

        return foodArray
    }

    private fun totalPrice(): Int {
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost
        }
        return sum
    }

    private fun getUserId(): String {
        return getSharedPreferences(
            getString(R.string.account_details_preference_file_name),
            Context.MODE_PRIVATE
        ).getString("user_id", null).toString()
    }

    class GetItemsFromDBAsync(context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "ordered-db").build()

        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDao().getAllOrders()
        }
    }

    class ClearDBAsync(context: Context, private val resId: String) :
        AsyncTask<Void, Void, Boolean>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "ordered-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        RestaurantMenuAdapter.isCartEmpty = true
        onBackPressed()
        return true
    }

    private fun clearCart(mode: String) {
        val clearCart = GetItemsFromDBAsync(applicationContext).execute().get()
        val len = clearCart.size - 1

        for (i in 0..len) {
            MenuActivity.ItemsOfCart(
                applicationContext,
                clearCart[i].resId,
                clearCart[i].foodItems,
                2
            ).execute()
        }
        if(mode != "OnBackPressed"){
            sharedPreferences.edit().clear().apply()
        }
    }

    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this@CartActivity)
        dialog.setTitle("Do you wish to go back to $resName Menu?")
        dialog.setMessage("Your cart will be cleared if you navigate back to $resName Menu")
        dialog.setPositiveButton("Go Back") { _, _ ->
            clearCart("OnBackPressed")
            intent = Intent(this@CartActivity, MenuActivity::class.java)
            startActivity(intent)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->

        }
        dialog.create()
        dialog.show()
    }
}