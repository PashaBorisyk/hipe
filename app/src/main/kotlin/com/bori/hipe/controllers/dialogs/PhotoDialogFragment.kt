package com.bori.hipe.controllers.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bori.hipe.R
import java.io.Serializable

/**
 * Created by pashaborisyk on 24.01.2017.
 */
class PhotoDialogFragment : androidx.fragment.app.DialogFragment(), View.OnClickListener {

    companion object {
        private const val KEY_SOURCE_CHOOSER = "KEY_SOURCE_CHOOSER"
        private const val TAG = "PhotoDialogFragment"
    }

    private var sourceChooser: SourceChooser? = null

    fun setSourceChooser(sourceChooser: SourceChooser) {
        this.sourceChooser = sourceChooser
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "PhotoDialogFragment.onCreateDialog")

        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity!!)
        val rootView = LayoutInflater.from(activity).inflate(R.layout.dialog_event_image_chooser, null)
        val camera: View = rootView.findViewById(R.id.dialog_select_camera)
        camera.setOnClickListener(this)
        val storage: View = rootView.findViewById(R.id.dialog_select_storage)
        storage.setOnClickListener(this)

        builder.setView(rootView).setNegativeButton("Отмена") { dialogInterface, i -> this@PhotoDialogFragment.dialog?.cancel() }.setTitle("Выберете поставщика")

        return builder.create()
    }

    override fun onClick(view: View) {
        Log.d(TAG, "PhotoDialogFragment.onClick")

        when (view.id) {

            R.id.dialog_select_camera -> {
                sourceChooser!!.captureFromCamera()
                dismiss()
            }

            R.id.dialog_select_storage -> {
                sourceChooser!!.chooseFromStorage()
                dismiss()
            }

            else -> {
            }
        }

    }

    interface SourceChooser : Serializable {

        fun chooseFromStorage()

        fun captureFromCamera()

    }

}