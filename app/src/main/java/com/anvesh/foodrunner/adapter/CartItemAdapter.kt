package com.anvesh.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.model.FoodItemModel

class CartItemAdapter(private val cartList: ArrayList<FoodItemModel>, val context: Context) :
    RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {
    override fun onCreateViewHolder(holder: ViewGroup, viewType: Int): CartViewHolder {
        val itemView =
            LayoutInflater.from(holder.context).inflate(R.layout.recycler_cart_row, holder, false)

        return CartViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val dishDetails = cartList[position]
        holder.itemName.text = dishDetails.name
        val cost = "₹${dishDetails.cost}"
        holder.itemCost.text = cost
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.txtCartItemName)
        val itemCost: TextView = view.findViewById(R.id.txtCartPrice)
    }
}