package com.devtau.popularmoviess2.view;

import com.devtau.popularmoviess2.model.Review;
import java.util.ArrayList;
/**
 * Вью-интерфейс, обеспечивающий общение презентера с пользователем
 * View interface to communicate with user
 */
public interface ReviewsListViewInterface extends View {
    void populateList(ArrayList<Review> reviewsList);
    void showMessage(String msg);
}
