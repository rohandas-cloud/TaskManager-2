package com.rohandas.taskmanagerpro.ui.calendar

// FIX: Package corrected from "calender" (typo) to "calendar"
// and NavigationHelper import updated to utils package.

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.utils.NavigationHelper

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        NavigationHelper.setupBottomMenu(this)
    }
}
