package com.vikaskonaparthi.origin.api;

import com.vikaskonaparthi.origin.model.Categories;
import com.vikaskonaparthi.origin.model.Meals;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FoodApi {

    @GET("latest.php")
    Call<Meals> getMeal();

    @GET("categories.php")
    Call<Categories> getCategories();

    @GET("filter.php") 
    Call<Meals> getMealByCategory(@Query("c") String category);

    //TODO #3 Call the search.php with query string the meal name @GET("search.php)
    @GET("search.php")
    Call<Meals> getMealByName(@Query("s") String mealName);
}
