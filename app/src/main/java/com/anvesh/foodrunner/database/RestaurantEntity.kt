package com.anvesh.foodrunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val res_id: Int,
    @ColumnInfo(name = "res_name") val resName: String,
    @ColumnInfo(name = "rating") val rating: String,
    @ColumnInfo(name = "cost_for_one") val costForOne: Int,
    @ColumnInfo(name = "res_Image") val resImage: String
)