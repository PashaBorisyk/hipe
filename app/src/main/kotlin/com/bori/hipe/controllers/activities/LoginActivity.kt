package com.bori.hipe.controllers.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AlertDialog
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

        login_button.setOnClickListener(myOnClickListener)
        sign_in_user_button.setOnClickListener(myOnClickListener)
        fault_hint.visibility = View.GONE

        sharedPreferences = getSharedPreferences(Const.HIPE_APPLICATION_SHARED_PREFERENCES, MODE_PRIVATE)

    }

    private fun validateData(): Boolean {
        Log.d(TAG, "validateData() called")

        if (!BootReciever.isConnected) {

            AlertDialog.Builder(this)
                    .setMessage("Отсутствует подключение к интернету")
                    .setIcon(R.drawable.hipe_dark_56_dp)
                    .setPositiveButton("ok") { dialog: DialogInterface, _ ->
                        Unit
                        dialog.dismiss()
                    }
                    .create().show()

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

                    fault_hint.visibility = View.GONE
                    val editor = sharedPreferences.edit()
                    editor.putLong(Const.USER_ID, response as Long).apply()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))

                } else
                    fault_hint.visibility = View.VISIBLE
            }
        }

        override fun onOk(requestID: Long) {
            Log.d(TAG, "onOk() called")
        }

        override fun onFailure(requestID: Long, t: Throwable) {
            Log.d(TAG, "onFailure() called with: t = [$t]")

            Toast.makeText(this@LoginActivity, "Ошибка входа. Повторите позже", Toast.LENGTH_SHORT).show()
            fault_hint.visibility = View.VISIBLE

        }

    }

    private val myOnClickListener = { v: View ->
        Unit

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