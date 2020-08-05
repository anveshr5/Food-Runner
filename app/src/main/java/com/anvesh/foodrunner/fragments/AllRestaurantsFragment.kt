package com.anvesh.foodrunner.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.agrawalsuneet.dotsloader.loaders.AllianceLoader
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.adapter.RestaurantRecyclerAdapter
import com.anvesh.foodrunner.database.RestaurantDatabase
import com.anvesh.foodrunner.database.RestaurantEntity
import com.anvesh.foodrunner.util.ConnectionManager
import com.anvesh.foodrunner.util.FETCH_RESTAURANTS
import com.anvesh.foodrunner.util.TOKEN
import com.anvesh.foodrunner.util.appJson
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class AllRestaurantsFragment : Fragment() {

    lateinit var recyclerRestaurants: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: RestaurantRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: AllianceLoader
    lateinit var noResMsg: RelativeLayout

    val restaurantList = arrayListOf<RestaurantEntity>()
    private val removedResList = arrayListOf<RestaurantEntity>()

    private val ratingComparator = Comparator<RestaurantEntity> { res1, res2 ->
        if (res1.rating.compareTo(res2.rating, true) == 0) {
            res1.resName.compareTo(res2.resName, true)
        } else {
            res1.rating.compareTo(res2.rating, true)
        }
    }
    private val nameComparator = Comparator<RestaurantEntity> { res1, res2 ->
        res1.resName.compareTo(res2.resName, false)
    }
    private val costHtoLComparator = Comparator<RestaurantEntity> { res1, res2 ->
        if (res1.costForOne.toString().compareTo(res2.costForOne.toString(), true) == 0) {
            res1.rating.compareTo(res2.rating, true)
        } else {
            res1.costForOne.toString().compareTo(res2.costForOne.toString(), true)
        }
    }
    private val costLtoHComparator = Comparator<RestaurantEntity> { res1, res2 ->
        if (res1.costForOne.toString().compareTo(res2.costForOne.toString(), true) == 0) {
            res2.rating.compareTo(res1.rating, true)
        } else {
            res1.costForOne.toString().compareTo(res2.costForOne.toString(), true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_restaurant, container, false)
        noResMsg = view.findViewById(R.id.NoResMsg)
        noResMsg.visibility = View.GONE

        setHasOptionsMenu(true)

        recyclerRestaurants = view.findViewById(R.id.recyclerRestaurants)
        layoutManager = LinearLayoutManager(activity)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE

        if (checkOnline()) {
            getRestaurants()
        }
        return view
    }

    private fun getRestaurants() {
        val queue = Volley.newRequestQueue(activity as Context)
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            FETCH_RESTAURANTS,
            null,
            Response.Listener {
                try {
                    progressLayout.visibility = View.GONE
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val resArray = data.getJSONArray("data")
                        for (i in 0 until resArray.length()) {
                            val resObject = resArray.getJSONObject(i)
                            val restaurant = RestaurantEntity(
                                resObject.getString("id").toInt(),
                                resObject.getString("name"),
                                resObject.getString("rating"),
                                resObject.getString("cost_for_one").toInt(),
                                resObject.getString("image_url")
                            )
                            restaurantList.add(restaurant)
                            recyclerAdapter =
                                RestaurantRecyclerAdapter(
                                    activity as Context,
                                    restaurantList,
                                    1
                                )
                            recyclerRestaurants.adapter = recyclerAdapter
                            recyclerRestaurants.layoutManager = layoutManager
                        }
                    } else {
                        Toast.makeText(
                            activity as Context,
                            "Some Error Occurred $it",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(
                        activity as Context,
                        "Some unexpected error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener {
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
                headers["Content-type"] = appJson
                headers["token"] = TOKEN
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    private fun checkOnline(): Boolean {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return false
    }

    private fun refreshResList() {
        if (checkOnline()) {
            restaurantList.removeAll(restaurantList)
            getRestaurants()
            noResMsg.visibility = View.GONE
        }
    }

    private fun getSearchResult(searchItem: String) {
        val search = searchItem.trimEnd()
        noResMsg.visibility = View.GONE
        var len = removedResList.size - 1
        for (i in 0..len) {
            restaurantList.add(removedResList[i])
        }
        removedResList.removeAll(removedResList)
        len = restaurantList.size - 1

        if (search.isNotEmpty()) {
            for (i in 0..len) {
                if (!restaurantList[i].resName.contains(search, ignoreCase = true)) {
                    //NoResMsg.visibility = View.GONE
                    removedResList.add(restaurantList[i])
                }
            }
            len = removedResList.size - 1
            for (i in 0..len) {
                restaurantList.remove(removedResList[i])
            }
            if (restaurantList.size == 0) {
                noResMsg.visibility = View.VISIBLE
            }
        } else {
            refreshResList()
        }
        recyclerAdapter.notifyDataSetChanged()
    }

    class DBAsyncTask(
        val context: Context,
        private val restaurantEntity: RestaurantEntity,
        private val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {
            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? =
                        db.restaurantDao().getRestaurantById(restaurantEntity.res_id.toString())
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_compare, menu)
        if (checkOnline()) {
            val searchItem = menu.findItem(R.id.searchView)
            val searchView = searchItem.actionView as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.clearFocus()
                    searchView.setQuery(query, false)
                    searchItem.collapseActionView()
                    if (query != null) {
                        getSearchResult(query)
                        noResMsg.visibility = View.GONE
                    }
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != null) {
                        getSearchResult(newText)
                    }
                    return false
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            when (id) {
                R.id.comName -> {
                    Collections.sort(restaurantList, nameComparator)
                    recyclerAdapter.notifyDataSetChanged()
                }
                R.id.comRating -> {
                    Collections.sort(restaurantList, ratingComparator)
                    restaurantList.reverse()
                    recyclerAdapter.notifyDataSetChanged()
                }
                R.id.comCostHighToLow -> {
                    Collections.sort(restaurantList, costHtoLComparator)
                    restaurantList.reverse()
                    recyclerAdapter.notifyDataSetChanged()
                }
                R.id.comCostLowToHigh -> {
                    Collections.sort(restaurantList, costLtoHComparator)
                    recyclerAdapter.notifyDataSetChanged()
                }
            }
            return super.onOptionsItemSelected(item)
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Connection Issue")
            dialog.setMessage("No Internet Connection found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return super.onOptionsItemSelected(item)
    }
}