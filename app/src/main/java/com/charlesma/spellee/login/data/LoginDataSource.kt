package com.charlesma.spellee.login.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charlesma.spellee.login.data.model.LoggedInUser
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.IOException


/**
 * Class that handles authentication w/ loginOrSignup credentials and retrieves user information.
 */
class LoginDataSource {

    val mAuth = FirebaseAuth.getInstance()
    val loginLivedata = MutableLiveData<Result<LoggedInUser>>()

    val emailExistingLiveData = MutableLiveData<Result<Boolean>>()

    val logginUser : FirebaseUser? get() = mAuth.currentUser

    fun checkEmailExisting(email: String) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.signInMethods?.isNotEmpty() == true) {
                    emailExistingLiveData.postValue(Result.Success(true))
                } else {
                    emailExistingLiveData.postValue(Result.Success(false))
                }
            } else {
                emailExistingLiveData.postValue(Result.Error(IOException(it.exception)))
            }
        }
    }

    fun login(email: String, password: String) = loginLivedata.apply {
        try {
            // TODO: handle loggedInUser authentication
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            // Log.d(TAG, "signInWithEmail:success")
                            val user = mAuth.getCurrentUser()
                            user?.apply {
                                loginLivedata.postValue(
                                    Result.Success(
                                        LoggedInUser(
                                            uid,
                                            displayName ?: this.uid
                                        )
                                    )
                                )
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            // Log.w(TAG, "signInWithEmail:failure", task.exception)
//                            Toast.makeText(
//                                this@EmailPasswordActivity, "Authentication failed.",
//                                Toast.LENGTH_SHORT
//                            ).show()
                            loginLivedata.postValue(
                                Result.Error(
                                    IOException(
                                        "Error logging in",
                                        null
                                    )
                                )
                            )
                        }

                        // ...
                    })
        } catch (e: Throwable) {
            loginLivedata.postValue(Result.Error(IOException("Error logging in", e)))
        }
    }

    fun signup(email: String, password: String) = loginLivedata.apply {
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            // Log.d(TAG, "signInWithEmail:success")
                            val user = mAuth.currentUser
                            user?.apply {
                                loginLivedata.postValue(
                                    Result.Success(
                                        LoggedInUser(uid, displayName ?: this.uid)
                                    )
                                )
                            }
                        } else {
                            loginLivedata.postValue(
                                Result.Error(
                                    IOException("Error logging in", null)
                                )
                            )
                        }
                    })
        } catch (e: Throwable) {
            loginLivedata.postValue(Result.Error(IOException("Error logging in", e)))
        }
    }

    fun logout() {
        mAuth.signOut()
    }

    fun resetPassword(email:String){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task->

        }
    }
}

