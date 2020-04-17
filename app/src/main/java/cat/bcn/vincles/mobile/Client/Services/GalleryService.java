package cat.bcn.vincles.mobile.Client.Services;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface GalleryService {
    @GET("/t/vincles-bcn.cat/vincles-services/1.0/galleries/mine/entries")
    public Call<ArrayList<GalleryContentRealm>> getMineContents(@Query("to") long to,
                                           @Query("types") String types);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/contents/{contentID}")
    public Call<ResponseBody> getContent(@Path("contentID") String contentID);

    @DELETE("/t/vincles-bcn.cat/vincles-services/1.0/galleries/mine/entries/{idGalleryEntry}")
    public Call<ResponseBody> deleteContent(@Path("idGalleryEntry") String idGalleryEntry);

    @Multipart
    @POST("/t/vincles-bcn.cat/vincles-services/1.0/contents")
    public Call<JsonObject> addContent(@Part MultipartBody.Part file);

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/galleries/mine/entries")
    public Call<JsonObject> addContentInTheGallery(@Body JsonObject contentData);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/galleries/mine/entries/{idGalleryEntry}")
    public Call<GalleryContentRealm> getContentInMyGallery(@Path("idGalleryEntry") int idGalleryEntry);
}
