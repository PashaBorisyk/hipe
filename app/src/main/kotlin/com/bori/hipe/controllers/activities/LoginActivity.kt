package com.bori.hipe.controllers.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bori.hipe.R
import com.bori.hipe.controllers.crypto.encode
import com.bori.hipe.controllers.receiver.BootReciever
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.util.Const
import com.bori.hipe.util.Status
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


private const val TAG = "LoginActivity"
private const val LOGIN_USER_ID = 8L

class LoginActivity : Activity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var restCallback: LoginActivityRestCallbackAdapter

    private lateinit var snackbar:Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
        RestService.registerCallback(restCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        RestService.unregisterCallback(restCallback)
    }

    private fun init() {
        Log.d(TAG, "init() called")
        restCallback = LoginActivityRestCallbackAdapter()
        snackbar = Snackbar.make(findViewById<View>(R.id.main_coordinator_layout), R.string.no_connection_detected , Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(R.string.dismiss){
            snackbar.dismiss()
        }
        snackbar.show()

        sharedPreferences = getSharedPreferences(Const.HIPE_APPLICATION_SHARED_PREFERENCES, MODE_PRIVATE)

    }

    private fun setInputsValidator(){
        password.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                s?:return

                if(s.length > password_input_layout.counterMaxLength)
                    password_input_layout.error = resources.getString(R.string.max_length_msg)
                else
                    password_input_layout.error = ""

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    private fun validateData(): Boolean {
        Log.d(TAG, "validateData() called")

        if (!BootReciever.isConnected) {
            return false
        }

        if (password.text.length < 8 || login.text.length < 10)
            return false

        return true
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    inner class LoginActivityRestCallbackAdapter : RestCallbackAdapter() {

        override fun onSimpleResponse(requestID: Long, response: Any?, serverCode: Int) {
            Log.d(TAG, " onSimple Response with code:  $serverCode")

            when (requestID) {
                LOGIN_USER_ID -> if (serverCode == Status.OK) {
                    val editor = sharedPreferences.edit()
                    editor.putLong(Const.USER_ID, response as Long).apply()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))

                } else -> {}
            }
        }

        override fun onOk(requestID: Long) {
            Log.d(TAG, "onOk() called")
        }

        override fun onFailure(requestID: Long, t: Throwable) {
            Log.d(TAG, "onFailure() called with: t = [$t]")
            Toast.makeText(this@LoginActivity, "Ошибка входа. Повторите позже", Toast.LENGTH_SHORT).show()
        }

    }

    private val myOnClickListener = {
        v: View -> Unit

        Log.d(TAG, "onClick() called with: v = [$v]")

        when (v.id) {

            R.id.login_button -> if (validateData()) {
                Log.d(TAG, "onClick: Data Validated!!!")
                UserService.loginUser(
                        requestID = LOGIN_USER_ID,
                        nickName = login.text.toString(),
                        password = Arrays.toString(encode(password.text.toString()))
                )

            }

            R.id.sign_in_user_button -> startActivity(Intent(this, SignInActivity::class.java))

        }

    }

}