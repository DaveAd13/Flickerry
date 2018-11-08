package com.david.flickerry;


import com.david.flickerry.pojo.PhotoSetsJson;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetPhotosService {
    @GET("rest/")
    Observable<PhotoSetsJson> getPhotoSets(@Query("method") String method, @Query("format") String format, @Query("nojsoncallback") int nojsoncallback, @Query("api_key") String apiKey, @Query("photoset_id") String photoset_id);
}
