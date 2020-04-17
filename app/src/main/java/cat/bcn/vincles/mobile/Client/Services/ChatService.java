package cat.bcn.vincles.mobile.Client.Services;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageMulticastRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Model.ChatMessagesSentResponse;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ChatService {

    @Headers("Content-Type: application/json")
    @POST("/t/vincles-bcn.cat/vincles-services/1.0/messages")
    Call<ChatMessageSentResponse> sendChatMessage(@Body ChatMessageRest chatMessageRest);

    @Headers("Content-Type: application/json")
    @POST("/t/vincles-bcn.cat/vincles-services/1.0/community/message")
    Call<ChatMessagesSentResponse> sendChatMessageMulticast(@Body ChatMessageMulticastRest chatMessageRest);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/messages/chat/{idUserSender}")
    public Call<ArrayList<ChatMessageRest>> getUserMessages(@Path("idUserSender") String idUserSender, @QueryMap Map<String, String> params);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/messages/mine")
    public Call<ArrayList<ChatMessageRest>> getChatMessagesFrom(@QueryMap Map<String, String> params);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/messages/{idMessage}")
    public Call<ChatMessageRest> getMessage(@Path("idMessage") String idMessage);

    @DELETE("/t/vincles-bcn.cat/vincles-services/1.0/messages/{idMessage}")
    public Call<ResponseBody> deleteMessage(@Path("idMessage") String idMessage);

    @PUT("/t/vincles-bcn.cat/vincles-services/1.0/messages/{idMessage}/watched")
    public Call<ResponseBody> setMessageWatched(@Path("idMessage") String idMessage);

    @Headers("Content-Type: application/json")
    @POST("/t/vincles-bcn.cat/vincles-services/1.0/chats/{idChat}/messages")
    Call<ChatMessageSentResponse> sendGroupMessage(@Body GroupMessageRest groupMessageRest,
                                                    @Path("idChat") String idChat);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/chats/{idChat}/messages/{idMessage}")
    public Call<GroupMessageRest> getGroupMessage(@Path("idChat") String idChat,
                                                  @Path("idMessage") String idMessage);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/chats/{idChat}/messages")
    public Call<ArrayList<GroupMessageRest>> getGroupMessages(@Path("idChat") String idChat, @QueryMap Map<String, String> params);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/chats/{idChat}/messages/{idMessage}")
    public Call<ResponseBody> getGroupContent(@Path("idChat") String idChat,
                                              @Path("idMessage") String idMessage);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/chats/{idChat}/lastAccess")
    public Call<JsonObject> getGroupLastAcess(@Path("idChat") String idChat);

    @PUT("/t/vincles-bcn.cat/vincles-services/1.0/chats/{idChat}/lastAccess")
    public Call<JsonObject> putGroupLastAcess(@Path("idChat") String idChat, @Body JsonObject body);

}
