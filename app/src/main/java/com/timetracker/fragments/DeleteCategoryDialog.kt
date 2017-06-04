package com.timetracker.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.timetracker.R

class DeleteCategoryDialog(val categoryName: String, val categoryId: Int, val dialogListener: (categoryId: Int) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(categoryName)
        builder.setMessage(R.string.delete_message)

        builder.setPositiveButton(R.string.delete) { _, _ ->
            dialogListener(categoryId)
            dismiss()
        }

        builder.setNegativeButton(R.string.cancel, {dialog, _ -> dialog.dismiss()})
        return builder.create()
    }

}