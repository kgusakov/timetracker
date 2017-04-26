package com.timetracker.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.timetracker.R;

public class CreateCategoryDialog extends DialogFragment {

    private CreateCategoryDialogListener dialogListener;

    public interface CreateCategoryDialogListener {
        void onDialogPositiveClick(Dialog dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.new_category);
        builder.setView(R.layout.create_category_dialog);

        builder.setPositiveButton(R.string.save, (dialog, id) -> { dialogListener.onDialogPositiveClick(this.getDialog()); });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dialogListener = (CreateCategoryDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement " + CreateCategoryDialogListener.class.getName());
        }
    }

}
