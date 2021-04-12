package com.example.dbwithfragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list.view.*

class RoomAdapter(list: ArrayList<RoomData>) : RecyclerView.Adapter<CustomViewHolder>() {
    var mList : ArrayList<RoomData> = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent,false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val p = mList.get(position)
        holder.setHolder(p)
    }

}

class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun setHolder(personalData: RoomData) {
        itemView.textView_room_no.text = personalData.room_no
        itemView.textView_room_temp.text = personalData.room_temperature
        itemView.textView_room_hum.text = personalData.room_humidity
        itemView.textView_room_gas.text = personalData.room_gas
        itemView.textView_room_dust.text = personalData.room_dust
        itemView.textView_room_light.text = personalData.room_light
    }
}