package com.anvesh.foodrunner.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.adapter.OrderHistoryRecyclerAdapter
import com.anvesh.foodrunner.model.OrderDetailsModel
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.FETCH_PREVIOUS_ORDERS
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import org.json.JSONException

class OrderHistoryFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var recyclerAdapter: OrderHistoryRecyclerAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var progressLayout: RelativeLayout

    lateinit var sharedPreferences: SharedPreferences
    var orderList = arrayListOf<OrderDetailsModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        sharedPreferences = activity!!.getSharedPreferences(
            getString(R.string.account_details_preference_file_name),
            Context.MODE_PRIVATE
        )
        val userId = sharedPreferences.getString("user_id", "0")

        recyclerView = view.findViewById(R.id.recyclerPreviousOrderRes)
        layoutManager = LinearLayoutManager(activity as Context)
        progressLayout = view.findViewById(R.id.progressLayout)

        val queue = Volley.newRequestQueue(activity as Context)
        if (checkConnectivity()) {
            val jsonObject = object :
                JsonObjectRequest(
                    Method.GET,
                    FETCH_PREVIOUS_ORDERS + userId,
                    null,
                    Response.Listener {
                        try {
                            progressLayout.visibility = View.GONE
                            val data = it.getJSONObject("data")
                            val success = data.getBoolean("success")
                            if (success) {
                                val orderObjectArray = data.getJSONArray("data")
                                for (i in 0 until orderObjectArray.length()) {
                                    val orderObject = orderObjectArray.getJSONObject(i)
                                    val orderDetails = OrderDetailsModel(
                                        orderObject.getString("order_id"),
                                        orderObject.getString("restaurant_name"),
                                        orderObject.getString("total_cost"),
                                        orderObject.getString("order_placed_at"),
                                        orderObject.getJSONArray("food_items")
                                    )
                                    orderList.add(orderDetails)
                                    recyclerAdapter =
                                        OrderHistoryRecyclerAdapter(activity as Context, orderList)
                                    recyclerView.adapter = recyclerAdapter
                                    recyclerView.layoutManager = layoutManager
                                }
                            } else {
                                val errorMessage = data.getString("error_message")
                                Toast.makeText(activity as Context, errorMessage, Toast.LENGTH_LONG)
                                    .show()
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
                                "Some Volley Error Occurred $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = appJson
                    headers["token"] = TOKEN
                    return headers
                }
            }
            queue.add(jsonObject)
        }
        return view
    }

    private fun checkConnectivity(): Boolean {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                Activity().finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(Activity().parent)
            }
            dialog.create()
            dialog.show()
        }
        return false
    }
}