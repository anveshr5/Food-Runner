package com.anvesh.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.model.FoodItemModel

class RestaurantMenuAdapter(
    val context: Context,
    private val menuList: ArrayList<FoodItemModel>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RestaurantMenuAdapter.MenuViewHolder>() {
    companion object {
        var isCartEmpty = true
    }

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodItemName: TextView = view.findViewById(R.id.txtItemName)
        val foodItemCost: TextView = view.findViewById(R.id.txtItemCost)
        val sno: TextView = view.findViewById(R.id.txtSNo)
        val addToCart: Button = view.findViewById(R.id.btnAddToCart)
        val removeFromCart: Button = view.findViewById(R.id.btnRemoveFromCart)
    }

    override fun onCreateViewHolder(holder: ViewGroup, p1: Int): MenuViewHolder {
        val itemView = LayoutInflater.from(holder.context)
            .inflate(R.layout.recycler_menu_row, holder, false)
        return MenuViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    interface OnItemClickListener {
        fun onAddItemClick(foodItem: FoodItemModel)
        fun onRemoveItemClick(foodItem: FoodItemModel)
    }


    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuObject = menuList[position]

        holder.foodItemName.text = menuObject.name
        val cost = "₹${menuObject.cost}"
        holder.foodItemCost.text = cost
        holder.sno.text = (position + 1).toString()
        holder.addToCart.setOnClickListener {
            holder.addToCart.visibility = View.GONE
            holder.removeFromCart.visibility = View.VISIBLE
            listener.onAddItemClick(menuObject)
        }

        holder.removeFromCart.setOnClickListener {
            holder.removeFromCart.visibility = View.GONE
            holder.addToCart.visibility = View.VISIBLE
            listener.onRemoveItemClick(menuObject)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}