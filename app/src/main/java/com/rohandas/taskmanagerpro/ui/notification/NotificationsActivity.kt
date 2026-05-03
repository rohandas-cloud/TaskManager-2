package com.rohandas.taskmanagerpro.ui.notification

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rohandas.taskmanagerpro.R
import com.rohandas.taskmanagerpro.utils.NavigationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {
    private lateinit var adapter: NotificationAdapter
    private var registration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val rv = findViewById<RecyclerView>(R.id.rvNotifications)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(emptyList())
        rv.adapter = adapter

        NavigationHelper.setupBottomMenu(this)
        listenForNotifications()
    }

    private fun listenForNotifications() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        registration = FirebaseFirestore.getInstance().collection("notifications")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching notifications: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val notifications = snapshot.toObjects(NotificationModel::class.java)
                    adapter.updateData(notifications)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        registration?.remove()
    }
}
