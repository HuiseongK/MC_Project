package com.example.waru

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.waru.databinding.ActivitySub22Binding

class FragmentOne(filename: String?) :Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ActivitySub22Binding.inflate(inflater, container, false).root
    }
}