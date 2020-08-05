package com.anvesh.foodrunner.adapter

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.activity.MenuActivity
import com.anvesh.foodrunner.database.RestaurantEntity
import com.anvesh.foodrunner.fragments.AllRestaurantsFragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.squareup.picasso.Picasso
import io.armcha.elasticview.ElasticView

class RestaurantRecyclerAdapter(
    val context: Context,
    private val itemList: List<RestaurantEntity>,
    private val mode: Int
) :
    RecyclerView.Adapter<RestaurantRecyclerAdapter.RestaurantDetailsViewHolder>() {

    class RestaurantDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val restaurantImage: ImageView = view.findViewById(R.id.imgRestaurantImage)
        val restaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val costPerPerson: TextView = view.findViewById(R.id.txtCostPerPerson)
        val restaurantRating: TextView = view.findViewById(R.id.txtRating)
        val btnAddToFav: ImageView = view.findViewById(R.id.btnAddToFav)

        val cardView: ElasticView = view.findViewById(R.id.RestaurantCardView)
        val resDetailsLayout: LinearLayout = view.findViewById(R.id.resDetailsLayout)

        val sharedPreferences: SharedPreferences = view.context.getSharedPreferences(
            view.context.getString(R.string.restaurant_details_preference_file_name),
            Context.MODE_PRIVATE
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_restaurant_details, parent, false)
        return RestaurantDetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RestaurantDetailsViewHolder, position: Int) {
        if (mode == 1) {
            YoYo.with(Techniques.FadeIn).playOn(holder.cardView)
        } else {
            holder.cardView.flexibility = 3F
        }
        val restaurantDetails = itemList[position]
        val cost = "₹${restaurantDetails.costForOne}"

        holder.restaurantName.text = restaurantDetails.resName
        holder.costPerPerson.text = cost
        holder.restaurantRating.text = restaurantDetails.rating
        Picasso.get().load(restaurantDetails.resImage)
            .error(R.drawable.foodster_logo_square)
            .into(holder.restaurantImage)
        holder.resDetailsLayout.setOnClickListener {
            var isClicked = false
            holder.btnAddToFav.setOnClickListener {
                isClicked = true
            }
            if (!isClicked && mode == 1) {
                val intent = Intent(context, MenuActivity::class.java)

                holder.sharedPreferences.edit().putInt("resId", restaurantDetails.res_id)
                    .apply()
                holder.sharedPreferences.edit().putString("resName", restaurantDetails.resName)
                    .apply()
                holder.sharedPreferences.edit().putInt("costForOne", restaurantDetails.costForOne)
                    .apply()
                holder.sharedPreferences.edit().putString("rating", restaurantDetails.rating)
                    .apply()
                holder.sharedPreferences.edit().putString("imageUrl", restaurantDetails.resImage)
                    .apply()

                val options = ActivityOptions.makeSceneTransitionAnimation(
                    context as Activity,
                    holder.resDetailsLayout,
                    "resCardView"
                )
                android.os.Handler().postDelayed({
                    context.startActivity(intent, options.toBundle())
                }, 200)
            } else {
                notifyDataSetChanged()
            }
        }
        val restaurantEntity =
            RestaurantEntity(
                restaurantDetails.res_id,
                restaurantDetails.resName,
                restaurantDetails.rating,
                restaurantDetails.costForOne,
                restaurantDetails.resImage
            )
        val checkFav = AllRestaurantsFragment.DBAsyncTask(context, restaurantEntity, 1).execute()
        val isFav = checkFav.get()

        if (isFav) {
            holder.btnAddToFav.setImageResource(R.drawable.filled_heart_bold)
        } else {
            holder.btnAddToFav.setImageResource(R.drawable.empty_heart_bold)
        }
        holder.btnAddToFav.setOnClickListener {
            if (!AllRestaurantsFragment.DBAsyncTask(context.applicationContext, restaurantEntity, 1)
                    .execute().get()
            ) {
                val async =
                    AllRestaurantsFragment.DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    holder.btnAddToFav.setImageResource(R.drawable.filled_heart_bold)
                } else {
                    Toast.makeText(
                        context,
                        "Some error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val async =
                    AllRestaurantsFragment.DBAsyncTask(
                        context.applicationContext,
                        restaurantEntity,
                        3
                    ).execute()
                val result = async.get()
                if (result) {
                    holder.btnAddToFav.setImageResource(R.drawable.empty_heart_bold)
                } else {
                    Toast.makeText(
                        context,
                        "Some error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}