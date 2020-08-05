package com.anvesh.foodrunner.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.adapter.FAQsRecyclerAdapter
import com.anvesh.foodrunner.model.FAQsModel


class FaqsFragment : Fragment() {

    private lateinit var recyclerFAQs: RecyclerView
    private lateinit var recyclerAdapter: FAQsRecyclerAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_faqs, container, false)

        sharedPreferences = context!!.getSharedPreferences(
            getString(R.string.account_details_preference_file_name),
            Context.MODE_PRIVATE
        )

        val mobile = sharedPreferences.getString("mobileNumber", "9000000000")
        val email = sharedPreferences.getString("emailId", "foodster@gmail.com")

        val qAList = listOf(
            FAQsModel(
                "What kind of restaurants are listed on Foodster?",
                "We have top restaurants listed. Our restaurants offer multiple cuisine options including Thai, Chinese, Indian, Pizza, Burgers, Italian, Mexican and so on. You name it, we have it!"
            ),
            FAQsModel(
                "Do you charge any delivery fee?",
                "No, as of now all the deliveries are done for free. Happy binging with your food!"
            ),
            FAQsModel(
                "How long will it take to delivery my food?",
                "We know you are hungry, so we strive to get your food to you as soon as possible. Most deliveries will arrive in under an hour. However, remember delivery time can be influenced by the restaurants ability to prepare your food, weather and traffic conditions. We always try to do our best and if there is any delay, we always try to convey it to you."
            ),
            FAQsModel(
                "Can I edit my order?",
                "Your order can be edited before it reaches the restaurant. Call us on $mobile or email us at $email to do so. Once the order is placed and the restaurant starts preparing your food, you may not be able to edit its contents."
            ),
            FAQsModel(
                "I have a big party coming up. Can I have a discount?",
                "For party arrangements and bulk orders we can offer some special offers and discounts. Get in touch with us at $email or $mobile"
            ),
            FAQsModel(
                "How do I know if my order has gone through?",
                "You will get a confirmation dialog as soon as the restaurant accepts your order."
            ),
            FAQsModel(
                "My order is getting late. What should I do?",
                "If you're not happy, we're not happy. Our partner restaurants do a great job of preparing and delivering orders on time, but sometimes unexpected things get in the way. If your tummy rumbles are getting louder with every tick of the clock, give the restaurant a call. If you can’t get through, buzz us at $mobile so we can track down your food."
            ),
            FAQsModel(
                "I have received my order but it’s not what I requested. What should I do?",
                "Quick, double check the confirmation email! Maybe the computer gremlins threw in some extra sides for themselves? If there is a difference between what’s on the email and what you received, please call the restaurant right away to let them know. Of course, our team are also always happy to help you out if you’re not able to resolve this directly with the restaurant."
            ),
            FAQsModel(
                "Where do the ratings for the restaurant come from?",
                "Each restaurant is rated by Foodster's standards. You worry about your carving, Let us worry about your experience"
            ),
            FAQsModel(
                "Why should I use Foodster instead of calling the restaurant?",
                "Restaurants are quite busy with their work. Getting a popular restaurant on a call would take an eternity some times and the delivery service is neither fast nor reliable, Here at Foodster, we create less hassle, handle your orders exceptionally well and delivery on time! "
            ),
            FAQsModel(
                "Is there a minimum order quantity?",
                "No, there is no minimum order quantity or amount with us, we always have believed in satisfying the need."
            ),
            FAQsModel(
                "What if I need to change the place of delivery instead of the prevailing address?",
                "Like we said, we believe in satisfying needs, we will be happy to deliver to your new address, provided it is within same distance of the prevailing address of delivery in terms of kilometres. However, this can be done on a one off basis but not on a daily basis."
            ),
            FAQsModel(
                "What are your delivery timings?",
                "We deliver every day of the week. Our deliveries are done daily between 8.00 am and 10.30 pm."
            ),
            FAQsModel(
                "Can I choose items from multiple restaurants in a single order?",
                "You can order from more than one restaurant, but not in a single order. You will need to place separate orders for each restaurant/store."
            ),
            FAQsModel(
                "What are your payment methods?",
                "We accept both Cash on Delivery and online payments. We are working on other payment methods to make your experience better."
            ),
            FAQsModel(
                "How do I provide Feedback?",
                "You can contact us by phone on $mobile or email us at $email."
            ),
            FAQsModel(
                "What if I'm not at home when the delivery boy comes by? Will they redeliver?",
                "Unfortunately, redelivery is not possible, But please arrange for someone to be home accordingly, we can hand it to your neighbours also."
            ),
            FAQsModel(
                "Where is my personal information used?",
                "We do not share your personal details (phone number, mail ID and delivery address) with the delivery restaurants. However we may use your number/mail id to send you promotional offers about Foodster. We take security seriously and we take precautions to keep your personal information secure. We have put in place appropriate physical, electronic and managerial procedures to safeguard the information we collect. However, due to the open communication nature of the Internet, Foodster cannot guarantee that communications between you and Foodster or information stored on Foodster servers, will be free from unauthorized access by third parties."
            ),
            FAQsModel(
                "How can I get my restaurant to take orders from your app?",
                "You can get your restaurant registered by getting in touch with us at $email or by giving us a call at $mobile. Our representative will let you know the procedure in detail."
            ),
            FAQsModel(
                "Why am I seeing my Email Id and Mobile number in the FAQ's section?",
                "This is just a model app and I did not want to share my details ;D Hope you enjoy using my app!"
            )
        )

        recyclerFAQs = view.findViewById(R.id.recyclerFAQs)
        layoutManager = LinearLayoutManager(activity as Context)
        recyclerAdapter = FAQsRecyclerAdapter(activity as Context, qAList)
        recyclerFAQs.adapter = recyclerAdapter
        recyclerFAQs.layoutManager = layoutManager

        return view
    }
}