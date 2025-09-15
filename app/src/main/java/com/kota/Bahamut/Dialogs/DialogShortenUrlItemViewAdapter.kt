package com.kota.Bahamut.Dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.DataModels.ShortenUrl
import com.kota.Bahamut.R

class DialogShortenUrlItemViewAdapter(private val _blocklist: List<ShortenUrl>) : 
    RecyclerView.Adapter<DialogShortenUrlViewHolder>() {
    
    private var mClickListener: DialogShortenUrlItemViewListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogShortenUrlViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.shorten_url_item_view, parent, false)
        return DialogShortenUrlViewHolder(v, mClickListener)
    }

    override fun onBindViewHolder(holder: DialogShortenUrlViewHolder, position: Int) {
        val obj = getItem(position)
        holder.setTitle(obj.title)
        holder.setDescription(obj.description)
    }

    fun getItem(position: Int): ShortenUrl {
        return _blocklist[position]
    }

    override fun getItemCount(): Int {
        return _blocklist.size
    }

    fun setOnItemClickListener(listener: DialogShortenUrlItemViewListener?) {
        mClickListener = listener
    }
}
