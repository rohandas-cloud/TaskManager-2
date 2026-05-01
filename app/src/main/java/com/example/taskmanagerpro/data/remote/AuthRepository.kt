package com.example.taskmanagerpro.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository - Handles all Firebase Authentication operations.
 *
 * FIXES:
 *  - Added FirebaseCrashlytics.getInstance().recordException(e) inside every
 *    catch block so auth errors are captured in the Crashlytics dashboard.
 */
class AuthRepository {

    private val auth = FirebaseCrashlytics.getInstance().let { FirebaseAuth.getInstance() }
    private val crashlytics = FirebaseCrashlytics.getInstance()

    val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    val isUserAuthenticated: Boolean
        get() = currentUser != null

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("Authentication failed: User not found"))
            }
        } catch (e: FirebaseAuthException) {
            crashlytics.recordException(e)           // <-- Crashlytics fix
            Result.failure(e)
        } catch (e: Exception) {
            crashlytics.recordException(e)           // <-- Crashlytics fix
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("Sign up failed: Unable to create account"))
            }
        } catch (e: FirebaseAuthException) {
            crashlytics.recordException(e)           // <-- Crashlytics fix
            Result.failure(e)
        } catch (e: Exception) {
            crashlytics.recordException(e)           // <-- Crashlytics fix
            Result.failure(e)
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)           // <-- Crashlytics fix
            Result.failure(e)
        }
    }

    fun getCurrentUserEmail(): String? = currentUser?.email
    fun getCurrentUserUid(): String? = currentUser?.uid
}
