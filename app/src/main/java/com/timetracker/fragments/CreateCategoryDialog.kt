package com.timetracker.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.EditText

import com.timetracker.R

class CreateCategoryDialog(val createCategoryListener: (categoryName: String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.new_category)
        builder.setView(R.layout.create_category_dialog)

        builder.setPositiveButton(R.string.save) { _, _ ->
            val categoryNameText = dialog.findViewById(R.id.category_name) as EditText
            createCategoryListener(categoryNameText.text.toString())
            dismiss()
        }
        return builder.create()
    }

}
