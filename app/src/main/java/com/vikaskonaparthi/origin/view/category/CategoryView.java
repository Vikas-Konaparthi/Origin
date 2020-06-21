package com.vikaskonaparthi.origin.view.category;

import com.vikaskonaparthi.origin.model.Meals;

import java.util.List;

public interface CategoryView {
    void showLoading();
    void hideLoading();
    void setMeals(List<Meals.Meal> meals);
    void onErrorLoading(String message);
}
