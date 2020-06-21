package com.vikaskonaparthi.origin.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.vikaskonaparthi.origin.Adapter;
import com.vikaskonaparthi.origin.Contact;
import com.vikaskonaparthi.origin.DarkMode;
import com.vikaskonaparthi.origin.MainActivity;
import com.vikaskonaparthi.origin.NewsDetailActivity;
import com.vikaskonaparthi.origin.R;
import com.vikaskonaparthi.origin.SettingsActivity;
import com.vikaskonaparthi.origin.Utils;
import com.vikaskonaparthi.origin.api.ApiClient;
import com.vikaskonaparthi.origin.api.ApiInterface;
import com.vikaskonaparthi.origin.models.Article;
import com.vikaskonaparthi.origin.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private DashboardViewModel dashboardViewModel;
    public static final String API_KEY= "e96f2eb28f2540bf98757628abd274b3";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = DashboardFragment.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle,errorMessage;
    private Button btnRetry;
    private Switch myswitch;
    private InterstitialAd interstitialAd;
    AdView mAdView;

    @SuppressLint("ResourceAsColor")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        super.onCreate(savedInstanceState);
        swipeRefreshLayout=root.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorAccent);
        recyclerView = root.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getActivity()   );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        setHasOptionsMenu(true);
        onLoadingSwipeRefresh("");

        errorLayout = root.findViewById(R.id.errorLayout);
        errorImage = root.findViewById(R.id.errorImage);
        errorTitle = root.findViewById(R.id.errorTitle) ;
        errorMessage = root.findViewById(R.id.errorMessage);
        btnRetry = root.findViewById(R.id.btnRetry);
        MobileAds.initialize(getActivity(),"ca-app-pub-xxxxxxxxxxxxxxxxx");
        interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId("ca-app-pub-xxxxxxxxxxxxxxxxxx");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed(){
                Intent i = new Intent(getActivity(),DarkMode.class);
                startActivity(i);
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
        mAdView = (AdView) root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        return root;
    }
    public void LoadJson(final String keyword){
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        final ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry();
        String language =Utils.getLanguage();
        Call<News> call;

        if(keyword.length()>0){
            call = apiInterface.getNewsSearch(keyword,language,"publishedAt",API_KEY);
        }else{
            call= apiInterface.getNews(country,API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticle() != null){
                    if(!articles.isEmpty()){
                        articles.clear();
                    }
                    articles = response.body().getArticle();
                    adapter = new Adapter(articles,getActivity());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListner();
                    swipeRefreshLayout.setRefreshing(false);

                }else{
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()){
                        case 404:
                            errorCode = "404 not found,don't worry you are on Origin";
                        case 500:
                            errorCode = "505 server broken,don't worry you are on Origin";
                        default:
                            errorCode="unknown error,don't worry you are on Origin";
                            break;
                    }
                    showErrorMessage(R.drawable.no_result,
                            "no result",
                            "Please Try Again\n"+
                                    errorCode);

                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(R.drawable.no_result,
                        "Oops!",
                        "Network failure,Please Try Later,don't worry we will back soon. \n Origin"+ t.toString());

            }
        });

    }
    private void initListner(){
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView =view.findViewById(R.id.img);
                Intent intent = new Intent(getActivity(), NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url",article.getUrl());
                intent.putExtra("title",article.getTitle());
                intent.putExtra("img",article.getUrlToImage());
                intent.putExtra("date",article.getPublishedAt());
                intent.putExtra("source",article.getSource().getName());
                intent.putExtra("author",article.getAuthor());

                Pair<View, String> pair = Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        pair


                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

                    startActivity(intent,optionsCompat.toBundle());

                }else{
                    startActivity(intent);
                }

            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)  {
        inflater.inflate(R.menu.menu_main,menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("Search Latest News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>2){
                    onLoadingSwipeRefresh(query);
                }else{
                    Toast.makeText(getActivity(), "Type More than two letters", Toast.LENGTH_SHORT).show();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchMenuItem.getIcon().setVisible(false,false);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.activity_contact:
                startActivity(new Intent(getActivity(), Contact.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.dark_mode:
                if(interstitialAd.isLoaded())
                {
                    interstitialAd.show();
                }
                else {

                    startActivity(new Intent(getActivity(), DarkMode.class));
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);

        }

    }


    @Override
    public void onRefresh() {
        LoadJson("");


    }
    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);

                    }
                }
        );
    }
    private void showErrorMessage(int imageView,String title,String message){
        if(errorLayout.getVisibility()==View.GONE){
            errorLayout.setVisibility(View.VISIBLE);
        }
        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);
        btnRetry.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onLoadingSwipeRefresh("");
            }
        });
    }


}