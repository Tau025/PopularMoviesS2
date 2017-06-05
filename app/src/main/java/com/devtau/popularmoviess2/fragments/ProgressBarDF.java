package com.devtau.popularmoviess2.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import com.devtau.popularmoviess2.R;
import com.devtau.popularmoviess2.util.Logger;
/**
 * Фрагмент чтобы показать пользователю, что приложение делает важную работу
 * Fragment shows to user that app is now busy doing important background work
 */
public class ProgressBarDF extends DialogFragment {
    private static final String TAG = ProgressBarDF.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().setBackgroundDrawableResource(R.color.colorTransparent);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.progress_bar, null);
        dialog.setContentView(view);
        return dialog;
    }

    //Метод создаст новый диалог, если его еще нет на экране
    //Creates new dialog if there is none shown to user
    public static boolean show(FragmentManager manager) {
        ProgressBarDF dialog = (ProgressBarDF) manager.findFragmentByTag(TAG);
        if (dialog == null) {
//            Logger.v(TAG, "ProgressBarDF not found. going to create new one");
            new ProgressBarDF().show(manager, TAG);
            return true;
        } else {
            Logger.e(TAG, "ProgressBarDF already shown");
            return false;
        }
    }

    //Метод закроет диалог, если найдет его
    //Closes dialog if it is present
    public static boolean dismiss(FragmentManager manager){
        ProgressBarDF dialog = (ProgressBarDF) manager.findFragmentByTag(TAG);
        if (dialog != null) {
//            Logger.v(TAG, "ProgressBarDF found. going to dismiss it");
            dialog.dismiss();
            return true;
        } else {
            Logger.e(TAG, "ProgressBarDF already dismissed");
            return false;
        }
    }
}