package com.bori.hipe.controllers.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bori.hipe.R
import com.bori.hipe.controllers.dialogs.FriendsListDialogFragment
import com.bori.hipe.controllers.dialogs.PhotoDialogFragment
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.controllers.rest.service.EventService
import com.bori.hipe.controllers.rest.service.HipeImageService
import com.bori.hipe.models.Event
import com.bori.hipe.models.HipeImage
import com.bori.hipe.models.User
import com.bori.hipe.util.web.Status
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import kotlinx.android.synthetic.main.activity_create_new_event.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CreateNewEventActivity"

private const val CREATE_NEW_EVENT_ID = 0L
private const val UPLOAD_EVENT_PHOTO_ID = 1L

private const val RC_CHOOSE_FROM_GALLERY = 54321
private const val RC_CAPTURE_FROM_CAMERA = 5431
private const val EVENT_IMAGE_TAG = -23
private const val ANIMATION_ADD_USER_DURATION = 100L
private const val SPINIING_SCALE_VIEW_ANIMATION_DURATION = 500L

private const val MILLIS_IN_SECOND = 1000L
private const val SECONDS_IN_MINUTE = 60L
private const val MINUTES_IN_HOUR = 60L
private const val HOURS_IN_DAY = 24L
private const val DAYS_IN_YEAR = 365L
private const val MILLISECONDS_IN_YEAR =
        MILLIS_IN_SECOND *
                SECONDS_IN_MINUTE *
                MINUTES_IN_HOUR *
                HOURS_IN_DAY *
                DAYS_IN_YEAR

private const val ACCESS_FINE_LOCATION_PERMISSION_CODE = 321

private const val PLACES_REQUEST_CODE = 234

class CreateNewEventActivity :
        AppCompatActivity(),
        PhotoDialogFragment.SourceChooser,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private var eventImageLarge: HipeImage? = null
    private var photoLocalUrl: String? = null

    private lateinit var friendsListDialogFragment: FriendsListDialogFragment
    private lateinit var photoDialogFragment: PhotoDialogFragment

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog

    private var dateMills: Long = 0
    private var timeMills: Long = 0

    private var YEAR: Int = 0
    private var MOUNTH: Int = 0
    private var DAY: Int = 0

    private var longtitude = -1.0
    private var latitude = -1.0

    private lateinit var restCallback: CreateEventActivityRestCallbackAdapter

    val addedUsers = hashSetOf<Long>()

    private fun init() {
        Log.d(TAG, "CreateNewEventActivity.init")

        friendsListDialogFragment = FriendsListDialogFragment()
        photoDialogFragment = PhotoDialogFragment()
        photoDialogFragment.setSourceChooser(this)

        button_add_photo.tag = EVENT_IMAGE_TAG
        event_photo.tag = EVENT_IMAGE_TAG
        button_time.text = resources.getString(R.string.default_time)
        switch_is_private_event.isChecked = true
        button_is_private_indicator.isSelected = true
        text_view_event_members_count.text = "+0"
        main_container.removeView(gender_selecters_layout)

        button_add_photo.setOnClickListener(myOnClickListener)
        button_date.setOnClickListener(myOnClickListener)
        button_time.setOnClickListener(myOnClickListener)
        switch_is_private_event.setOnClickListener(myOnClickListener)
        button_add_event_members.setOnClickListener(myOnClickListener)
        text_view_event_members_count.setOnClickListener(myOnClickListener)
//        button_gender_man.setOnClickListener(myOnClickListener)
//        button_gender_woman.setOnClickListener(myOnClickListener)
//        button_event_location.setOnClickListener(myOnClickListener)
//        button_event_at_home_selector.setOnClickListener(myOnClickListener)
//        button_create_new_event.setOnClickListener(myOnClickListener)

        timePickerDialog = TimePickerDialog(this, this, 0, 0, true)
        restCallback = CreateEventActivityRestCallbackAdapter()

        setButtonDate(Calendar.getInstance().timeInMillis)

    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "CreateNewEventActivity.onCreate")

        setContentView(R.layout.activity_create_new_event)
        init()
        initToolBar()
        initPickerDialog()
        RestService.registerCallback(restCallback)

    }

    override fun onDestroy() {
        Log.d(TAG, "CreateNewEventActivity.onDestroy")

        super.onDestroy()
        RestService.unregisterCallback(restCallback)
    }

    private fun initToolBar() {
        Log.d(TAG, "CreateNewEventActivity.initToolBar")

        setSupportActionBar(new_event_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        new_event_toolbar.setNavigationOnClickListener { super.onBackPressed() }

    }

    override fun chooseFromStorage() {
        Log.d(TAG, "CreateNewEventActivity.chooseFromStorage")

        val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, RC_CHOOSE_FROM_GALLERY)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult() called with: requestCode = [$requestCode], resultCode = [$resultCode], data = [$data]")

        if (requestCode == RC_CHOOSE_FROM_GALLERY && null != data) {
            onImageChosenFormGallery(data)
            return
        }

        if (requestCode == RC_CAPTURE_FROM_CAMERA && resultCode == RESULT_OK) {
            onImageCapturedOnCamera()
            return
        }

        if (requestCode == PLACES_REQUEST_CODE && resultCode == RESULT_OK) {
            onPlaceChosen(data)
            return
        }
    }

    private fun onPlaceChosen(data: Intent?) {
        Log.d(TAG, "CreateNewEventActivity.onPlaceChosen")

        val place = PlacePicker.getPlace(this, data!!)
        val name = place.name
        val address = place.address

        try {
            button_event_location.text = "$address \n  $name"

        } catch (e: NullPointerException) {
            button_event_location.text = "${place.latLng.latitude}  ${place.latLng.longitude}"
        }

        longtitude = place.latLng.longitude
        latitude = place.latLng.latitude

    }

    private fun onImageChosenFormGallery(data: Intent?) {
        Log.d(TAG, "CreateNewEventActivity.onImageChosenFormGallery")

        val selectedImage = data?.data
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(selectedImage,
                filePathColumn, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val picturePath = cursor.getString(columnIndex)
        cursor.close()
        Log.d(TAG, "onActivityResult: " + picturePath)
        photoLocalUrl = picturePath
        setImageForUrl("file://" + picturePath, event_photo)
    }

    private fun onImageCapturedOnCamera() {
        Log.d(TAG, "CreateNewEventActivity.onImageCapturedOnCamera")

        setImageForUrl("file://" + photoLocalUrl!!, event_photo)
        galleryAddPic()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        Log.d(TAG, "CreateNewEventActivity.createImageFile")

        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        )

        photoLocalUrl = image.absolutePath
        return image
    }

    override fun captureFromCamera() {
        Log.d(TAG, " CreateNewEventActivity.captureFromCamera")

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this,
                        "com.bori.hipe.fileprovider",
                        photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, RC_CAPTURE_FROM_CAMERA)
            }
        }
    }

    private fun galleryAddPic() {
        Log.d(TAG, "CreateNewEventActivity.galleryAddPic")
        Log.d(TAG, "galleryAddPic() called")

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(photoLocalUrl!!)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    private fun setImageForUrl(url: String, imageView: ImageView) {
        Log.d(TAG, "CreateNewEventActivity.setImageForUrl")
        Log.d(TAG, "setImageForUrl() called with: url = [$url], imageView = [$imageView]")

        ImageLoader.getInstance().displayImage(url, imageView, DisplayImageOptions.createSimple(), object : ImageLoadingListener {
            override fun onLoadingStarted(s: String, view: View) {
                Log.d(TAG, "CreateNewEventActivity.onLoadingStarted")

                Log.d(TAG, "onLoadingStarted() called with: s = [$s], animatedView = [$view]")

            }

            override fun onLoadingFailed(s: String, view: View, failReason: FailReason) {
                Log.d(TAG, "CreateNewEventActivity.onLoadingFailed")
                Toast.makeText(this@CreateNewEventActivity, "Loading Failed", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onLoadingFailed() called with: s = [$s], animatedView = [$view], failReason = [$failReason]")

            }

            override fun onLoadingComplete(s: String, view: View, bitmap: Bitmap) {
                Log.d(TAG, "CreateNewEventActivity.onLoadingComplete")
                Toast.makeText(this@CreateNewEventActivity, "Loading complete", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onLoadingComplete() called with: s = [$s], animatedView = [$view], bitmap = [$bitmap]")

                if ((view.tag != null) and (view.tag is Int) and (view.tag as Int == EVENT_IMAGE_TAG)) {
                    button_add_photo.visibility = View.GONE
                    event_photo.setOnClickListener(myOnClickListener)
                }
            }

            override fun onLoadingCancelled(s: String, view: View) {
                Log.d(TAG, "CreateNewEventActivity.onLoadingCancelled")
                Toast.makeText(this@CreateNewEventActivity, "Loading cancelled", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onLoadingCancelled() called with: s = [$s], animatedView = [$view]")

            }
        })

    }

    private fun createEvent() {
        Log.d(TAG, "CreateNewEventActivity.createEvent")
        Log.d(TAG, "createNewEvent() called")

        val me = User.thisUser

        val thisEvent = Event(

                creatorId = me.id,
                dateMills = dateMills + timeMills,
                creationDateMills = System.currentTimeMillis(),
                isPublic = !switch_is_private_event.isChecked,
                description = button_date.text.toString() + " " + button_time.text.toString(),
                isForOneGender = !(button_gender_man.isSelected && button_gender_woman.isSelected),
                isForMale = button_gender_man.isSelected

        )

        if (validate(thisEvent))
            EventService.createNewEvent(CREATE_NEW_EVENT_ID, thisEvent)
        else
            animateMainTintViewOff()

    }

    private fun animateMainTintViewOn() {
        Log.d(TAG, "CreateNewEventActivity.animateMainTintViewOn")
        Log.d(TAG, "animateMainTintViewOn() called")
        spining_scale_view_progress_main.catchContext().start(ANIMATION_ADD_USER_DURATION, main_tint_view)
    }

    private fun animateMainTintViewOff() {
        Log.d(TAG, "CreateNewEventActivity.animateMainTintViewOff")
        Log.d(TAG, "animateMainTintViewOff() called")

        if (main_tint_view.visibility == View.VISIBLE) {

            main_tint_view.animate()
                    .alpha(0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            Log.d(TAG, "CreateNewEventActivity.onAnimationEnd")
                            super.onAnimationEnd(animation)
                            spining_scale_view_progress_main.catchContext().stopAndRelease()
                        }
                    })

        }
    }

    private fun refreshCalendar() {
        Log.d(TAG, "CreateNewEventActivity.refreshCalendar")
        Log.d(TAG, "refreshCalendar() called")

        val c = Calendar.getInstance()
        YEAR = c.get(Calendar.YEAR)
        MOUNTH = c.get(Calendar.MONTH)
        DAY = c.get(Calendar.DAY_OF_MONTH)

    }

    private fun initPickerDialog() {
        Log.d(TAG, "CreateNewEventActivity.initPickerDialog")
        Log.d(TAG, "initPickerDialog() called")

        datePickerDialog = DatePickerDialog(this, this, YEAR, MOUNTH, DAY)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() + MILLISECONDS_IN_YEAR

    }

    private val currentLocale: Locale
        @TargetApi(Build.VERSION_CODES.N)
        get() {
            Log.d(TAG, "getCurrentLocale() called")
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) resources.configuration.locales[0]
            else resources.configuration.locale
        }

    private fun setButtonTime(hour: Int, minute: Int) {
        Log.d(TAG, "CreateNewEventActivity.setButtonTime")
        Log.d(TAG, "setButtonTime() called with: hour = [$hour], minute = [$minute]")

        timeMills = (hour * 3600 * 1000 + minute * 60 * 1000).toLong()
        val minuteString = if (minute < 10) "0" + minute.toString() else minute.toString()
        val hourString = if (hour < 10) "0" + hour.toString() else hour.toString()
        button_time.text = hourString + ":" + minuteString

    }

    private fun setButtonDate(dateMills: Long) {
        Log.d(TAG, "CreateNewEventActivity.setButtonDate")

        this.dateMills = dateMills
        val simpleDateFormat = SimpleDateFormat("EEEE dd MMMM", currentLocale)
        button_date.text = simpleDateFormat.format(Date(dateMills))

    }

    override fun onDateSet(datePicker: DatePicker, year: Int, mounth: Int, day: Int) {
        Log.d(TAG, "CreateNewEventActivity.onDateSet")

        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, mounth)
        c.set(Calendar.DAY_OF_MONTH, day)
        setButtonDate(c.timeInMillis)

    }

    override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {
        Log.d(TAG, "CreateNewEventActivity.onTimeSet")
        Log.d(TAG, "onTimeSet() called with: timePicker = [$timePicker], hour = [$hour], minute = [$minute]")

        setButtonTime(hour, minute)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "CreateNewEventActivity.onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [$requestCode], permissions = [$permissions], grantResults = [$grantResults]")

        if (requestCode == ACCESS_FINE_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "location permission denied", Toast.LENGTH_SHORT).show()

        }

    }

    val myOnClickListener = {

        view: View ->
        Unit

        Log.d(TAG, "onClick: ")

        if (view.tag != null && view.tag is Int && view.tag as Int == EVENT_IMAGE_TAG)
            photoDialogFragment.show(supportFragmentManager, TAG)
        else
            when (view.id) {

                R.id.button_create_new_event -> {
                    animateMainTintViewOn()
                    Log.d(TAG, "onClick: fab")
                    createEvent()
                }

                R.id.button_date -> {
                    refreshCalendar()
                    datePickerDialog.show()
                }

                R.id.button_time -> timePickerDialog.show()

                R.id.button_gender_man -> button_gender_man.isSelected = !button_gender_man.isSelected

                R.id.button_gender_woman -> button_gender_woman.isSelected = !button_gender_woman.isSelected

                R.id.button_add_event_members -> friendsListDialogFragment.show(supportFragmentManager, TAG)

                R.id.switch_is_private_event -> if (switch_is_private_event.isChecked) {
                    main_container.removeView(gender_selecters_layout)
                    button_is_private_indicator.isSelected = true
                } else {
                    main_container.addView(gender_selecters_layout, 10)
                    button_is_private_indicator.isSelected = false
                }

                R.id.button_event_location -> {
                    animateMainTintViewOn()
                    try {
                        val intentBuilder = PlacePicker.IntentBuilder()
                        val intent = intentBuilder.build(this)

                        startActivityForResult(intent, PLACES_REQUEST_CODE)

                    } catch (e: GooglePlayServicesRepairableException) {
                        e.printStackTrace()
                    } catch (e: GooglePlayServicesNotAvailableException) {
                        e.printStackTrace()
                    } finally {
                        animateMainTintViewOff()
                    }
                }

            }

    }

    private fun validate(thisEvent: Event): Boolean {
        Log.d(TAG, "CreateNewEventActivity.validate")

        if (true)
            return true

        Log.d(TAG, "validate() called")

        var validated = true

        if (dateMills + timeMills == 0L) {

            validated = false
            create_event_members_hint.setTextColor(0xFFF12652.toInt())
            create_event_members_hint.text = "Вы не выбрали дату"

        }

        if (thisEvent.latitude + thisEvent.longitude == 0.0) {

            validated = false
            create_event_members_hint.setTextColor(0xFFF12652.toInt())
            create_event_members_hint.text = "Вы не выбрали место"

        }

        if (!thisEvent.isPublic && addedUsers.isEmpty()) {

            validated = false
            create_event_members_hint.setTextColor(0xFFF12652.toInt())
            create_event_members_hint.text = "Впишите хотя бы одного человека или сделайте вписку открытой"

        }

        if (thisEvent.isPublic && !button_gender_man.isSelected && !button_gender_woman.isSelected) {

            validated = false
            create_event_members_hint.setTextColor(0xFFF12652.toInt())
            create_event_members_hint.text = "Выберите хотя бы один пол"

        }

        return validated
    }

    inner class CreateEventActivityRestCallbackAdapter : RestCallbackAdapter() {

        override fun onSimpleResponse(requestID: Long, response: Any?, serverCode: Int) {
            Log.d(TAG, "CreateEventActivityRestCallbackAdapter.onSimpleResponse")
            when (requestID) {
                CREATE_NEW_EVENT_ID -> {
                    if (serverCode == Status.CREATED) {
                        if (photoLocalUrl != null)
                            HipeImageService.upload(UPLOAD_EVENT_PHOTO_ID, response as Long, File(photoLocalUrl))
                    }

                }
                UPLOAD_EVENT_PHOTO_ID -> if (serverCode == Status.CREATED) {
                    Log.d(TAG, "photo uploaded")
                } else {
                    Log.e(TAG, "photo upload error")
                }

            }

        }

    }

}