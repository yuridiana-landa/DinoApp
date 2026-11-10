package com.example.dinoapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dinoapp.R

data class LibroItem(val title: String, val imageUrl: String)

class LibroAdapter(
    private val librosList: List<LibroItem>,
    private val onClick: (LibroItem) -> Unit
) : RecyclerView.Adapter<LibroAdapter.LibroViewHolder>() {

    class LibroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgLibro: ImageView = view.findViewById(R.id.imgLibro)
        val txtNombreLibro: TextView = view.findViewById(R.id.txtNombreLibro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_libro, parent, false)
        return LibroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibroViewHolder, position: Int) {
        val libro = librosList[position]
        holder.txtNombreLibro.text = libro.title

        Glide.with(holder.itemView.context)
            .load(libro.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .centerCrop()
            .into(holder.imgLibro)

        holder.itemView.setOnClickListener {
            onClick(libro)
        }
    }

    override fun getItemCount(): Int = librosList.size
}