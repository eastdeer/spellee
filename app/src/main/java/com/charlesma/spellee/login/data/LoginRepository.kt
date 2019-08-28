package com.charlesma.spellee.login.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.charlesma.spellee.login.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of loginOrSignup statusColor and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    val logginUser : FirebaseUser? get() = dataSource.logginUser

    val isLoggedIn: Boolean
        get() = logginUser != null

    val emailExistingLiveData: MutableLiveData<Result<Boolean>> get() = dataSource.emailExistingLiveData

    init {
    }

    fun logout() {
        dataSource.logout()
    }

    fun login(username: String, password: String) = dataSource.login(username, password)

    fun signup(username: String, password: String) = dataSource.signup(username, password)


    fun checkEmailExisting(email: String) = dataSource.checkEmailExisting(email)

}
