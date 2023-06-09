package com.example.waru

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView

class LoadingAni(private val loadingTextView: TextView) {
    private lateinit var animation: AlphaAnimation

    // 일기 작성 시 gpt-3.5-turbo로 부터 코멘트를 받아오는 동안 보여지는 애니메이션 
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
        //변수가 초기화 되어있으면 애니메이션 취소
        if (::animation.isInitialized) {
            animation.cancel()
        }
    }

    fun startLoding(){
        animateLoadingText()
    }
}
