package com.anvesh.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.model.FoodItemModel
import com.anvesh.foodrunner.model.OrderDetailsModel

class OrderHistoryRecyclerAdapter(
    val context: Context,
    private val itemList: ArrayList<OrderDetailsModel>
) : RecyclerView.Adapter<OrderHistoryRecyclerAdapter.OrderHistoryOnViewHolder>() {

    class OrderHistoryOnViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resName: TextView = view.findViewById(R.id.txtResHistoryResName)
        val orderDate: TextView = view.findViewById(R.id.txtDate)
        val orderTime: TextView = view.findViewById(R.id.txtTime)
        val historyItemsRecycler: RecyclerView = view.findViewById(R.id.recyclerResHistoryItems)
        val txtTotalCost: TextView = view.findViewById(R.id.txtTotalCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryOnViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_history_tile, parent, false)
        return OrderHistoryOnViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: OrderHistoryOnViewHolder, position: Int) {
        val orderDetails = itemList[position]
        holder.resName.text = orderDetails.resName

        val orderDate = orderDetails.orderDate
        val time = orderDate.split(" ")
        holder.orderDate.text = time[0]
        holder.orderTime.text = time[1].removeRange(4, 7)
        val totalCost =
            "Total Cost : ₹" + setUpRecyclerHistory(holder.historyItemsRecycler, orderDetails)
        holder.txtTotalCost.text = totalCost
    }

    private fun setUpRecyclerHistory(
        recyclerResHistory: RecyclerView,
        orderDetails: OrderDetailsModel
    ): Int {
        var totalCost = 0
        val foodItemList = ArrayList<FoodItemModel>()
        for (i in 0 until (orderDetails.foodItem.length())) {
            val foodJson = orderDetails.foodItem.getJSONObject(i)
            foodItemList.add(
                FoodItemModel(
                    foodJson.getString("food_item_id"),
                    foodJson.getString("name"),
                    foodJson.getString("cost").toInt()
                )
            )
            totalCost += foodJson.getString("cost").toInt()
        }
        val cartItemAdapter = CartItemAdapter(foodItemList, context)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerResHistory.layoutManager = mLayoutManager
        recyclerResHistory.itemAnimator = DefaultItemAnimator()
        recyclerResHistory.adapter = cartItemAdapter
        return totalCost
    }
}