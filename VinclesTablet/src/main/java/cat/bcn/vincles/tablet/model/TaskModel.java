/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.business.ChatService;
import cat.bcn.vincles.lib.business.MessageService;
import cat.bcn.vincles.lib.business.ResourceService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.TaskService;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.dao.ChatDAO;
import cat.bcn.vincles.lib.dao.ChatDAOImpl;
import cat.bcn.vincles.lib.dao.MessageDAO;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.ResourceDAO;
import cat.bcn.vincles.lib.dao.ResourceDAOImpl;
import cat.bcn.vincles.lib.dao.TaskDAO;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Communication;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskModel {
    protected MainModel mainModel;
    private static final String TAG = "TaskModel";
    private static TaskModel instance;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    private MessageDAO messageDAO;
    private ChatDAO chatDAO;
    private ResourceDAO resourceDAO;

    public User currentUser;
    public VinclesGroup currentGroup;
    private Message currentMessage;
    private Chat currentChat;
    public Resource currentResource;
    public boolean isGroupAction;
    public boolean isPrivateChat;
    public String code;
    public Boolean resultMessage;
    public String view;
    public Date selectedDate;
    public static final String TASK_SHARE_IMAGE = "taskShareImage";
    public static final String TASK_DELETE_IMAGE = "taskDeleteImage";
    public static final String TASK_USER_RESULT = "taskUserResult";
    public static final String TASK_NETWORK_CODE = "taskNetworkCode";
    public static final String TASK_DELETE_USER = "taskDeleteUser";

    public static final String TASK_MONTH = "taskMonth";

    public static TaskModel getInstance() {
        if (instance == null) {
            instance = new TaskModel();
            instance.initialize();
        }
        return instance;
    }

    private TaskModel() {
    }

    private void initialize() {
        mainModel = MainModel.getInstance();

        view = ""; // TASK_TODAY;
        taskDAO = new TaskDAOImpl();
        userDAO = new UserDAOImpl();
        messageDAO = new MessageDAOImpl();
        chatDAO = new ChatDAOImpl();
        resourceDAO = new ResourceDAOImpl();
        resultMessage = false;
        currentUser = new User();
        selectedDate = new Date();
        currentGroup = new VinclesGroup();
    }

    public List<Resource> getResources() {
        return resourceDAO.getAll();
    }

    public List<User> getUserList() {
        Log.i(TAG, "getUserList()");
        // Get non 'currentUser' user list
        List<User> items = userDAO.getUserList(mainModel.currentUser);
        return items;
    }

    public void getUserServerList(final AsyncResponse response) {
        if (MainModel.avoidServerCalls) return;
        Log.i(TAG, "getUserServerList()");

        UserService client = ServiceGenerator.createService(UserService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getUserVinclesNetworkList();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<User> items = new ArrayList<User>();
                    for (JsonElement item : jsonArray) {
                        JsonObject user = item.getAsJsonObject().get("user").getAsJsonObject();
                        User it = User.fromJSON(user);
                        it.relationship = item.getAsJsonObject().get("relationship").getAsString();
                        items.add(it);
                    }
                    // Sort by Id
                    Collections.sort(items, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return o1.getId().compareTo(o2.getId());
                        }
                    });

                    // Async user list
                    saveOrUpdateUserList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            response.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.i(TAG, "saveOrUpdateUserList() - error: " + error);
                            response.onFailure(error);
                        }
                    }, items, new ArrayList<User>());
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.i(TAG, "getUserNetworkList() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    // Save all user list async
    private void saveOrUpdateUserList(final AsyncResponse response, final List<User> inputList, final List<User> resultList) {
        if (inputList.size() > 0) {
            User item = inputList.get(0);
            inputList.remove(0);
            mainModel.saveUser(item);
            mainModel.getUserPhotoUrlFromUser(item);    // Get First Foto and save as imgName
            resultList.add(item);
            saveOrUpdateUserList(response, inputList, resultList);
        } else {
            response.onSuccess(resultList);
        }
    }

    // Synchronize list
    private void saveOrUpdateMessageList(List<Message> items) {
        // CAUTION: Create only new items!!!
        for (Message item : items) {
            saveOrUpdateMessage(item);
        }
    }

    public void saveOrUpdateMessage(Message item) {
        saveMessage(item);
        updateMessageResources(item, item.resourceTempList);
    }

    private void updateMessageResources(Message item, List<Resource> resList) {
        // Only update new Resources!!!
        for (Resource it : resList) {
            Resource re = resourceDAO.get(it.getId());
            if (re == null) {
                it.message = item;
                saveResource(it);
            }
        }
    }

    public List<Message> getMessageList() {
        Log.i(TAG, "getMessageList()");
        List<Message> items = messageDAO.findByUserVincles(mainModel.currentUser);

        // Populate userFrom for each message
        for (Message it : items) {
            it.userFrom = mainModel.getUser(it.idUserFrom);
        }

        return items;
    }

    public void getMessageServerList(final AsyncResponse response, String dateFrom, String dateTo) {
        if (MainModel.avoidServerCalls) return;
        Log.i(TAG, "getMessageServerList()");
        MessageService client = ServiceGenerator.createService(MessageService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getMessageList(dateFrom, dateTo, 0l); // CAUTION: 0l means from all users!!!
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<Message> items = new ArrayList<Message>();
                    for (JsonElement it : jsonArray) {
                        Message item = Message.fromJSON(it.getAsJsonObject());

                        // CAUTION: Check if Message has 'metadataTipus'
                        if (VinclesConstants.hasKnownType(item.metadataTipus)) {
                            item.userFrom = mainModel.getUser(item.idUserFrom);
                            items.add(item);
                        } else {
                            Log.e("getMessageServerList", "message " + item.getId() + "has no metadataTipus or is unknown!");
                        }

                    }
                    saveOrUpdateMessageList(items);
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

    public void getServerResourceData(final AsyncResponse response, final Long resourceId) {
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.getResource(resourceId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    byte[] data = new byte[0];
                    try {
                        data = IOUtils.toByteArray(result.body().byteStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    response.onSuccess(data);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    public void getServerChatResourceData(final AsyncResponse response, Long chatId, Long id) {
        ChatService client = ServiceGenerator.createService(ChatService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.getChatResource(chatId, id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    byte[] data = new byte[0];
                    try {
                        data = IOUtils.toByteArray(result.body().byteStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    response.onSuccess(data);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    public Message getMessage(Long id) {
        return messageDAO.get(id);
    }

    public long saveMessage(Message item) {
        return messageDAO.save(item);
    }

    public long saveChat(Chat item) {
        return chatDAO.save(item);
    }

    public void saveResource(Resource item) {
        resourceDAO.save(item);
    }

    public List<Task> getTaskList() {
        Log.i(TAG, "getTaskList()");
        return taskDAO.findByNetwork(mainModel.currentNetwork.getId());
    }

    public List<Task> getTodayTaskList() {
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 1);

        return taskDAO.findByRangeDate(calFrom.getTime(), calTo.getTime(), mainModel.currentNetwork);
    }

    public void getTodayTaskServerList(AsyncResponse response) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 1);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);
        getTaskServerList(response, mainModel.currentUser.idCalendar, dateFrom, dateTo);
    }

    public List<Task> getTomorrowTaskList() {
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.add(Calendar.DATE, 1);
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 2);

        return taskDAO.findByRangeDate(calFrom.getTime(), calTo.getTime(), mainModel.currentNetwork);
    }

    public void getTomorrowTaskServerList(AsyncResponse response) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.add(Calendar.DATE, 1);
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 2);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);
        getTaskServerList(response, mainModel.currentUser.idCalendar, dateFrom, dateTo);
    }

    public List<Task> getSelectedTaskList(Date date) {
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(date);
        calFrom = VinclesConstants.getCalendarWithoutTime(calFrom);
        Calendar calTo = Calendar.getInstance();
        calTo.setTime(date);
        calTo = VinclesConstants.getCalendarWithoutTime(calTo);
        calTo.add(Calendar.DATE, 1);

        return taskDAO.findByRangeDate(calFrom.getTime(), calTo.getTime(), mainModel.currentNetwork);
    }

    public void getSelectedTaskServerList(AsyncResponse response, Date date) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(date);
        calFrom = VinclesConstants.getCalendarWithoutTime(calFrom);
        Calendar calTo = Calendar.getInstance();
        calTo.setTime(date);
        calTo = VinclesConstants.getCalendarWithoutTime(calTo);
        calTo.add(Calendar.DATE, 1);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);
        getTaskServerList(response, mainModel.currentUser.idCalendar, dateFrom, dateTo);
    }

    public List<Task> getMonthTask(int month, int year) {
        return getTasksBetweenMonths(month, year, month+1, (month == 12 ? year+1 : year));
    }

    public List<Task> getTasksBetweenMonths(int monthBegining, int yearBegining, int monthEnd, int yearEnd) {
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.set(Calendar.YEAR, yearBegining);
        calFrom.set(Calendar.DATE, 1);
        calFrom.set(Calendar.MONTH, monthBegining);
        Date dateFrom = calFrom.getTime();

        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.set(Calendar.YEAR, yearEnd);
        calTo.set(Calendar.DATE, 1);
        calTo.set(Calendar.MONTH, monthEnd);
        Date dateTo = calTo.getTime();

        return taskDAO.findByRangeDate(dateFrom, dateTo, mainModel.currentNetwork);
    }

    public void getMonthTaskServerList(AsyncResponse response, int month, int year) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.set(Calendar.YEAR, year);
        calFrom.set(Calendar.DATE, 1);
        calFrom.set(Calendar.MONTH, month);

        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.set(Calendar.YEAR, year);
        calTo.set(Calendar.DATE, 1);
        calTo.set(Calendar.MONTH, month + 1);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);

        getTaskServerList(response, mainModel.currentUser.idCalendar, dateFrom, dateTo);
    }

    public void getTaskServerList(final AsyncResponse response, Long calendarId, String dateFrom, String dateTo) {
        if (MainModel.avoidServerCalls) return;
        Log.i(TAG, "getTaskServerList()");
        TaskService client = ServiceGenerator.createService(TaskService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getEventList(calendarId, dateFrom, dateTo);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<Task> items = new ArrayList<Task>();
                    for (JsonElement it : jsonArray) {
                        Task item = Task.fromJSON(it.getAsJsonObject());
                        item.network = mainModel.currentNetwork;
                        User userCreator = User.fromJSON(it.getAsJsonObject().get("userCreator").getAsJsonObject());
                        item.owner = userCreator;
                        items.add(item);
                    }

                    // Sort by date
                    Collections.sort(items, new Comparator<Task>() {
                        @Override
                        public int compare(Task o1, Task o2) {
                            return o1.getDate().compareTo(o2.getDate());
                        }
                    });

                    saveOrUpdateTaskList(items);
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
    private void saveOrUpdateTaskList(List<Task> items) {
        // CAUTION: Create only new items!!!
        for (Task item : items) {
            saveTask(item);
        }
    }

    public void saveTask(Task item) {
        // Save first UserVincles (must be persisted separated by the ORM)
        if (item.owner != null) {
            if (userDAO.get(item.owner.getId()) == null)
                userDAO.save(item.owner);
        }

        item.network = mainModel.currentNetwork;
        taskDAO.save(item);
    }

    public void removeNetworkUser(final AsyncResponse response) {
        Long idUser = currentUser.getId();
        Log.i(TAG, "removeNetworkUser()");
        UserService client = ServiceGenerator.createService(UserService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.deleteUser(idUser);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Log.i(TAG, "result: " + result.body());
                if (result.isSuccessful()) {
//                    userDAO.delete(currentUser);
                    userDAO.deactivate(currentUser);
                    response.onSuccess(true);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "generateCode() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void generateCode(final AsyncResponse response) {
        Log.i(TAG, "associate()");
        UserService client = ServiceGenerator.createService(UserService.class, mainModel.accessToken);
        Call<JsonObject> call = client.generateCode(new JsonObject());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "result: " + result.body());
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    response.onSuccess(json.get("registerCode").getAsString());
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "generateCode() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void markMessageAsWatched(final Communication communication) {
        Log.i(TAG, "markMessageAsWatched()");
        MessageService client = ServiceGenerator.createService(MessageService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.markMessage(communication.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    // Update message 'watched'
                    communication.watched = true;
                    if (communication instanceof Message) {
                        saveMessage((Message)communication);
                    } else {
                        saveChat((Chat)communication);
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    String errorMessage = mainModel.getErrorByCode(errorCode);
                    Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String errorMessage = mainModel.getErrorByCode(t);
                Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void sendMessage(final AsyncResponse response, final Message message) {
        Log.i(TAG, "sendMessage()");
        if (message.resourceTempList.size() > 0) {
            // Synchronize Send of all resources
            List<Resource> items = new ArrayList<Resource>(message.resourceTempList);
            final List<Resource> idList = new ArrayList<Resource>();

            sendResources(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Replace collection without id's
                    message.resourceTempList = idList;
                    sendMessageToServer(response, message);
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "sendMessage() - error: " + error);
                    response.onFailure(error);
                }
            }, items, idList);
        } else {
            // No data, only text
            sendMessageToServer(response, message);
        }
    }

    public void sendMessageToServer(final AsyncResponse response, Message message) {
        Log.i(TAG, "sendMessageToServer()");
        MessageService client = ServiceGenerator.createService(MessageService.class, mainModel.accessToken);
        Call<JsonObject> call = client.sendMessage(message.toJSON());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    String messageId = json.get("id").getAsString();

                    response.onSuccess(messageId);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "sendMessageToServer() - error: " + t.getMessage());

                // Check for cancel operations!
                if (call.isCanceled()) {
                    response.onFailure(VinclesError.ERROR_CANCEL);
                } else {
                    response.onFailure(t);
                }
            }
        });
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

    // Send message resource & send only message to list of users
    public void sendMessageToAll(final AsyncResponse response, final Message message, final List<User> userList) {
        Log.i(TAG, "sendMessageToAll()");
        if (message.resourceTempList.size() > 0) {
            // Synchronize Send of all resources
            List<Resource> items = new ArrayList<Resource>(message.resourceTempList);
            final List<Resource> idList = new ArrayList<Resource>();

            sendResources(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    sendMessageToUserList(response, message, userList);
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "sendMessage() - error: " + error);
                    response.onFailure(error);
                }
            }, items, idList);
        } else {
            // No data, only text
            sendMessageToUserList(response, message, userList);
        }
    }

    public void sendMessageToUserList(final AsyncResponse response, final Message message, final List<User> inputList) {
        if (inputList.size() > 0) {
            final User user = inputList.get(0);
            inputList.remove(0);

            final Message msg = message;
            msg.idUserTo = user.getId();
            sendMessageToServer(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    sendMessageToUserList(response, message, inputList);
                }

                @Override
                public void onFailure(Object error) {
                    response.onFailure(error);
                }
            }, msg);
        } else {
            response.onSuccess(true);
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

    public Task getTAskById(Long taskId) {
        return taskDAO.get(taskId);
    }

    public void acceptTaskServer(final Long taskId, final boolean ok) {
        Task task = taskDAO.get(taskId);
        if (task != null) acceptTaskServer(task, ok);
    }

    public void acceptTaskServer(final Task task, final boolean ok) {
        TaskService client = ServiceGenerator.createService(TaskService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.acceptTask(task.calendarId, task.getId(), ok);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                // Update local 'Task'
                if (ok) {
                    task.state = Task.STATE_ACCEPTED;
                } else {
                    task.state = Task.STATE_REJECTED;
                }
                saveTask(task);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                String errorMessage = mainModel.getErrorByCode(t);
                Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public Communication getComunnication() {
        Communication result = null;
        if (currentChat != null) {
            result = currentChat;
        } else if (currentMessage != null){
            result = currentMessage;
        }
        return result;
    }

    public Chat getCurrentChat() {
        return currentChat;
    }

    // CAUTION: set currentMessage to null
    public void setCurrentChat(Chat currentChat) {
        this.currentChat = currentChat;
        this.currentMessage = null;
    }

    public Message getCurrentMessage() {
        return currentMessage;
    }

    // CAUTION: set currentChat to null
    public void setCurrentMessage(Message currentMessage) {
        this.currentMessage = currentMessage;
        this.currentChat = null;
    }
}
