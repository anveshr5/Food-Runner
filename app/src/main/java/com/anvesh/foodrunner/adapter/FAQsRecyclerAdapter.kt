package com.anvesh.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.foodrunner.R
import com.anvesh.foodrunner.model.FAQsModel
import io.armcha.elasticview.ElasticView

class FAQsRecyclerAdapter(val context: Context, private val QAList: List<FAQsModel>) :
    RecyclerView.Adapter<FAQsRecyclerAdapter.FAQsViewHolder>() {

    class FAQsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtQuestion: TextView = view.findViewById(R.id.txtQuestion)
        val txtAnswer: TextView = view.findViewById(R.id.txtAnswer)
        val faqCard: ElasticView = view.findViewById(R.id.FAQCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_faqs, parent, false)
        return FAQsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return QAList.size
    }

    override fun onBindViewHolder(holder: FAQsViewHolder, position: Int) {
        val qA = QAList[position]
        holder.txtQuestion.text = qA.question
        holder.txtAnswer.text = qA.answer

        holder.faqCard.setOnClickListener {
            if (holder.txtAnswer.visibility == View.VISIBLE) {
                holder.txtAnswer.visibility = View.GONE
            } else {
                holder.txtAnswer.visibility = View.VISIBLE
            }
        }
    }
}