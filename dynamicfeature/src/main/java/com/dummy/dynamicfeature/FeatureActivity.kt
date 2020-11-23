package com.dummy.dynamicfeature

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.play.core.splitcompat.SplitCompat

class FeatureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val txt = TextView(this)
        txt.text = "Hello From Dynamic Installed Activity"
        txt.gravity = Gravity.CENTER
        root.addView(txt)
        setContentView(root)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.install(newBase)
    }
}