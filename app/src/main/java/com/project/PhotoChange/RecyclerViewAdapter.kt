package com.project.PhotoChange

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.zomato.photofilters.imageprocessors.Filter

class RecyclerViewAdapter(val listener: OnItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){

    var photoFilterList = Filters.values()
    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_filter_element, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return photoFilterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(position)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val textViewfilterName: TextView = itemView.findViewById(R.id.textView_filterName)
        private val imageViewfilterPreview: ImageView = itemView.findViewById(R.id.imageView_filterPreview)
        private val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout_filter_element)
        fun bindView(position: Int){
            textViewfilterName.text = photoFilterList[position].filterName
            if(photoFilterList[position].photoFilter!=null)
            imageViewfilterPreview.setImageBitmap(photoFilterList[position].photoFilter?.processFilter(
                Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(itemView.resources, R.drawable.filter_preview), 200, 200, false)
            )
            )
            else
                imageViewfilterPreview.setImageDrawable(ResourcesCompat.getDrawable(itemView.resources, R.drawable.filter_preview, null))
            linearLayout.setOnClickListener {
                selectedPosition = position
                listener.onItemClick(filter = photoFilterList[position].photoFilter, itemView = itemView)
            }

        }
    }
    interface OnItemClickListener{
        fun onItemClick(filter: Filter?, itemView: View?)
    }
}