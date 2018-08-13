package com.bori.hipe.controllers.fragments.root

import android.animation.Animator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.res.ResourcesCompat.getColor
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import com.bori.hipe.HipeApplication
import com.bori.hipe.R
import com.bori.hipe.controllers.activities.MainActivity
import com.bori.hipe.controllers.activities.SignInActivity
import com.bori.hipe.controllers.crypto.encode
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.controllers.rest.service.UserRegistrationService
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.controllers.views.CircularRevealFrameLayout
import com.bori.hipe.controllers.views.FlippingEdgesView
import com.bori.hipe.util.Const
import com.bori.hipe.util.Status
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView
import com.jaredrummler.materialspinner.MaterialSpinner
import java.util.*

class LoginFragment : HipeBaseFragment() , View.OnClickListener{

    companion object {
        private const val TAG = "LoginFragment.kt"
        private const val LOGIN_USER_REQ_ID = 8L
        private const val REGISTER_REQ_USER_ID = 9L
        private const val SNACK_BAR_ANIMATION_DURATION = 3000
        private const val LIFT_ANIMATION_DURATION = 250L
        private const val WINDOW_ELEMENT_ANIMATION_DURATION = 200L
        private const val WINDOW_ELEMENT_ANIMATION_DELAY = 100L
    }

    //Main views
    private lateinit var loginButton: FlippingEdgesView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var restCallback: LoginActivityRestCallbackAdapter
    private lateinit var createAccountText: View
    private lateinit var mainLayout: View
    private lateinit var contentLayout: View
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var username: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var password: TextInputEditText
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var confirmPassword: TextInputEditText
    private lateinit var mainRevealFrameLayout: CircularRevealFrameLayout
    private lateinit var snackbar: Snackbar
    private lateinit var liftAnimationListener: LiftAnimationListener
    private lateinit var materialSpinner: MaterialSpinner

    //Window views
    private lateinit var loadingEmailConfirmationView:FlippingEdgesView
    private lateinit var windowCard:View
    private lateinit var tintView: View
    private lateinit var photoAndGenderLayout:View
    private lateinit var windowRootView:View
    private lateinit var windwowRootCircularRevavalView: CircularRevealFrameLayout
    private lateinit var userPhoto:ImageView
    private lateinit var userMailLayout:TextInputLayout
    private lateinit var userMailEditText:TextInputEditText
    private lateinit var confirmButton:FloatingActionButton
    private lateinit var privacyCheckBox:CheckBox
    private lateinit var updatesCheckBox:CheckBox
    private val windowInputsViews = LinkedList<View>()
    private val windowLoadingsViews = LinkedList<View>()

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
        initMainLayout()
        initWindowLayout()
    }

    private fun initMainLayout(){

        mainLayout = findViewById(R.id.main_coordinator_layout)
        contentLayout = findViewById(R.id.content_coordinator_layout)

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

        createAccountText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                createAccountText.visibility = View.GONE
                mainRevealFrameLayout.showIn(event = event)
                loginButton.show(false)
                loginButton.changeText(getString(R.string.sign_up))
                loginButton.colors = getColor(resources,R.color.allowed,null)
                shouldCallOnFragment = true

            }
            return@setOnTouchListener true
        }
        createAccountText.setOnClickListener(this)

        loginButton.setOnClickListener(this)
        restCallback = LoginActivityRestCallbackAdapter()
        snackbar = Snackbar.make(mainLayout, R.string.no_connection_detected, Snackbar.LENGTH_LONG)
        snackbar.duration = SNACK_BAR_ANIMATION_DURATION

        liftAnimationListener = LiftAnimationListener()
        snackbar.setAction(R.string.dismiss) {
            snackbar.dismiss()
            contentLayout.animate().translationY(0f).setStartDelay(0).setDuration(LIFT_ANIMATION_DURATION).setListener(null).start()
        }

    }


    private fun initWindowLayout(){

        tintView = findViewById(R.id.tint_view)
        materialSpinner = findViewById(R.id.gender_select_spinner)
        photoAndGenderLayout = findViewById(R.id.photo_and_gender_layout)
        windowRootView = findViewById(R.id.window_container)
        windwowRootCircularRevavalView = findViewById(R.id.window_circular_container)
        userPhoto = findViewById(R.id.user_photo)
        userMailEditText = findViewById(R.id.email)
        userMailLayout = findViewById(R.id.email_input_layout)
        confirmButton = findViewById(R.id.confirm_registration_button)
        privacyCheckBox = findViewById(R.id.privacy_check_box)
        updatesCheckBox = findViewById(R.id.receive_email_check_box)
        windowCard = findViewById(R.id.window_card)
        loadingEmailConfirmationView = findViewById(R.id.email_confirmation_loading_button)

        windowInputsViews.add(updatesCheckBox)
        windowInputsViews.add(privacyCheckBox)
        windowInputsViews.add(userMailLayout)
        windowInputsViews.add(photoAndGenderLayout)

        windowLoadingsViews.add(loadingEmailConfirmationView)

        materialSpinner.setItems("M","W")
        tintView.setOnClickListener(this)
        confirmButton.setOnClickListener(this)
        windwowRootCircularRevavalView.onUpdate = {
            tintView.alpha = 0.9f* if (windwowRootCircularRevavalView.showForward)
                it else 1f - it
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
            ignoreConfirmation: Boolean = false

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
                confirmPasswordInputLayout.error = ""
            }
        } else
            result = false

        return result
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "LoginFragment onResume()")
        loadingEmailConfirmationView.show(true)

    }

    override fun onStop() {
        super.onStop()
        activity?.finish()
    }

    inner class LoginActivityRestCallbackAdapter : RestCallbackAdapter() {

        override fun onSimpleResponse(requestID: Long, response: Any?, serverCode: Int) {
            Log.d(TAG, " onSimple Response with code:  $serverCode; Response : $response")

            when (requestID) {
                LOGIN_USER_REQ_ID -> if (serverCode == Status.OK) {
                    HipeApplication.sharedPreferences.edit().putString(Const.USER_TOKEN, response as String).apply()
                    startActivity(Intent(context, MainActivity::class.java))
                } else if (serverCode == Status.NOT_FOUND) {
                    loginButton.circleColor = resources.getColor(R.color.colorAccent)
                    loginButton.stopLoading()
                    contentLayout.animate()
                            .translationY(-48f * HipeApplication.pixelsPerDp)
                            .setListener(liftAnimationListener)
                            .setDuration(LIFT_ANIMATION_DURATION)
                            .setStartDelay(0)
                            .start()

                    snackbar.setText(getString(R.string.invalid_credentials)).show()
                }
                REGISTER_REQ_USER_ID -> if (serverCode == Status.CREATED) {
                    HipeApplication.sharedPreferences.edit().putString(Const.USER_TOKEN, response as String).apply()
                    showWindow()
                } else if (serverCode == Status.CONFLICT) {
                    loginButton.circleColor = resources.getColor(R.color.colorAccent)
                    loginButton.stopLoading()
                    usernameInputLayout.error = getString(R.string.user_already_exists)
                }

                else -> {
                    loginButton.circleColor = resources.getColor(R.color.colorAccent)
                    loginButton.stopLoading()
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
            if (activity is MainActivity) {
                (activity as MainActivity).showNextFragment(HipeBaseFragment())
            }
        }

    }

    private fun showWindow(){

        windowRootView.visibility = View.VISIBLE
        windwowRootCircularRevavalView.showIn()
        loadingEmailConfirmationView.colors = resources.getColor(R.color.colorAccent)
        loadingEmailConfirmationView.show(true)

    }

    override fun onClick(v: View) {

        Log.d(TAG, "onClick() called with: v = [$v]")

        when (v.id) {

            R.id.login_button -> {
                Log.d(TAG, "onClick: Data Validated!!!")
                loginButton.startLoading()
                if (createAccountText.visibility != View.VISIBLE) {
                    UserRegistrationService.registerUserStepOne(
                            requestID = REGISTER_REQ_USER_ID,
                            username = username.text.toString(),
                            password = encode(password.text.toString()
                            )

                    )
                } else
                    UserService.loginUser(
                            requestID = LOGIN_USER_REQ_ID,
                            username = username.text.toString(),
                            password = encode(password.text.toString())
                    )

            }

            R.id.sign_in_user_button -> startActivity(Intent(context, SignInActivity::class.java))
            R.id.confirm_registration_button-> hideWindowViews()
            R.id.email_confirmation_loading_button -> {
                loadingEmailConfirmationView.startLoading()}
            else ->{}
        }

    }

    private fun hideWindowViews(){

        windowInputsViews.forEachIndexed{ index, view ->
            view.animate().setInterpolator { duration ->
                view.pivotY = view.width/2*( 1 - duration)
                view.scaleX = 1f - duration/2
                view.scaleY = 1f -duration/2
                view.alpha = 1f - duration

                duration
            }.setDuration(WINDOW_ELEMENT_ANIMATION_DURATION).setStartDelay(index* WINDOW_ELEMENT_ANIMATION_DELAY).start()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val hasToShow = validateData(ignoreConfirmation = true)
        loginButton.show(hasToShow) {
            it.changeText(getString(R.string.sign_in))
        }
        loginButton.colors = resources.getColor(R.color.colorAccent)
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

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }

    inner class LiftAnimationListener : Animator.AnimatorListener {

        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            if (contentLayout.translationY == -48f * HipeApplication.pixelsPerDp) {
                contentLayout.animate().translationY(0f).startDelay = SNACK_BAR_ANIMATION_DURATION.toLong()
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
        }

    }

}