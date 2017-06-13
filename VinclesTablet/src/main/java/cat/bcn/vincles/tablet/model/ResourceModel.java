/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.business.ResourceService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.ResourceDAO;
import cat.bcn.vincles.lib.dao.ResourceDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourceModel {
    private static final String TAG = "ResourceModel";
    private MainModel mainModel = MainModel.getInstance();
    private TaskModel taskModel = TaskModel.getInstance();
    private boolean initialized;
    private static ResourceModel instance;
    private ResourceDAO resourceDAO;
    public List<Resource> resourceList;

    public ResourceModel() {
    }

    public static ResourceModel getInstance() {
        if (instance == null) {
            instance = new ResourceModel();
            instance.initialize();
        }
        return instance;
    }

    public void initialize() {
        if (!initialized) {
            initialized = true;

            resourceDAO = new ResourceDAOImpl();
        }
    }

    public List<Resource> getLocalResourceList() {
        List<Resource> items = resourceDAO.getActiveResourceList();
        return items;
    }

    public void deleteResource(Resource item) {
        resourceDAO.delete(item);
    }

    // Get local & updated non-data resources (eg.: resources from messages unwatched)
    public void getResourceList(final AsyncResponse response) {
        final List<Resource> items = getLocalResourceList();
        // Get resource from server if not exist
        getDataList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                response.onSuccess(true);
            }

            @Override
            public void onFailure(Object error) {
                // return success also (non update)
                response.onSuccess(true);
            }
        }, items);
    }

    public void saveResource(Resource item) {
        resourceDAO.save(item);
    }

    public void getServerResourceList(final  AsyncResponse response, String dateFrom, String dateTo) {
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getResourceListFromLibrary(dateFrom, dateTo);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<Resource> items = new ArrayList<Resource>();
                    // Parse jsonArray;
                    for (JsonElement it : jsonArray) {
                        Resource item = Resource.fromJSON(it.getAsJsonObject());
                        //CAUTION: check not in local becauseOf inclusionTime is not exact!!!
                        Resource resource = resourceDAO.get(item.getId());
                        if (resource == null) {
                            items.add(item);
                        } else {
                            // Now, yes! update local resource inclusionTime with server value
                            resource.inclusionTime = item.inclusionTime;
                            saveResource(resource);
                        }
                    }
                    getDataList(response, items);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.i(TAG, "sendResource() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    private void getDataList(final AsyncResponse response, final List<Resource> inputList) {
        if (inputList.size() > 0) {
            final Resource item = inputList.get(0);
            inputList.remove(0);

            // Check if resource data exists locally
            File file = null;
            if (item.filename != null) {
                file = new File(VinclesConstants.getImagePath() + item.filename);
            }

            if (file == null || !file.exists()) {
                taskModel.getServerResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // Only for message resources
                        final byte[] data = (byte[]) result;
                        // If message|chat is null resource comes from user library and always is an Image
                        if ((item.chat == null && item.message == null)
                                || item.message.metadataTipus.equals(VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE)
                                || item.chat.metadataTipus.equals(VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE)) {
                            item.filename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
                            item.type = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;

                            VinclesConstants.saveImage(data, item.filename);
                        } else {
                            item.filename = VinclesConstants.VIDEO_PREFIX + new Date().getTime() + VinclesConstants.VIDEO_EXTENSION;
                            item.type = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE;
                            VinclesConstants.saveVideo(data, item.filename);
                        }
                        // Save updated resource
                        taskModel.saveResource(item);

                        getDataList(response, inputList);
                    }

                    @Override
                    public void onFailure(Object error) {
                        // Don't stop!!! Continue with following item
                        getDataList(response, inputList);
                    }
                }, item.getId());
            } else {
                getDataList(response, inputList);
            }
        } else {
            response.onSuccess(true);
        }
    }

    private void getResourceInfo(final AsyncResponse response, Long resourceId) {
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        Call<JsonObject> call = client.getResourceInfo(resourceId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    String mimeType = json.get("mimeType").getAsString();
                    String type = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;
                    if (mimeType.contains(VinclesConstants.VIDEO_EXTENSION.substring(1))) {
                        type = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE;
                    } else if (mimeType.contains(VinclesConstants.AUDIO_EXTENSION.substring(1))) {
                        type = VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE;
                    }
                    response.onSuccess(type);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "sendResource() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void addResourceToLibrary(final AsyncResponse response, Long resourceId) {
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.addResource(resourceId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    response.onSuccess(true);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "addResourceToLibrary() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void deleteResourceFromLibrary(final AsyncResponse response, Long resourceId) {
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.deleteResource(resourceId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    response.onSuccess(true);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "deleteResourceFromLibrary() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void loadChatGroupResource(final AsyncResponse response, final Chat chat) {
        boolean newResource = false;
        if (chat.getResources().size() == 0) {
            if (chat.idContent != null && new ResourceDAOImpl().get(chat.idContent) == null) {
                newResource = true;
            } else  {
                response.onSuccess(new Resource());
            }
        } else {
            Resource tmpResource = new Resource();
            if (!newResource) tmpResource = chat.getResources().get(0);
            final Resource resource = tmpResource;

            boolean existe = false;
            if (resource.filename != null) {
                File file = new File(VinclesConstants.getImagePath() + "/" + resource.filename);
                existe = file.exists();
            }
            if (resource.filename == "" || resource.filename == null || !existe) {
                taskModel.getServerChatResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        byte[] data = (byte[]) result;

                        // Update resource, si ya existe solo guardamos el archivo en disco
                        if (resource.filename == null || !resource.filename.startsWith(VinclesConstants.IMAGE_PREFIX)) {
                            resource.filename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
                            resource.type = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;
                            resource.chat = chat;

                            taskModel.saveChat(chat);
                            taskModel.saveResource(resource);
                        }
                        // Save locally image or data
                        VinclesConstants.saveImage(data, resource.filename);
                        response.onSuccess(resource);
                    }

                    @Override
                    public void onFailure(Object error) {
                        response.onFailure(error);
                    }
                }, chat.idChat, chat.getId());
            }
        }
    }
}