package com.bori.hipe.controllers.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bori.hipe.R
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.util.web.Status
import kotlinx.android.synthetic.main.activity_sign_in.*

//https://developers.facebook.com/docs/facebook-login/android/

private const val CHECK_USER_EXISTENCE_ID = 3

private const val TAG = "SignInActivity"
private const val SEINING_AND_SCALING_DURATION = 200L
private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1345566
private const val RC_CHOOSE_FROM_GALLERY = 54321

class SignInActivity : AppCompatActivity() {

    var nickName: String = "Hiper"
    var name: String = "Donald"
    var status: String = "let's hipe"

    private var selectedPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "SignInActivity.onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)
        init()
        RestCallbackRepository.registerCallback(restCallback)

    }

    override fun onDestroy() {
        Log.d(TAG, "SignInActivity.onDestroy")
        super.onDestroy()
        RestCallbackRepository.unregisterCallback(restCallback)
    }

    private fun init() {
        Log.d(TAG, "SignInActivity.init")
        ok_button.setOnClickListener(this::onClick)
    }

    @SuppressLint("NewApi")
    private fun validate(): Boolean {
        Log.d(TAG, "SignInActivity.validate")
        Log.d(TAG, "validate() called")

        var validated = true
        val okVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

        val nameSurname = name_surname.text.toString()
        if (nameSurname.isEmpty() || nameSurname.split("\\s+".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().size != 2 || nameSurname.length < 4 || nameSurname.length > 20) {
            if (okVersion)
                name_surname.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483838591_1_9, 0)

            name_surname_hint.visibility = View.VISIBLE
            name_surname_hint.text = "Введите имя и фамилию, не менее 4-ех символов"

            validated = false
        } else {
            if (okVersion) {
                name_surname.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483821583_checkmark_24, 0)
            }
            name_surname_hint.visibility = GONE
        }

        val nickName = nickname.text.toString()
        if (nickName.length < 4) {
            if (okVersion)
                nickname.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483838591_1_9, 0)
            nickname_hint.visibility = View.VISIBLE
            nickname_hint.text = "Введите не менее 4-ех символов"

            validated = false
        } else {
            if (okVersion) {
                nickname.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483821583_checkmark_24, 0)
            }
            nickname_hint.visibility = GONE
        }
        val password = password.text.toString()
        if (password.length < 8 || password.length > 40) {
            if (okVersion)
                this.password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483838591_1_9, 0)

            password_hint.visibility = View.VISIBLE
            password_hint.text = "Введите не менее 8-ми символов"

            validated = false
        } else {
            if (okVersion)
                this.password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483821583_checkmark_24, 0)

            password_hint.visibility = GONE
        }

        val confirmPassword = confirm_password.text.toString()

        if (confirmPassword.length < 8 || confirmPassword.length > 40) {
            if (okVersion)
                confirm_password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483838591_1_9, 0)

            password_hint.text = "Введите не менее, чем 8 символов"
            validated = false
        } else {
            if (okVersion)
                confirm_password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483821583_checkmark_24, 0)

            if (confirmPassword != password) {

                if (okVersion) {
                    this.password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483838591_1_9, 0)
                    confirm_password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483838591_1_9, 0)
                }
                password_hint.visibility = View.VISIBLE
                password_hint.text = "Пароли не равны"
                validated = false
            } else {
                if (okVersion) {
                    this.password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483821583_checkmark_24, 0)
                    confirm_password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483821583_checkmark_24, 0)
                }
                password_hint.visibility = GONE

            }
        }

        return validated
    }

    private fun choosePhotoFromGallery() {
        Log.d(TAG, "SignInActivity.choosePhotoFromGallery")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return
            }
        }

        val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, RC_CHOOSE_FROM_GALLERY)
    }

    private fun mainTintViewAnimateOn() {
        Log.d(TAG, "SignInActivity.mainTintViewAnimateOn")
        spining_scale_view_progress_main.catchContext().start(SEINING_AND_SCALING_DURATION, main_tint_view)
    }

    private fun mainTintViewAnimateOff() {
        Log.d(TAG, "SignInActivity.mainTintViewAnimateOff")
        spining_scale_view_progress_main.catchContext().stopAndRelease()
    }

    private fun stopImageLoadingAnimation() {
        Log.d(TAG, "SignInActivity.stopImageLoadingAnimation")
    }

    private fun buildAndRegisterUser() {
        Log.d(TAG, "SignInActivity.buildAndRegisterUser")
    }

    private val restCallback = object : RestCallback() {

        var photoCreationsCount = 0

        override fun onSimpleResponse(requestID: Int, response: Any?, responseStatus: Int) {
            Log.d(TAG, "SignInActivity.onSimpleResponse")
            Log.d(TAG, "onSimpleResponse: $responseStatus")

            when (responseStatus) {
                Status.FOUND -> {
                    Log.e(TAG, "onSimpleResponse: USER_ALREADY_EXIST")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        nickname.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483838591_1_9, 0)

                    nickname_hint.visibility = View.VISIBLE
                    nickname_hint.text = "Такой никнейм уже существует"

                }
                Status.NOT_FOUND -> {
                    Log.e(TAG, "onSimpleResponse: CONST_STATUS_USER_NOT_EXISTS")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        nickname.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_1483821583_checkmark_24, 0)

                    nickname_hint.visibility = GONE
                    selectedPage++

                }
            }
        }

        override fun onFailure(requestID: Int, t: Throwable) {
            Log.d(TAG, "SignInActivity.onFailure")
            Log.d(TAG, "onFailure() called with: t = [$t]")
            Toast.makeText(this@SignInActivity, "Error!", Toast.LENGTH_SHORT).show()
            mainTintViewAnimateOff()
            stopImageLoadingAnimation()
        }

    }

    fun onClick(v: View) {
        Log.d(TAG, "SignInActivity.onClick")

        when (v.id) {

            R.id.ok_button -> if (validate()) {
                Log.e(TAG, "onClick: ")
                if (selectedPage == 0) {

                    Log.d(TAG, "onClick: Check user Existence")
                    mainTintViewAnimateOn()
                    UserService.checkUserExistence(CHECK_USER_EXISTENCE_ID, nickname.text.toString())

                } else if (selectedPage == 1) {
                }
            }

            else -> {
            }
        }

    }

    override fun onBackPressed() {
        Log.d(TAG, "SignInActivity.onBackPressed")

        if (selectedPage != 0) {
            selectedPage--

        } else
            super.onBackPressed()

    }

}