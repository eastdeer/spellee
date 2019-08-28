package com.charlesma.spellee.login.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.map
import com.charlesma.spellee.R
import com.charlesma.spellee.login.data.LoginRepository
import com.charlesma.spellee.login.data.Result
import com.charlesma.spellee.util.AnalyticsUtil
import com.google.firebase.analytics.FirebaseAnalytics


class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm
    val _emailExistingLiveData: MutableLiveData<Result<Boolean>> get() = loginRepository.emailExistingLiveData
    val emailExistingLiveData: LiveData<Result<Boolean>> get() = _emailExistingLiveData

    fun loginOrSignup(username: String, password: String): LiveData<LoginResult> {
        val emailExistingValue = _emailExistingLiveData.value
        if (emailExistingValue is Result.Success && emailExistingValue?.data) {

            AnalyticsUtil.logEvent(FirebaseAnalytics.Event.LOGIN)
            return loginRepository.login(username, password).map { result ->

                if (result is Result.Success) {
                    LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
                } else {
                    LoginResult(error = R.string.login_failed)
                }

            }

        } else {
            AnalyticsUtil.logEvent(FirebaseAnalytics.Event.SIGN_UP)

            return loginRepository.signup(username, password).map { result ->

                if (result is Result.Success) {
                    LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
                } else {
                    LoginResult(error = R.string.login_failed)
                }

            }
        }
    }

    fun loginDataChanged(username: String, password: String) {

        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
            if (username.isNullOrBlank()) {
                _emailExistingLiveData.postValue(Result.Error(NullPointerException()))
            }
        } else {
            checkEmailExisting(username)
            if (!isPasswordValid(password)) {
                _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
            } else {
                _loginForm.value = LoginFormState(isDataValid = true)
            }
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun checkEmailExisting(email: String) = loginRepository.checkEmailExisting(email)
}
