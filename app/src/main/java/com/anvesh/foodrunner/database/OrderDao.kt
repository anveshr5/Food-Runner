package com.anvesh.foodrunner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrderDao {
    @Insert
    fun insertOrder(orderEntity: OrderEntity)

    @Delete
    fun deleteOrder(orderEntity: OrderEntity)

    @Query("SELECT * FROM getOrder")
    fun getAllOrders(): List<OrderEntity>

    @Query("DELETE FROM getOrder WHERE resId = :resId")
    fun deleteOrders(resId: String)
}