package com.rohandas.taskmanagerpro.data.repository

import android.util.Log
import com.rohandas.taskmanagerpro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val dbRef = database.getReference("users")

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user!!)
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email."))
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email or password."))
        } catch (e: com.google.firebase.FirebaseNetworkException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, pass: String, name: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user!!
            
            // Save user to Firestore
            val userModel = User(
                uid = user.uid,
                email = email,
                displayName = name
            )
            firestore.collection("users").document(user.uid).set(userModel).await()
            Log.d("Firestore", "Successfully registered user in Firestore: ${user.uid}")
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e("Firestore", "Error registering user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Checks if the Realtime Database is accessible by writing a temporary "ping" value.
     */
    suspend fun checkRealtimeDatabaseConnection(): Result<Boolean> = try {
        val pingRef = database.getReference(".info/connected")
        // We can't use await() easily on .info/connected as it's a listener,
        // so we'll try a simple write to a test node.
        val testRef = database.getReference("connection_test").child(currentUser?.uid ?: "anonymous")
        testRef.setValue(System.currentTimeMillis()).await()
        Log.d("RTDB", "Realtime Database connection check successful.")
        Result.success(true)
    } catch (e: Exception) {
        Log.e("RTDB", "Realtime Database connection failed: ${e.message}")
        Result.failure(e)
    }

    /**
     * Updates the FCM token in the Realtime Database for the current user.
     */
    suspend fun updateFcmToken(token: String): Result<Unit> = try {
        val uid = currentUser?.uid ?: throw Exception("User not logged in")
        
        // Update in Realtime Database (as requested)
        database.getReference("users").child(uid).child("fcmToken").setValue(token).await()
        
        // Also update in Firestore to keep data consistent
        firestore.collection("users").document(uid).update("fcmToken", token).await()
        
        Log.d("FCM", "Successfully updated FCM token in RTDB and Firestore")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FCM", "Error updating FCM token: ${e.message}")
        Result.failure(e)
    }

    fun logout() {
        auth.signOut()
    }
}
