package com.example.taskmanagerpro.ui.notification

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagerpro.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private var notifications: List<NotificationModel>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvNotifTitle)
        val message: TextView = view.findViewById(R.id.tvNotifMessage)
        val time: TextView = view.findViewById(R.id.tvNotifTime)
        val indicator: View = view.findViewById(R.id.viewTypeIndicator)
    }

    fun updateData(newList: List<NotificationModel>) {
        notifications = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val n = notifications[position]
        holder.title.text = n.title
        holder.message.text = n.message
        holder.time.text = formatTime(n.timestamp)

        val color = when (n.type) {
            "success" -> "#4CAF50"
            "warning" -> "#FF9800"
            "error"   -> "#F44336"
            else      -> "#2196F3"
        }
        holder.indicator.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    override fun getItemCount(): Int = notifications.size

    private fun formatTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000         -> "Just now"
            diff < 3_600_000      -> "${diff / 60_000}m ago"
            diff < 86_400_000     -> "${diff / 3_600_000}h ago"
            else                  -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
        }
    }
}