package com.bori.hipe.controllers.fragments.root

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bori.hipe.R
import com.bori.hipe.controllers.activities.MainActivity
import com.bori.hipe.controllers.activities.SignInActivity
import com.bori.hipe.controllers.crypto.encode
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.controllers.views.CircularRevavalView
import com.bori.hipe.controllers.views.CircularRevealFrameLayout
import com.bori.hipe.controllers.views.FlippingEdgesView
import com.bori.hipe.util.Const
import com.bori.hipe.util.Status
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView
import java.util.*

class LoginFragment : HipeBaseFragment() {

    companion object {
        private const val TAG = "LoginFragment.kt"
        private const val LOGIN_USER_ID = 8L
    }

    private lateinit var loginButton: FlippingEdgesView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var restCallback: LoginActivityRestCallbackAdapter

    private lateinit var createAccountText: View

    private lateinit var circularRevavalView: CircularRevavalView

    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var username: TextInputEditText

    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var password: TextInputEditText

    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var confirmPassword: TextInputEditText

    private lateinit var mainRevealFrameLayout: CircularRevealFrameLayout

    private lateinit var snackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setContentView(R.layout.fragment_login, inflater, container)
        init()
        setInputsValidator()
        RestService.registerCallback(restCallback)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        RestService.unregisterCallback(restCallback)
    }

    private fun init() {
        Log.d(TAG, "init() called")
        createAccountText = findViewById(R.id.create_account_text)
        loginButton = findViewById(R.id.login_button)

        usernameInputLayout = findViewById(R.id.username_input_layout)
        username = findViewById(R.id.username)

        passwordInputLayout = findViewById(R.id.password_input_layout)
        password = findViewById(R.id.password)

        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout)
        confirmPassword = findViewById(R.id.confirm_password)

        createAccountText = findViewById(R.id.create_account_text)
        mainRevealFrameLayout = findViewById(R.id.main_reveal)
        mainRevealFrameLayout.child = confirmPasswordInputLayout

        loginButton.mainText = getString(R.string.sign_in)

        createAccountText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                createAccountText.visibility = View.GONE
                mainRevealFrameLayout.showIn(event = event)
                loginButton.show(false)
                loginButton.changeText(getString(R.string.sign_up))
                loginButton.colors = intArrayOf(Color.GRAY, resources.getColor(R.color.allowed))
                shouldCallOnFragment = true

            }
            return@setOnTouchListener true
        }
        createAccountText.setOnClickListener(myOnClickListener)

        loginButton.setOnClickListener(myOnClickListener)
        restCallback = LoginActivityRestCallbackAdapter()
        snackbar = Snackbar.make(findViewById(R.id.main_coordinator_layout), R.string.no_connection_detected, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(R.string.dismiss) {
            snackbar.dismiss()
        }

    }

    private fun setInputsValidator() {

        val textUpdateListener = TextUpdateListener()
        username.addTextChangedListener(textUpdateListener)
        password.addTextChangedListener(textUpdateListener)
        confirmPassword.addTextChangedListener(textUpdateListener)

    }

    private fun validateData(
            username: Editable? = this.username.editableText,
            password: Editable? = this.password.editableText,
            confirmPassword: Editable? = this.confirmPassword.editableText,
            ignoreConfirmation:Boolean = false

    ): Boolean {

        username ?: return false
        password ?: return false
        confirmPassword ?: return false

        var result = true

        if (username.length > Const.MIN_USERNAME_SIZE) {

            if (username.length > usernameInputLayout.counterMaxLength) {
                usernameInputLayout.error = resources.getString(R.string.max_length_msg) + " ${usernameInputLayout.counterMaxLength}"
                result = false
            } else {
                usernameInputLayout.error = ""
            }
        } else
            result = false

        if (password.length > Const.MIN_USERNAME_SIZE) {
            if (password.length > passwordInputLayout.counterMaxLength) {
                passwordInputLayout.error = resources.getString(R.string.max_length_msg) + " ${passwordInputLayout.counterMaxLength}"
                result = false
            } else {
                passwordInputLayout.error = ""
            }
        } else
            result = false

        if (confirmPasswordInputLayout.visibility != View.VISIBLE || ignoreConfirmation)
            return result

        if (confirmPassword.length > Const.MIN_PASSWORD_SIZE) {

            if (confirmPassword.length > usernameInputLayout.counterMaxLength) {
                confirmPasswordInputLayout.error = resources.getString(R.string.max_length_msg) + " ${usernameInputLayout.counterMaxLength}"
                result = false
            } else {
                usernameInputLayout.error = ""
            }
        } else
            result = false

        return result
    }

    override fun onStop() {
        super.onStop()
        activity?.finish()
    }

    inner class LoginActivityRestCallbackAdapter : RestCallbackAdapter() {

        override fun onSimpleResponse(requestID: Long, response: Any?, serverCode: Int) {
            Log.d(TAG, " onSimple Response with code:  $serverCode")

            when (requestID) {
                LOGIN_USER_ID -> if (serverCode == Status.OK) {
                    val editor = sharedPreferences.edit()
                    editor.putLong(Const.USER_ID, response as Long).apply()
                    startActivity(Intent(context, MainActivity::class.java))

                }
                else -> {
                }
            }
        }

        override fun onOk(requestID: Long) {
            loginButton.circleColor = resources.getColor(R.color.allowed)
            loginButton.stopLoading()
            Log.d(TAG, "onOk() called")
        }

        override fun onFailure(requestID: Long, t: Throwable) {
            Log.d(TAG, "onFailure() called with: t = [$t]")
            loginButton.circleColor = resources.getColor(R.color.colorAccent)
            loginButton.stopLoading()
            snackbar.setText(getString(R.string.cannot_obtain_connection_message)).show()
        }

    }

    private val myOnClickListener = { v: View ->
        Unit

        Log.d(TAG, "onClick() called with: v = [$v]")

        when (v.id) {

            R.id.login_button -> {
                Log.d(TAG, "onClick: Data Validated!!!")
                loginButton.startLoading()
                UserService.loginUser(
                        requestID = LOGIN_USER_ID,
                        nickName = username.text.toString(),
                        password = Arrays.toString(encode(password.text.toString()))
                )

            }

            R.id.sign_in_user_button -> startActivity(Intent(context, SignInActivity::class.java))
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val hasToShow = validateData(ignoreConfirmation = true)
        loginButton.show(hasToShow){
            it.changeText(getString(R.string.sign_in))
        }
        loginButton.colors = intArrayOf(Color.GRAY, resources.getColor(R.color.colorAccent))
        createAccountText.alpha = 0f
        createAccountText.visibility = View.VISIBLE
        createAccountText.animate().alpha(1f).setDuration(300).start()
        mainRevealFrameLayout.showOut()
        confirmPassword.text.clear()

    }

    inner class TextUpdateListener : TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            loginButton.show(
                    validateData(
                            ignoreConfirmation =
                            mainRevealFrameLayout.state == CircularRevealFrameLayout.State.IS_SHOWING
                    )
            )
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }

}