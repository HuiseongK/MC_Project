package com.example.waru

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.waru.databinding.ListMainBinding

class MyAdapter(private var dataSet: MutableList<MyElement>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ListMainBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = dataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        val currentElement = dataSet[position]

        binding.sentence.text = currentElement.text
        binding.score.text = "Score: ${currentElement.sentimentScore}"
        binding.magnitude.text = "Magnitude: ${currentElement.sentimentMagnitude}"
    }
}