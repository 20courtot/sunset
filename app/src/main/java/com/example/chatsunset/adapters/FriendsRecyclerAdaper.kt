package com.example.chatsunset.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatsunset.R
import com.example.chatsunset.activities.ChatActivity
import com.example.chatsunset.models.Friend
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date

class FriendsRecyclerAdaper : RecyclerView.Adapter<FriendsRecyclerAdaper.ViewHolder>(){
    var items: MutableList<Friend> = mutableListOf()
        set(value){
            field = value
            notifyDataSetChanged()
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_friend,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = items[position]
        holder.bind(friend)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val ivFriend : ShapeableImageView = itemView.findViewById(R.id.ivFriend)
        val tvPseudo : TextView = itemView.findViewById(R.id.tvPseudo)
        val tvLastMsg : TextView = itemView.findViewById(R.id.tvLastMsg)
        val tvHour: TextView = itemView.findViewById(R.id.tvHour)

        fun bind(friend : Friend){
            tvPseudo.text = friend.pseudo
            tvLastMsg.text = friend.lastMsg
            val sdf = SimpleDateFormat("HH:mm")
            tvHour.text = sdf.format(Date(friend.timestamp))
            // affichage de l'image si il y en a une sinon affichage de l'avatar
            if(friend.image.isNotEmpty()){
                Glide.with(itemView.context).load(friend.image).placeholder(R.drawable.avatar).into(ivFriend)
            }
            // click sur l'item redirige vers l'activity chat avec l'uid de l'utilisateur
            itemView.setOnClickListener {
                Intent(itemView.context, ChatActivity::class.java).also{
                    it.putExtra("friend",friend.uuid)
                    itemView.context.startActivity(it)
                }
            }
        }
    }


}