package com.example.s13_17


import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RaceResultAdapter(private val results: List<RaceResult>) :
    RecyclerView.Adapter<RaceResultAdapter.ResultViewHolder>() {

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivWinner: ImageView = itemView.findViewById(R.id.ivWinner)
        val tvInfo: TextView = itemView.findViewById(R.id.tvInfo)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val pbrabbit:ProgressBar=itemView.findViewById(R.id.progressBar)
        val pbturtle:ProgressBar=itemView.findViewById(R.id.progressBar2)
        val tv3:TextView=itemView.findViewById(R.id.tv3)
        val tv4:TextView=itemView.findViewById(R.id.tv4)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        holder.tvInfo.text = "兔子: ${result.rabbit}, 烏龜: ${result.turtle}, 勝者: ${result.winner}"
        holder.tvTime.text = result.timestamps
        val winnerImage = if (result.winner == "兔子") R.drawable.rabbit else R.drawable.turtle
        holder.ivWinner.setImageResource(winnerImage)
        holder.pbrabbit.progressDrawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
        holder.pbturtle.progressDrawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
        holder.pbrabbit.progress=result.rabbit
        holder.pbturtle.progress=result.turtle
        holder.tv3.text="${result.rabbit}%"
        holder.tv4.text="${result.turtle}%"
    }

    override fun getItemCount(): Int = results.size
}
