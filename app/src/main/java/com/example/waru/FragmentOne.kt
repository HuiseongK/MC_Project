package com.example.waru

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.waru.databinding.ActivitySub21Binding

class FragmentOne : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ActivitySub21Binding.inflate(inflater, container, false).root
    }
}