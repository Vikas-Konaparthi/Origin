package com.vikaskonaparthi.origin.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.vikaskonaparthi.origin.R;
import com.vikaskonaparthi.origin.Utils;
import com.vikaskonaparthi.origin.adapter.RecyclerViewHomeAdapter;
import com.vikaskonaparthi.origin.adapter.ViewPagerHeaderAdapter;
import com.vikaskonaparthi.origin.model.Categories;
import com.vikaskonaparthi.origin.model.Meals;
import com.vikaskonaparthi.origin.view.category.CategoryActivity;
import com.vikaskonaparthi.origin.view.detail.DetailActivity;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment implements HomeView{
    AdView mAdView;

    private HomeViewModel homeViewModel;
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_DETAIL = "detail";

    @BindView(R.id.viewPagerHeader)
    ViewPager viewPagerMeal;
    @BindView(R.id.recyclerCategory)
    RecyclerView recyclerViewCategory;

    HomePresenter presenter;
    protected View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(getActivity());

        presenter = new HomePresenter(this);
        presenter.getMeals();
        presenter.getCategories();
        mAdView = (AdView) root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return root;
    }

    @Override
    public void showLoading() {
        if(getView()!=null && isAdded()) {
            getView().findViewById(R.id.shimmerMeal).setVisibility(View.VISIBLE);
        }
        if(getView()!=null && isAdded()) {
            getView().findViewById(R.id.shimmerCategory).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if(getView()!=null && isAdded()) {
            getView().findViewById(R.id.shimmerMeal).setVisibility(View.GONE);
        }
        if(getView()!=null && isAdded()) {
            getView().findViewById(R.id.shimmerCategory).setVisibility(View.GONE);
        }
    }

    @Override
    public void setMeal(List<Meals.Meal> meal) {
        ViewPagerHeaderAdapter headerAdapter = new ViewPagerHeaderAdapter(meal, getActivity());
        viewPagerMeal.setAdapter(headerAdapter);
        viewPagerMeal.setPadding(20, 0, 150, 0);
        headerAdapter.notifyDataSetChanged();

        headerAdapter.setOnItemClickListener((view, position) -> {
            TextView mealName = view.findViewById(R.id.mealName);
            Intent intent = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
            intent.putExtra(EXTRA_DETAIL,mealName.getText().toString());
            startActivity(intent);
        });
    }

    @Override
    public void setCategory(List<Categories.Category> category) {
        RecyclerViewHomeAdapter homeAdapter = new RecyclerViewHomeAdapter(category, getActivity());
        recyclerViewCategory = getView().findViewById(R.id.recyclerCategory);
        recyclerViewCategory.setAdapter(homeAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3,
                GridLayoutManager.VERTICAL, false);
        recyclerViewCategory.setLayoutManager(layoutManager);
        recyclerViewCategory.setNestedScrollingEnabled(true);
        homeAdapter.notifyDataSetChanged();

        homeAdapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(getActivity(), CategoryActivity.class);
            intent.putExtra(EXTRA_CATEGORY, (Serializable) category);
            intent.putExtra(EXTRA_POSITION, position);
            startActivity(intent);
        });
    }

    @Override
    public void onErrorLoading(String message) {
        Utils.showDialogMessage(getActivity(), "Title", message);
    }
}