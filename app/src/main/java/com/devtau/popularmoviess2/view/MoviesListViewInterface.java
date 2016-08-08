package com.devtau.popularmoviess2.view;

import android.database.Cursor;
/**
 * Вью-интерфейс, обеспечивающий общение презентера с пользователем
 */
public interface MoviesListViewInterface extends View {
    void showNoInternet();
    void showProgressBar();
    boolean dismissProgressBar();
    void initList();
    void showMessage(String msg);
    void swapCursor(Cursor cursor);
}
