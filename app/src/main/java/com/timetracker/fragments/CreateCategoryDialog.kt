package com.timetracker.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

import com.timetracker.R

class CreateCategoryDialog : DialogFragment() {

    private var dialogListener: CreateCategoryDialogListener? = null

    interface CreateCategoryDialogListener {
        fun onDialogPositiveClick(dialog: Dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.new_category)
        builder.setView(R.layout.create_category_dialog)

        builder.setPositiveButton(R.string.save) { _, _ -> dialogListener!!.onDialogPositiveClick(this.dialog) }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            dialogListener = activity as CreateCategoryDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString()
                    + " must implement " + CreateCategoryDialogListener::class.java.name)
        }

    }

}
