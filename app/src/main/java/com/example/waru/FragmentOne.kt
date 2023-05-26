package com.example.waru

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.waru.databinding.ActivitySub21Binding
import java.io.*

class FragmentOne(private val filename: String?) : Fragment(){
    lateinit var binding: ActivitySub21Binding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ActivitySub21Binding.inflate(inflater, container, false)
        binding.result.text = readTextFile(filename)

        return binding.root
    }

    // 저장된 일기에 대한 분석결과를 내부저장소에서 읽어와 textview에 출력
    private fun readTextFile(filename: String?): String?{

        val file = File(requireContext().filesDir, filename)
        Log.d("TAG", "READ - $file")

        if(!file.exists()){
            return null
        }

        val reader = FileReader(file)
        val buffer = BufferedReader(reader)
        var temp: String?
        val result = StringBuffer()

        while(true){
            temp = buffer.readLine()
            if(temp == null){break}
            else {result.append(temp)}
        }

        buffer.close()
        return result.toString()
    }

}