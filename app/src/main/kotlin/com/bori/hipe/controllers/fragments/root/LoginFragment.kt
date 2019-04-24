package com.bori.hipe.controllers.fragments.root

import android.animation.Animator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat.getColor
import com.bori.hipe.MainApplication
import com.bori.hipe.R
import com.bori.hipe.controllers.activities.MainActivity
import com.bori.hipe.controllers.activities.SignInActivity
import com.bori.hipe.controllers.crypto.encode
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.controllers.rest.routes.Route
import com.bori.hipe.controllers.rest.service.UserRegistrationService
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.controllers.views.CircularRevealFrameLayout
import com.bori.hipe.controllers.views.CounterView
import com.bori.hipe.controllers.views.FlippingEdgesView
import com.bori.hipe.util.Const
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView
import com.bori.hipe.util.web.Status
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jaredrummler.materialspinner.MaterialSpinner
import java.util.*

class LoginFragment : HipeBaseFragment(), View.OnClickListener {

    companion object {
        private const val TAG = "LoginFragment.kt"
        private const val LOGIN_USER_REQ_ID = 8
        private const val REGISTER_STEP_ONE_REQ_USER_ID = 9
        private const val REGISTER_STEP_TWO_REQ_USER_ID = 10
        private const val SNACK_BAR_ANIMATION_DURATION = 3000
        private const val LIFT_ANIMATION_DURATION = 250L
        private const val WINDOW_ELEMENT_ANIMATION_DURATION = 200L
        private const val WINDOW_ELEMENT_ANIMATION_DELAY = 100L
    }

    //Main views
    private lateinit var loginButton: FlippingEdgesView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var restCallback: LoginActivityRestCallback
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
    private lateinit var counterView: CounterView
    private lateinit var windowCard: View
    private lateinit var tintView: View
    private lateinit var photoAndGenderLayout: View
    private lateinit var windowRootView: View
    private lateinit var windwowRootCircularRevavalView: CircularRevealFrameLayout
    private lateinit var userPhoto: ImageView
    private lateinit var userMailLayout: TextInputLayout
    private lateinit var userMailEditText: TextInputEditText
    private lateinit var confirmButton: FloatingActionButton
    private lateinit var privacyCheckBox: CheckBox
    private lateinit var updatesCheckBox: CheckBox
    private lateinit var countDown: TextView
    private val windowInputsViews = LinkedList<View>()
    private val windowLoadingsViews = LinkedList<View>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "LoginFragment.onCreateView")
        super.onCreateView(inflater, container, savedInstanceState)
        setContentView(R.layout.fragment_login, inflater, container)
        init()
        setInputsValidator()
        RestCallbackRepository.registerCallback(restCallback)
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "LoginFragment.onDestroy")
        super.onDestroy()
        RestCallbackRepository.unregisterCallback(restCallback)
    }

    private fun init() {
        Log.d(TAG, "LoginFragment.init")
        Log.d(TAG, "init() called")
        initMainLayout()
        initWindowLayout()
    }

    private fun initMainLayout() {
        Log.d(TAG, "LoginFragment.initMainLayout")

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

        countDown = findViewById(R.id.count_down)

        mainRevealFrameLayout.child = confirmPasswordInputLayout

        loginButton.mainText = getString(R.string.sign_in)

        createAccountText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                createAccountText.visibility = View.GONE
                mainRevealFrameLayout.showIn(event = event)
                loginButton.show(false)
                loginButton.changeText(getString(R.string.sign_up))
                loginButton.colors = getColor(resources, R.color.allowed, null)
                shouldCallOnFragment = true

            }
            return@setOnTouchListener true
        }
        createAccountText.setOnClickListener(this)

        loginButton.setOnClickListener(this)
        restCallback = LoginActivityRestCallback()
        snackbar = Snackbar.make(mainLayout, R.string.no_connection_detected, Snackbar.LENGTH_LONG)
        snackbar.duration = BaseTransientBottomBar.LENGTH_LONG

        liftAnimationListener = LiftAnimationListener()
        snackbar.setAction(R.string.dismiss) {
            snackbar.dismiss()
            contentLayout.animate().translationY(0f).setStartDelay(0).setDuration(LIFT_ANIMATION_DURATION).setListener(null).start()
        }

    }

    private fun initWindowLayout() {
        Log.d(TAG, "LoginFragment.initWindowLayout")

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
        counterView = findViewById(R.id.email_confirmation_counter_view)

        windowInputsViews.add(updatesCheckBox)
        windowInputsViews.add(privacyCheckBox)
        windowInputsViews.add(userMailLayout)
        windowInputsViews.add(photoAndGenderLayout)

        windowLoadingsViews.add(counterView)

        materialSpinner.setItems("M", "W")
        tintView.setOnClickListener(this)
        confirmButton.setOnClickListener(this)
        windwowRootCircularRevavalView.onUpdate = {
            tintView.alpha = 0.9f * if (windwowRootCircularRevavalView.showForward)
                it else 1f - it
        }

        counterView.setOnClickListener(this)

        counterView.start(100000, countDown)


    }

    private fun setInputsValidator() {
        Log.d(TAG, "LoginFragment.setInputsValidator")

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

        Log.d(TAG, "LoginFragment.validateData")
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
        Log.d(TAG, "LoginFragment.onResume")
        super.onResume()
        Log.d(TAG, "LoginFragment onResume()")

    }

    override fun onStop() {
        Log.d(TAG, "LoginFragment.onStop")
        super.onStop()
        activity?.finish()
    }

    inner class LoginActivityRestCallback : RestCallback() {

        override fun onSimpleResponse(requestID: Int, response: Any?, responseStatus: Int) {
            Log.d(TAG, "LoginActivityRestCallback.onSimpleResponse")
            Log.d(TAG, " onSimple Response with code:  $responseStatus; Response : $response")

            when (requestID) {
                LOGIN_USER_REQ_ID -> if (responseStatus == Status.OK) {
                    MainApplication.sharedPreferences?.edit()?.putString(Route.TOKEN, response as String)?.apply()
                    startActivity(Intent(context, MainActivity::class.java))
                } else if (responseStatus == Status.NOT_FOUND) {
                    loginButton.circleColor = resources.getColor(R.color.colorAccent)
                    loginButton.stopLoading()
                    contentLayout.animate()
                            .translationY(-48f * MainApplication.pixelsPerDp)
                            .setListener(liftAnimationListener)
                            .setDuration(LIFT_ANIMATION_DURATION)
                            .setStartDelay(0)
                            .start()

                    snackbar.setText(getString(R.string.invalid_credentials)).show()
                }
                REGISTER_STEP_ONE_REQ_USER_ID -> if (responseStatus == Status.CREATED) {
                    MainApplication.sharedPreferences?.edit()?.putString(Const.USER_PUBLIC_TOKEN, response as String)?.apply()
                    showWindow()
                } else if (responseStatus == Status.CONFLICT) {
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

        override fun onFailure(requestID: Int, t: Throwable) {
            Log.d(TAG, "LoginActivityRestCallback.onFailure")
            Log.d(TAG, "onFailure() called with: t = [$t]")
            loginButton.circleColor = resources.getColor(R.color.colorAccent)
            loginButton.stopLoading()
            snackbar.setText(getString(R.string.cannot_obtain_connection_message)).show()
            if (activity is MainActivity) {
                (activity as MainActivity).showNextFragment(HipeBaseFragment())
            }
        }

    }

    private fun showWindow() {
        Log.d(TAG, "LoginFragment.showWindow")

        windowRootView.visibility = View.VISIBLE
        windwowRootCircularRevavalView.showIn()

    }

    override fun onClick(v: View) {
        Log.d(TAG, "LoginFragment.onClick")

        Log.d(TAG, "onClick() called with: v = [$v]")

        when (v.id) {

            R.id.login_button -> {
                Log.d(TAG, "onClick: Data Validated!!!")
                loginButton.startLoading()
                if (createAccountText.visibility != View.VISIBLE) {
                    UserRegistrationService.registerUserStepOne(
                            requestID = REGISTER_STEP_ONE_REQ_USER_ID,
                            username = username.text.toString(),
                            email = username.text.toString(),
                            password = encode(password.text.toString()
                            )

                    )
                } else
                    UserService.login(
                            requestID = LOGIN_USER_REQ_ID,
                            username = username.text.toString(),
                            password = encode(password.text.toString())
                    )

            }

            R.id.sign_in_user_button -> startActivity(Intent(context, SignInActivity::class.java))
            R.id.confirm_registration_button -> hideWindowViews()
            R.id.email_confirmation_counter_view -> {
                counterView.done(isSuccessful = true)
            }
            else -> {
            }
        }

    }

    private fun hideWindowViews() {
        Log.d(TAG, "LoginFragment.hideWindowViews")

        windowInputsViews.forEachIndexed { index, view ->
            view.animate().setInterpolator { duration ->
                view.pivotY = view.width / 2 * (1 - duration)
                view.scaleX = 1f - duration / 2
                view.scaleY = 1f - duration / 2
                view.alpha = 1f - duration

                duration
            }.setDuration(WINDOW_ELEMENT_ANIMATION_DURATION)
                    .setStartDelay(index * WINDOW_ELEMENT_ANIMATION_DELAY).start()
        }

    }

    override fun onBackPressed() {
        Log.d(TAG, "LoginFragment.onBackPressed")
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
        confirmPassword.text?.clear()

    }

    inner class TextUpdateListener : TextWatcher {
        override fun afterTextChanged(text: Editable?) {
            Log.d(TAG, "TextUpdateListener.afterTextChanged")
            loginButton.show(
                    validateData(
                            ignoreConfirmation =
                            mainRevealFrameLayout.state == CircularRevealFrameLayout.State.IS_SHOWING
                    )
            )
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.d(TAG, "TextUpdateListener.beforeTextChanged")
        }


        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.d(TAG, "TextUpdateListener.onTextChanged")
        }


    }

    inner class LiftAnimationListener : Animator.AnimatorListener {

        override fun onAnimationRepeat(animation: Animator?) {
            Log.d(TAG, "LiftAnimationListener.")
        }


        override fun onAnimationEnd(animation: Animator?) {
            Log.d(TAG, "LiftAnimationListener.onAnimationEnd")
            if (contentLayout.translationY == -48f * MainApplication.pixelsPerDp) {
                contentLayout.animate().translationY(0f).startDelay = SNACK_BAR_ANIMATION_DURATION.toLong()
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
            Log.d(TAG, "LiftAnimationListener.onAnimationCancel")
        }


        override fun onAnimationStart(animation: Animator?) {
            Log.d(TAG, "LiftAnimationListener.onAnimationStart")
        }


    }

}