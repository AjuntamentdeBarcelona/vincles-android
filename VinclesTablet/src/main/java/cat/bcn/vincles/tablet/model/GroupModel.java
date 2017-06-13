/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import android.util.Log;
import android.widget.Toast;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import cat.bcn.vincles.lib.business.ChatService;
import cat.bcn.vincles.lib.business.GroupService;
import cat.bcn.vincles.lib.business.ResourceService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.dao.ChatDAO;
import cat.bcn.vincles.lib.dao.ChatDAOImpl;
import cat.bcn.vincles.lib.dao.MessageDAO;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.GroupDAO;
import cat.bcn.vincles.lib.dao.GroupDAOImpl;
import cat.bcn.vincles.lib.dao.ResourceDAO;
import cat.bcn.vincles.lib.dao.ResourceDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.dao.UserGroupDAO;
import cat.bcn.vincles.lib.dao.UserGroupDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.UserGroup;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupModel {
    private static final String TAG = "GroupModel";
    protected MainModel mainModel = MainModel.getInstance();
    protected TaskModel taskModel = TaskModel.getInstance();
    private boolean initialized;
    private static GroupModel instance;
    private GroupDAO groupDAO;
    private UserDAO userDAO;
    private MessageDAO messageDAO;
    private ChatDAO chatDAO;
    private ResourceDAO resourceDAO;
    private UserGroupDAO userGroupDAO;
    public List<VinclesGroup> groupList;
    public VinclesGroup currentGroup;

    public GroupModel() {
    }

    public static GroupModel getInstance() {
        if (instance == null) {
            instance = new GroupModel();
            instance.initialize();
        }
        return instance;
    }

    public void initialize() {
        if (!initialized) {
            groupDAO = new GroupDAOImpl();
            userDAO = new UserDAOImpl();
            messageDAO = new MessageDAOImpl();
            chatDAO = new ChatDAOImpl();
            resourceDAO = new ResourceDAOImpl();
            userGroupDAO = new UserGroupDAOImpl();

            initialized = true;
        }
    }

    public List<VinclesGroup> getGroupList() {
        return groupDAO.getActiveList();
    }

    public void getGroupServer (final AsyncResponse response, final Long groupId) {
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

                    VinclesGroup group = null;
                    for (VinclesGroup it : items) {
                        group = groupDAO.get(it.getId());
                        if (group == null || group.getId().longValue() == groupId.longValue()) {
                            it.active = true;
                            it.dynamizer.isDynamizer = true;
                            userDAO.save(it.dynamizer);
                            groupDAO.save(it);

                            // Get user for this group
                            group = it;
                            break;
                        }
                    }

                    response.onSuccess(group);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    public void getGroupServerList(final AsyncResponse response) {
        Log.i(TAG, "getGroupServerList()");

        GroupService client = ServiceGenerator.createService(GroupService.class, mainModel.accessToken);
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
                    saveOrUpdateGroupList(items);
                    response.onSuccess(items);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    private void saveOrUpdateGroupList(List<VinclesGroup> items) {
        for (VinclesGroup item : items) {
            VinclesGroup group = getGroup(item.getId());
            item.active = true;
            saveGroup(item);
        }
    }

    public VinclesGroup getGroup(Long id) {
        return groupDAO.get(id);
    }

    public VinclesGroup getGroupByChat(Long idChat) {
        return groupDAO.getGroupByChat(idChat);
    }

    public VinclesGroup getGroupByDynamizerChat(Long idDynamizerChat) {
        return groupDAO.getGroupByDynamizerChat(idDynamizerChat);
    }

    public void saveGroup(VinclesGroup item) {
        // Save first User (must be persisted separated by the ORM)
        item.dynamizer.isDynamizer = true;
        mainModel.saveUser(item.dynamizer);

        // Last save VinclesGroup
        groupDAO.save(item);
    }

    public void getInvitation(final AsyncResponse response, long idInvitation) {
        GroupService client = ServiceGenerator.createService(GroupService.class, mainModel.accessToken);
        Call<JsonObject> call = client.getInvitation(idInvitation);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {

                    JsonObject json = result.body();
                    JsonElement groupElement = json.get("group");
                    if (groupElement != null && groupElement.isJsonNull() == false) {
                        VinclesGroup vinclesGroup = VinclesGroup.fromJSON(groupElement.getAsJsonObject());

                        saveGroup(vinclesGroup);

                        getGroupUserServerList(new AsyncResponse() {
                            @Override
                            public void onSuccess(Object result) {
                                response.onSuccess(true);
                            }

                            @Override
                            public void onFailure(Object error) {
                                String errorMessage = mainModel.getErrorByCode(error);
                                Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }, vinclesGroup.getId());
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "getInvitation() - error: " + t.getMessage());
                String errorMessage = mainModel.getErrorByCode(t);
                Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public GlideUrl getGroupPhotoUrlFromGroupId(Long groupId) {
        GlideUrl glideUrl = new GlideUrl(
                ServiceGenerator.getApiBaseUrl() + "API_URL_GROUPS" + groupId + "/photo", new LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer " + MainModel.getInstance().getAccessToken())
                .build());
        return glideUrl;
    }

    public long saveChat(Chat item) {
        // Save first message
        long result = chatDAO.save(item);

        // Later save resource list associated
        for (Resource it : item.resourceTempList) {
            it.chat = item;
            resourceDAO.save(it);
        }
        return result;
    }

    public List<Chat> getChatList(long idChat, boolean preloadImages) {
        Log.i(TAG, "getChatList()");
        List<Chat> items = chatDAO.findByChatId(idChat);
        ResourceModel resourceModel = ResourceModel.getInstance();

        // Populate userFrom for each message
        for (Chat it : items) {
            it.userFrom = mainModel.getUser(it.idUserFrom);
            if (preloadImages)
                resourceModel.loadChatGroupResource(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onFailure(Object error) {

                    }
                }, it);

        }

        return items;
    }

    public List<Chat> getDynamizerChatList() {
        Log.i(TAG, "getChatList()");
        List<Chat> items = chatDAO.findByChatId(currentGroup.idDynamizerChat);

        // Populate userFrom for each message
        for (Chat it : items) {
            it.userFrom = mainModel.getUser(it.idUserFrom);
        }

        return items;
    }

    public void getChatServerList(final AsyncResponse response, Long idChat, String dateFrom, String dateTo) {
        Log.i(TAG, "getChatServerList()");
        ChatService client = ServiceGenerator.createService(ChatService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getChatList(idChat, dateFrom, dateTo);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<Chat> items = new ArrayList<Chat>();
                    for (JsonElement it : jsonArray) {
                        Chat item = Chat.fromJSON(it.getAsJsonObject());

                        // CAUTION: Check if Chat has 'metadataTipus'
                        if (VinclesConstants.hasKnownType(item.metadataTipus)) {
                            item.userFrom = mainModel.getUser(item.idUserFrom);
                            items.add(item);
                        } else {
                            Log.e("getChatServerList", "chat " + item.getId() + "has no metadataTipus or is unknown!");
                        }

                    }
                    saveOrUpdateChatList(items);
                    response.onSuccess(items);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    // Synchronize list
    private void saveOrUpdateChatList(List<Chat> items) {
        for (Chat item : items) {
            saveChat(item);
        }
    }

    public List<User> getUserListByGroup(VinclesGroup group) {
        List<User> result = userDAO.findUserByGroup(group);
        return result;
    }

    public void getGroupUserServerList(final AsyncResponse response, final Long groupId) {
        Log.i(TAG, "getGroupUserServerList()");
        GroupService client = ServiceGenerator.createService(GroupService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getGroupUserList(groupId);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<User> items = new ArrayList<User>();
                    for (JsonElement it : jsonArray) {
                        JsonObject json = it.getAsJsonObject();
                        User item = User.fromJSON(json);
                        items.add(item);
                    }
                    saveOrUpdateUserGroupList(items, groupId);
                    response.onSuccess(items);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    public void getGroupUserServer(final AsyncResponse response, final Long groupId, final Long userId) {
        Log.i(TAG, "getGroupUserServer()");
        GroupService client = ServiceGenerator.createService(GroupService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getGroupUserList(groupId);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<User> items = new ArrayList<User>();
                    for (JsonElement it : jsonArray) {
                        JsonObject json = it.getAsJsonObject();
                        User item = User.fromJSON(json);
                        if (item.getId().longValue() == userId.longValue()) {
                            items.add(item);
                            break;
                        }
                    }
                    saveOrUpdateUserGroupList(items, groupId);
                    response.onSuccess(items);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    // Synchronize list
    private void saveOrUpdateUserGroupList(List<User> items, Long groupId) {
        for (User item : items) {
            saveOrUpdateUserGroup(item, groupId);
        }
    }

    public void saveOrUpdateUserGroup(User item, Long groupId) {
        //CAUTION: Don't override current user!!!
        if (item.getId().longValue() != mainModel.currentUser.getId().longValue()) {
            //CAUTION: Don't override imageName user property!!!
            User user = mainModel.getUser(item.getId());
            if (user != null) {
                item.imageName = user.imageName;
            }
            item.isUserVincles = true;
            mainModel.saveUser(item);
        }

        // Save relation
        UserGroup userGroup = new UserGroup();
        userGroup.userId = item.getId();
        userGroup.groupId = groupId;
        userGroupDAO.save(userGroup);
    }

    // Send message resource & send only message to list of users
    public void sendChatToAll(final AsyncResponse response, final Chat chat, final List<Long> idChatList) {
        Log.i(TAG, "sendMessageToAll()");
        if (chat.resourceTempList.size() > 0) {
            // Synchronize Send of all resources
            List<Resource> items = new ArrayList<Resource>(chat.resourceTempList);
            final List<Resource> idList = new ArrayList<Resource>();

            sendResources(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Update chat with resources loaded with id to save all later
                    chat.resourceTempList = (List<Resource>)result;
                    sendChatToChatList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            // Finally save new chat-chat & its resource list
                            saveChat(chat);

                            response.onSuccess(true);
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.i(TAG, "sendChat() - error: " + error);
                            response.onFailure(error);
                        }
                    }, chat, idChatList);
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "sendMessage() - error: " + error);
                    response.onFailure(error);
                }
            }, items, idList);
        } else {
            // No data, only text
            sendChatToChatList(response, chat, idChatList);
        }
    }

    private void sendResources(final AsyncResponse response, final List<Resource> inputList, final List<Resource> resultList) {
        if (inputList.size() > 0) {
            final Resource item = inputList.get(0);
            inputList.remove(0);
            sendResource(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Update resource id
                    item.setId(Long.parseLong((String) result));

                    resultList.add(item);
                    sendResources(response, inputList, resultList);
                }

                @Override
                public void onFailure(Object error) {
                    response.onFailure(error);
                }
            }, item.data);
        } else {
            response.onSuccess(resultList);
        }
    }

    public void sendResource(final AsyncResponse response, MultipartBody.Part data) {
        Log.i(TAG, "sendResource()");
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        Call<JsonObject> call = client.sendResource(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    String resourceId = json.get("id").getAsString();

                    response.onSuccess(resourceId);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "sendResource() - error: " + t.getMessage());

                // Check for cancel operations!
                if (call.isCanceled()) {
                    response.onFailure(VinclesError.ERROR_CANCEL);
                } else {
                    response.onFailure(t);
                }
            }
        });
    }

    public void sendChatToChatList(final AsyncResponse response, final Chat chat, final List<Long> idChatList) {
        if (idChatList.size() > 0) {
            final Long idChat = idChatList.get(0);
            idChatList.remove(0);

            sendChatToServer(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    sendChatToChatList(response, chat, idChatList);
                }

                @Override
                public void onFailure(Object error) {
                    response.onFailure(error);
                }
            }, idChat, chat);
        } else {
            // Finally save local message and its resource list
            taskModel.saveChat(chat);

            response.onSuccess(true);
        }
    }

    public void sendChatToServer(final AsyncResponse response, final Long idChat, final Chat chat) {
        Log.i(TAG, "sendChatToServer()");
        ChatService client = ServiceGenerator.createService(ChatService.class, mainModel.accessToken);
        Call<JsonObject> call = client.sendChat(idChat, chat.toJSON());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    Long chatId = json.get("id").getAsLong();
                    chat.setId(chatId);
                    response.onSuccess(true);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "sendChatToServer() - error: " + t.getMessage());

                // Check for cancel operations!
                if (call.isCanceled()) {
                    response.onFailure(VinclesError.ERROR_CANCEL);
                } else {
                    response.onFailure(t);
                }
            }
        });
    }

    public Chat getChat(Long id) {
        return chatDAO.get(id);
    }
}