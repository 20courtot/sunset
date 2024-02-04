package com.example.chatsunset.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatsunset.R
import com.example.chatsunset.activities.ChatActivity
import com.example.chatsunset.models.User

class UsersRecyclerAdapter: RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder>(), Filterable {
    var items: MutableList<User> = mutableListOf()
        set(value){
            field = value
            usersFileredList = value
            notifyDataSetChanged()
        }
    private var usersFileredList: MutableList<User> = mutableListOf()

    override fun getFilter(): Filter {
        return object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if(charSearch.isEmpty()){
                    usersFileredList = items
                }else{
                    val resultList = items.filter{ it.pseudo.lowercase().contains(charSearch.lowercase())}
                    usersFileredList = resultList as MutableList<User>
                }
                val filterResults = FilterResults()
                filterResults.values = usersFileredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                usersFileredList = results?.values as MutableList<User>
                notifyDataSetChanged()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = usersFileredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = usersFileredList[position]
        holder.bind(user)
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvShortPseudo : TextView = itemView.findViewById(R.id.tvShortPseudo)
        val tvPseudo : TextView = itemView.findViewById(R.id.tvPseudo)

        fun bind(user: User) {
            // on reprend la première lettre du pseudo pour l'afficher dans le cercle
            tvShortPseudo.text = user.pseudo[0].toString()
            tvPseudo.text = user.pseudo

            // click sur l'utilisateur dans la liste redirige vers l'activity chat avec l'uid de l'utilisateur
            itemView.setOnClickListener {
                Intent(itemView.context,ChatActivity::class.java).also{
                    it.putExtra("friend",user.uuid)
                    itemView.context.startActivity(it)
                }
            }
        }

    }

}