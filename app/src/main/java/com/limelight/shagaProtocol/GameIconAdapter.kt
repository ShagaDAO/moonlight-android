package com.limelight.shagaProtocol

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.limelight.R

// Adapter class
class GameIconAdapter(private val gameIcons: List<Bitmap>) : RecyclerView.Adapter<GameIconAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gameIconImageView: ImageView = view.findViewById(R.id.gameIconImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_icon_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realPosition = position % gameIcons.size  // Loop through your list
        holder.gameIconImageView.setImageBitmap(gameIcons[realPosition])
    }

    override fun getItemCount(): Int {
        return Math.min(4, gameIcons.size)  // Only show a maximum of 4 items
    }
}
