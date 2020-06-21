package com.vikaskonaparthi.origin.ui.home;

import com.vikaskonaparthi.origin.model.Categories;
import com.vikaskonaparthi.origin.model.Meals;

import java.util.List;

public interface HomeView {
    void showLoading();
    void hideLoading();
    void setMeal(List<Meals.Meal> meal);
    void setCategory(List<Categories.Category> category);
    void onErrorLoading(String message);
}
