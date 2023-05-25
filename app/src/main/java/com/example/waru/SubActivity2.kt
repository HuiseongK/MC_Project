package com.example.waru

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.waru.databinding.ActivitySub2Binding
import com.google.android.material.tabs.TabLayout

class SubActivity2 : AppCompatActivity() {
    lateinit var binding: ActivitySub2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySub2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val tab = binding.tab
        val tab1:TabLayout.Tab = tab.newTab()
        tab1.text="분석 결과"
        tab.addTab(tab1)

        val tab2:TabLayout.Tab = tab.newTab()
        tab2.text="코멘트"
        tab.addTab(tab2)

        //tab선택 전에 tab1이 실행되게 만들기 위해서 replaceFragment 메소드를 만들어줌
        tab.selectTab(tab1)
        replaceFragment(FragmentOne())

        tab.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val transaction = supportFragmentManager.beginTransaction()
                when(tab?.text){
                    "분석 결과" -> transaction.replace(binding.tabContent.id,FragmentOne())
                    "코멘트" -> transaction.replace(binding.tabContent.id,FragmentTwo())
               }
                transaction.commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.tabContent.id, fragment)
        transaction.commit()
    }

    //intent를 이용하여 뒤로가기 버튼을 누르면 MainActivity로 돌아가게 만듦
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}