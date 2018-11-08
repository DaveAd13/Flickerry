package com.david.flickerry.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.david.flickerry.R;
import com.david.flickerry.UIUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageActivity extends AppCompatActivity {

    private String url;
    private List<String> commentList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private Bitmap bitmap;
    private  ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        if(getActionBar() != null)
            getActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        listView = findViewById(R.id.comment_list);
        setImage();
        addComments();
        Button send = findViewById(R.id.send);
        send.setOnClickListener(view -> addComment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        int menuToChoose = R.menu.share_menu;
        inflater.inflate(menuToChoose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.share:
                share(bitmap);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addComments() {
        commentList = getComments();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commentList);
        listView.setAdapter(arrayAdapter);
        UIUtils.setListViewHeightBasedOnItems(listView);
    }

    private void addComment() {
        EditText comment_et = findViewById(R.id.comment_et);
        String comment = comment_et.getText().toString().trim();
        if(!comment.equals("")) {
            hideKeyboard();
            comment_et.setText("");
            commentList.add(comment);
            setComments(commentList);
            arrayAdapter.notifyDataSetChanged();
            UIUtils.setListViewHeightBasedOnItems(listView);
        }
    }

    private void setImage() {
        url = Objects.requireNonNull(getIntent().getExtras()).getString("url");
        ImageView imageView = findViewById(R.id.img);
        Glide.with(this)
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .placeholder(R.drawable.placeholder).dontTransform())
                .into(new Target<Bitmap>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onStop() {

                    }

                    @Override
                    public void onDestroy() {

                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resource);
                        bitmap = resource;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void getSize(@NonNull SizeReadyCallback cb) {

                    }

                    @Override
                    public void removeCallback(@NonNull SizeReadyCallback cb) {

                    }

                    @Override
                    public void setRequest(@Nullable Request request) {

                    }

                    @Nullable
                    @Override
                    public Request getRequest() {
                        return null;
                    }
                });
    }

    private void share(Bitmap bitmap) {
        Uri bitmapUri = getImageUri(bitmap);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(bitmapUri, getContentResolver().getType(bitmapUri));
        shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        shareIntent.setType("image/png");
        startActivity(Intent.createChooser(shareIntent, "Shape"));
    }

    public Uri getImageUri(Bitmap bitmap) {
        File cachePath = new File(getCacheDir(), "my_images");
        cachePath.mkdirs();
        try {
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File imagePath = new File(getCacheDir(), "my_images");
        File newFile = new File(imagePath, "image.png");
        return FileProvider.getUriForFile(this, "com.david.flickerry.fileprovider", newFile);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private List<String> getComments() {
        SharedPreferences sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("comments_" + url, null);
        if(json == null)
            return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void setComments(List<String> value) {
        SharedPreferences sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        editor.putString("comments_" + url, json);
        editor.apply();
    }
}
