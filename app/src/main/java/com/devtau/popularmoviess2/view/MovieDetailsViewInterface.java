package com.devtau.popularmoviess2.view;

import com.devtau.popularmoviess2.model.Movie;
import com.devtau.popularmoviess2.model.Trailer;
import java.util.ArrayList;
/**
 * Вью-интерфейс, обеспечивающий общение презентера с пользователем
 */
public interface MovieDetailsViewInterface extends View {
    void showMessage(String msg);
    void populateUI(Movie movie);
    void populateTrailersList(ArrayList<Trailer> trailersList);
}
