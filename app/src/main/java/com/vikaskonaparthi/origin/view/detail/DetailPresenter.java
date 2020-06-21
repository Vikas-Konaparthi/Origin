package com.vikaskonaparthi.origin.view.detail;

import android.support.annotation.NonNull;

import com.vikaskonaparthi.origin.Utils;
import com.vikaskonaparthi.origin.model.Meals;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPresenter {
    private DetailView view;

    public DetailPresenter(DetailView view) {
        this.view = view;
    }

    void getMealById(String mealName) {
        
        view.showLoading();
        
        //TODO #6 Make a request to the server (Don't forget to hide loading when the response is received)
        Utils.getApi().getMealByName(mealName)
                .enqueue(new Callback<Meals>() {
                    @Override
                    public void onResponse(@NonNull Call<Meals> call,@NonNull Response<Meals> response) {
                        view.hideLoading();
                        if(response.isSuccessful() && response.body() != null){
                            view.setMeal(response.body().getMeals().get(0));
                        }else{
                            view.onErrorLoading(response.message());
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<Meals> call,@NonNull Throwable t) {
                        view.hideLoading();
                        view.onErrorLoading(t.getLocalizedMessage());

                    }
                });
        //TODO #7 Set response (meal)
    }
}
