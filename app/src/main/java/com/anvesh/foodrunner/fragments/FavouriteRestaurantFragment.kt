package com.anvesh.foodrunner.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.agrawalsuneet.dotsloader.loaders.AllianceLoader
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.adapter.RestaurantRecyclerAdapter
import com.anvesh.foodrunner.database.RestaurantDatabase
import com.anvesh.foodrunner.database.RestaurantEntity

class FavouriteRestaurantFragment : Fragment() {

    private lateinit var recyclerFavourites: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerAdapter: RestaurantRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: AllianceLoader

    private var dbFavResList = listOf<RestaurantEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite_restaurant, container, false)

        recyclerFavourites = view.findViewById(R.id.recyclerFavourites)
        layoutManager = LinearLayoutManager(activity as Context)

        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE

        dbFavResList = RetrieveFavourites(activity as Context).execute().get()
        if (dbFavResList.isEmpty()) {
            progressLayout.visibility = View.VISIBLE
        }

        if (activity != null && dbFavResList.isNotEmpty()) {
            progressLayout.visibility = View.GONE
            recyclerAdapter = RestaurantRecyclerAdapter(activity as Context, dbFavResList, 1)
            recyclerFavourites.adapter = recyclerAdapter
            recyclerFavourites.layoutManager = layoutManager
        }
        return view
    }

    class RetrieveFavourites(val context: Context) :
        AsyncTask<Void, Void, List<RestaurantEntity>>() {
        override fun doInBackground(vararg p0: Void?): List<RestaurantEntity> {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db")
                .build()
            return db.restaurantDao().getAllRestaurants()
        }
    }
}