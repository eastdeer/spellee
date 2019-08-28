package com.charlesma.spellee.login.ui.login

/**
 * Data validation state of the loginOrSignup form.
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)
