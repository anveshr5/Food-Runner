package com.anvesh.foodrunner.model

import org.json.JSONArray

data class OrderDetailsModel(
    val orderId: String,
    val resName: String,
    val totalCost: String,
    val orderDate: String,
    val foodItem: JSONArray
)