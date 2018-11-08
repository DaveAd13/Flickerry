package com.david.flickerry.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.david.flickerry.GetPhotosService;
import com.david.flickerry.R;
import com.david.flickerry.RecyclerViewAdapter;
import com.david.flickerry.pojo.Photo;
import com.david.flickerry.pojo.PhotoSetsJson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.david.flickerry.Constants.FLICKR_API_KEY;
import static com.david.flickerry.Constants.FLICKR_FORMAT;
import static com.david.flickerry.Constants.NO_JSON_CALLBACK;
import static com.david.flickerry.Constants.PHOTOSET_ID;
import static com.david.flickerry.Constants.BASE_URL;
import static com.david.flickerry.Constants.PHOTO_URL;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Retrofit retrofit;
    private RecyclerViewAdapter recyclerViewAdapter;
    private CompositeDisposable compositeDisposable;
    private ProgressBar progressBar;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);
        progressBar = findViewById(R.id.progress);

        if(isNetworkAvailable())
            setUpConnection();
        else
            noNetwork();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        int menuToChoose = R.menu.grid_menu;
        inflater.inflate(menuToChoose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.grid:
                if(item.getTitle().equals("List")) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_grid));
                    item.setTitle("Grid");
                    staggeredGridLayoutManager.setSpanCount(1);
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_list));
                    item.setTitle("List");
                    staggeredGridLayoutManager.setSpanCount(2);
                }
                recyclerViewAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void noNetwork() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        LinearLayout linearLayout = findViewById(R.id.no_inet_ll);
        linearLayout.setVisibility(View.VISIBLE);
        Button retry = findViewById(R.id.retry);
        retry.setOnClickListener(v -> {
            if(isNetworkAvailable()) {
                setUpConnection();
                recyclerView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setUpConnection() {
        progressBar.setVisibility(View.VISIBLE);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        callEndpoints();
    }

    private void callEndpoints() {
        GetPhotosService getPhotosService = retrofit.create(GetPhotosService.class);
        Observable<PhotoSetsJson> photoSetObservable = getPhotosService.getPhotoSets("flickr.photosets.getPhotos", FLICKR_FORMAT, NO_JSON_CALLBACK, FLICKR_API_KEY, PHOTOSET_ID);
        compositeDisposable = new CompositeDisposable();
        Disposable mDisposable = photoSetObservable
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleResults, this::handleError);

        compositeDisposable.add(mDisposable);
    }

    private void handleResults(PhotoSetsJson photoSetsJson) {
        progressBar.setVisibility(View.GONE);
        compositeDisposable.dispose();
        List<String> photoUrls = new ArrayList<>();
        List<Photo> photoList = photoSetsJson.getPhotoset().getPhoto();
        for(Photo photo: photoList) {
            String url = String.format(PHOTO_URL, photo.getFarm(), photo.getServer(), photo.getId(), photo.getSecret(), "c");
            photoUrls.add(url);
        }
        recyclerViewAdapter.setData(photoUrls);
    }

    private void handleError(Throwable throwable) {
        throwable.printStackTrace();
        compositeDisposable.dispose();
        noNetwork();
        TextView textView = findViewById(R.id.error_tv);
        textView.setText(getString(R.string.error));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
