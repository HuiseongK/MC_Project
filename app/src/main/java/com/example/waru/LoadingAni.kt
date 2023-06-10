package com.example.waru

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoadingAni(private val loadingTextView: TextView) : AppCompatActivity() {
    private lateinit var animation: AlphaAnimation

    private fun animateLoadingText() {
        animation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1000 // 애니메이션 지속 시간 (밀리초)
            repeatCount = Animation.INFINITE // 무한 반복 설정
            repeatMode = Animation.REVERSE // 반복 모드 설정 (역방향으로 반복)
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    loadingTextView.visibility = TextView.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    loadingTextView.text = "당신의 하루는 어땠나요?"
                }

                override fun onAnimationEnd(animation: Animation?) {
                    loadingTextView.visibility = TextView.GONE
                }
            })
        }

        loadingTextView.startAnimation(animation)
    }

    fun stopAnimation() {
        animation.cancel()
    }

    fun startLoding(){
        animateLoadingText()
    }
}
