/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.push;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.business.ChatService;
import cat.bcn.vincles.lib.business.GroupService;
import cat.bcn.vincles.lib.business.MessageService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.TaskService;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.dao.ChatDAO;
import cat.bcn.vincles.lib.dao.ChatDAOImpl;
import cat.bcn.vincles.lib.dao.GroupDAO;
import cat.bcn.vincles.lib.dao.GroupDAOImpl;
import cat.bcn.vincles.lib.dao.MessageDAO;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.NetworkDAOImpl;
import cat.bcn.vincles.lib.dao.PushMessageDAO;
import cat.bcn.vincles.lib.dao.PushMessageDAOImpl;
import cat.bcn.vincles.lib.dao.ResourceDAO;
import cat.bcn.vincles.lib.dao.ResourceDAOImpl;
import cat.bcn.vincles.lib.dao.TaskDAO;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.UserGroup;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InternalPushProcessor implements VinclesPushListener {
    private static final String TAG = "InternalPushLH";
    private static VinclesPushListener appPushListener;
    private PushMessageDAO pushMessageDAO;
    private MessageDAO messageDAO;
    private ChatDAO chatDAO;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    private ResourceDAO resourceDAO;
    private GroupDAO groupDAO;

    // VOID APP LISTENER AT FIRST
    public InternalPushProcessor() {
        pushMessageDAO = new PushMessageDAOImpl();
        messageDAO = new MessageDAOImpl();
        chatDAO = new ChatDAOImpl();
        taskDAO = new TaskDAOImpl();
        userDAO = new UserDAOImpl();
        resourceDAO = new ResourceDAOImpl();
        groupDAO = new GroupDAOImpl();

        setAppPushListener(appPushListener);
    }

    private void asyncUpdatePushMessageData (final PushMessage pushMessage, Long id) {
        MessageService messageService = ServiceGenerator.createService(
                MessageService.class, TokenAuthenticator.model.getAccessToken());

        Call<JsonObject> call = messageService.getMessage(id);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    Message message = Message.fromJSON(result.body());

                    // CAUTION: Check if Message has 'metadataTipus'
                    if (VinclesConstants.hasKnownType(message.metadataTipus)) {
                        // Save resources of message in 'resourceTempList'
                        for (Resource it : message.resourceTempList) {
                            it.message = message;
                            resourceDAO.save(it);
                        }
                        messageDAO.save(message);

                        // Update pushMessage
                        pushMessage.setRawDataJson(result.body().toString());
                        pushMessageDAO.save(pushMessage);
                        appPushListener.onPushMessageReceived(pushMessage);
                    } else {
                        Log.e("PushMessage", "No metadataTipus or unknown!");
                    }
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    appPushListener.onPushMessageError(pushMessage.getIdPush(), new Throwable(error.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                pushMessageDAO.save(pushMessage);
                appPushListener.onPushMessageError(pushMessage.getIdPush(), t);
            }
        });
    }

    private void asyncUpdateEventData (final PushMessage pushMessage, Long id, final Long calendarId) {
        TaskService client = ServiceGenerator.createService(TaskService.class, TokenAuthenticator.model.getAccessToken());
        Call<JsonObject> call = client.getTask(calendarId, id);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.body() != null) pushMessage.setRawDataJson(result.body().toString());
                pushMessageDAO.save(pushMessage);

                Task dbTask = taskDAO.get(pushMessage.getIdData());
                JsonObject json = result.body();
                boolean sendreturn = true;
                if (json != null) {
                    Task task = Task.fromJSON(json);

                    User userCreator = null;
                    JsonElement userCreatorJSON = json.getAsJsonObject().get("userCreator");
                    if (userCreatorJSON != null && userCreatorJSON.isJsonNull() == false) {
                        userCreator = User.fromJSON(userCreatorJSON.getAsJsonObject());

                        // Retrive local data
                        User user = userDAO.get(userCreator.getId());
                        if (user != null) userCreator.imageName = user.imageName;
                        if (userCreator != null) {
                            task.owner = userCreator;

                            // Get user photo & save
                            if (user == null || user.imageName == null || user.imageName.equals("")) {
                                // Wait for user photo
                                sendreturn = false;
                                getUserPhoto(new AsyncResponse() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        appPushListener.onPushMessageReceived(pushMessage);
                                    }

                                    @Override
                                    public void onFailure(Object error) {
                                        String msgError = ((VinclesError)error).getMessage();
                                        appPushListener.onPushMessageError(pushMessage.getIdPush(), new Throwable(msgError));
                                        appPushListener.onPushMessageReceived(pushMessage);
                                    }
                                }, userCreator);
                            }
                        }
                        // Save first UserCreator (must be persisted separated by the ORM)
                        userDAO.save(userCreator);
                    }
                    Network n = new NetworkDAOImpl().findByUserCalendar(calendarId);
                    task.network = n;
                    taskDAO.save(task);
                    if (sendreturn) appPushListener.onPushMessageReceived(pushMessage);
                    Log.d(null, "GCM: userCreatorJSON null, filtered Message");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                pushMessageDAO.save(pushMessage);
                appPushListener.onPushMessageError(pushMessage.getIdPush(), t);
            }
        });
    }

    // COMMON PROCESS MESSAGE PART
    @Override
    public void onPushMessageReceived(final PushMessage pushMessage) {
        Log.d(null, "GCM: MESSAGE INTO LIBRARY LISTENER: " + pushMessage.getInfo());
        Long id = null;
        switch (pushMessage.getType()) {
            // PROCESS HERE AND STORE IN DB
            case PushMessage.TYPE_NEW_MESSAGE:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idMessage").getAsLong();
                    pushMessage.setIdData(id);
                    asyncUpdatePushMessageData(pushMessage, id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_EVENT_UPDATED:
            case PushMessage.TYPE_NEW_EVENT:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idEvent").getAsLong();
                    Long calendarId = obj.get("idCalendar").getAsLong();
                    pushMessage.setIdData(id);
                    asyncUpdateEventData(pushMessage, id, calendarId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_REMEMBER_EVENT:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idEvent").getAsLong();
                    pushMessage.setIdData(id);
                    pushMessageDAO.save(pushMessage);
                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_DELETED_EVENT:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idEvent").getAsLong();
                    // Delete locally here
                    Task task = taskDAO.get(id);
                    if (task != null) {
                        // store data for notification
                        pushMessage.setRawDataJson(task.toJSON().toString());
                        // now delete
                        taskDAO.delete(task);
                    }

                    pushMessage.setIdData(id);
                    pushMessageDAO.save(pushMessage);
                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_EVENT_ACCEPTED:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idEvent").getAsLong();
                    // Delete locally here
                    Task task = taskDAO.get(id);
                    if (task != null) {
                        // store data for notification
                        task.state = Task.STATE_ACCEPTED;
                        pushMessage.setRawDataJson(task.toJSON().toString());
                        taskDAO.save(task);
                    }

                    pushMessage.setIdData(id);
                    pushMessageDAO.save(pushMessage);
                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_EVENT_REJECTED:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idEvent").getAsLong();
                    // Delete locally here
                    Task task = taskDAO.get(id);
                    if (task != null) {
                        // store data for notification
                        task.state = Task.STATE_REJECTED;
                        pushMessage.setRawDataJson(task.toJSON().toString());
                        taskDAO.save(task);
                    }

                    pushMessage.setIdData(id);
                    pushMessageDAO.save(pushMessage);
                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_USER_LINKED:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idUser").getAsLong();
                    pushMessage.setIdData(id);
                    pushMessageDAO.save(pushMessage);

                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_USER_UNLINKED:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idUser").getAsLong();
                    pushMessage.setIdData(id);
                    pushMessageDAO.save(pushMessage);

                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_USER_UPDATED:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idUser").getAsLong();
                    pushMessage.setIdData(id);
                    pushMessageDAO.save(pushMessage);

                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_INVITATION_SENT:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idInvitation").getAsLong();
                    pushMessage.setIdData(id);

                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_ADDED_TO_GROUP:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idGroup").getAsLong();
                    pushMessage.setIdData(id);

                    getGroup(pushMessage);
                    //appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_NEW_USER_GROUP:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idGroup").getAsLong();
                    pushMessage.setIdData(id);
                    Long extraId = obj.get("idUser").getAsLong();
                    pushMessage.setIdExtra(extraId);

                    appPushListener.onPushMessageReceived(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PushMessage.TYPE_NEW_CHAT:
                try {
                    JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
                    id = obj.get("idChatMessage").getAsLong();
                    pushMessage.setIdData(id);

                    getChat(pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                pushMessage.setIdData(-1l);
                pushMessage.setInfo("Type Message Not Supported");
                pushMessage.save();
                break;
        }
    }

    @Override
    public void onPushMessageError(long idPush, Throwable t) {
        t.printStackTrace();
        appPushListener.onPushMessageError(idPush, t);
    }

    public void getUserPhoto (final AsyncResponse response, final User user) {
        UserService client = ServiceGenerator.createService(UserService.class, TokenAuthenticator.model.getAccessToken());
        Call<ResponseBody> call = client.getUserPhoto(user.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    byte[] data = null;
                    try {
                        data = IOUtils.toByteArray(result.body().byteStream());
                        String imageName = VinclesConstants.IMAGE_USER_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
                        VinclesConstants.saveImage(data, imageName);

                        // Update reference to user image file
                        user.imageName = imageName;
                        userDAO.save(user);
                        response.onSuccess(imageName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    response.onFailure(error);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    public void getChat(final PushMessage pushMessage) {
        JsonObject obj = new JsonParser().parse(pushMessage.getInfo()).getAsJsonObject();
        long idChat = obj.get("idChat").getAsLong();

        ChatService client = ServiceGenerator.createService(ChatService.class, TokenAuthenticator.model.getAccessToken());
        Call<JsonObject> call = client.getChat(idChat, pushMessage.getIdData());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    if (json != null) {
                        Chat chat = Chat.fromJSON(json);

                        // CAUTION: Check if Chat has 'metadataTipus'
                        if (VinclesConstants.hasKnownType(chat.metadataTipus)) {
                            // Save resources from 'chat'
                            if (chat.idContent != null) {
                                Resource re = new Resource();
                                re.chat = chat;
                                re.setId(chat.idContent);
                                resourceDAO.save(re);
                            }
                            chatDAO.save(chat);

                            // Update pushMessage
                            pushMessageDAO.save(pushMessage);
                            appPushListener.onPushMessageReceived(pushMessage);
                        } else {
                            Log.e("PushChat", "No metadataTipus or unknown!");
                        }
                    }
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    appPushListener.onPushMessageError(pushMessage.getIdPush(), new Throwable(error.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                appPushListener.onPushMessageError(pushMessage.getIdPush(), t);
            }
        });
    }

    public void getGroup(final PushMessage pushMessage) {
        GroupService client = ServiceGenerator.createService(GroupService.class, TokenAuthenticator.model.getAccessToken());
        Call<JsonArray> call = client.getUserGroupList();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<VinclesGroup> items = new ArrayList<VinclesGroup>();
                    for (JsonElement it : jsonArray) {
                        JsonObject jsonGroup = it.getAsJsonObject().get("group").getAsJsonObject();
                        VinclesGroup item = VinclesGroup.fromJSON(jsonGroup);

                        // Add dynamizer chat associated
                        item.idDynamizerChat = it.getAsJsonObject().get("idDynamizerChat").getAsLong();

                        items.add(item);
                    }

                    for (VinclesGroup it : items) {
                        VinclesGroup group = groupDAO.get(it.getId());
                        if (group == null) {
                            it.active = true;
                            it.dynamizer.isDynamizer = true;
                            userDAO.save(it.dynamizer);
                            groupDAO.save(it);

                            // Get user for this group
                            break;
                        }
                    }

                    // Update pushMessage
                    pushMessageDAO.save(pushMessage);
                    appPushListener.onPushMessageReceived(pushMessage);
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    appPushListener.onPushMessageError(pushMessage.getIdPush(), new Throwable(error.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                appPushListener.onPushMessageError(pushMessage.getIdPush(), t);
            }
        });
    }

    public void setAppPushListener(VinclesPushListener mPushListener) {
        if (mPushListener == null)
            appPushListener = new VinclesPushListener() {
                @Override
                public void onPushMessageReceived(PushMessage pushMessage) {
                    // EMPTY RECEIVER
                }

                @Override
                public void onPushMessageError(long idPush, Throwable t) {
                    // EMPTY RECEIVER
                }
            };

        else appPushListener = mPushListener;
    }

    public VinclesPushListener getAppPushListener() {
        return appPushListener;
    }
}
