/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet;

import android.app.Instrumentation;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.dao.ChatDAO;
import cat.bcn.vincles.lib.dao.ChatDAOImpl;
import cat.bcn.vincles.lib.dao.FeedItemDAO;
import cat.bcn.vincles.lib.dao.FeedItemDAOImpl;
import cat.bcn.vincles.lib.dao.GroupDAO;
import cat.bcn.vincles.lib.dao.GroupDAOImpl;
import cat.bcn.vincles.lib.dao.MessageDAO;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.PushMessageDAO;
import cat.bcn.vincles.lib.dao.PushMessageDAOImpl;
import cat.bcn.vincles.lib.dao.ResourceDAO;
import cat.bcn.vincles.lib.dao.ResourceDAOImpl;
import cat.bcn.vincles.lib.dao.TaskDAO;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.dao.UserGroupDAO;
import cat.bcn.vincles.lib.dao.UserGroupDAOImpl;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.UserGroup;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

@RunWith(AndroidJUnit4.class)
public class UtilInstrumentationTest extends InstrumentationTestCase {
    private static final String TAG = "UtilInstrumentationTest";
    private Instrumentation instrumentation;
    private MainModel mainModel;

    private MessageDAO messageDAO;
    private ChatDAO chatDAO;
    private UserGroupDAO userGroupDAO;
    private ResourceDAO resourceDAO;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instrumentation = getInstrumentation();
        mainModel = MainModel.getInstance();
        mainModel.initialize(instrumentation.getTargetContext());
        messageDAO = new MessageDAOImpl();
        chatDAO = new ChatDAOImpl();
        resourceDAO = new ResourceDAOImpl();
        userGroupDAO = new UserGroupDAOImpl();
    }

    @Test
    public void testDeleteDatabase() {
        //The Android's default system path of your application database.
        String DB_PATH = "/data/data/cat.bcn.vincles.tablet.debug/databases/";
        String DB_NAME = "vincles-tablet.db";

        boolean b = instrumentation.getTargetContext().deleteDatabase(DB_NAME); // true if deleted
        assertTrue(b);
        Log.i(TAG, "deleteDatabase()");
    }

    @Test
    public void testLoadUserDatabase() {
        Date today = new Date();
        mainModel.savePreferences(VinclesTabletConstants.USER_ID, 1l, VinclesConstants.PREFERENCES_TYPE_LONG);

        UserDAO userDAO = new UserDAOImpl();

        User vincles = new User();
        vincles.setId(1l);
        vincles.username = "user1_vincles@vincles-bcn.cat";
        vincles.password = "123456";
        vincles.name = "Test";
        vincles.lastname = "Test Last Name";
        vincles.photoMimeType = "img/png";
        vincles.email = "user1_vincles@vincles-bcn.cat";
        vincles.phone = "123456789";
        vincles.gender = false;
        vincles.liveInBarcelona = true;
        vincles.birthdate = new Date();
        vincles.setCreated(today);
        vincles.setUpdated(today);

        userDAO.save(vincles);

        User it = new User();
        it.setId(2l);
        it.username = "demo@carballares.es";
        it.password = "123456";
        it.name = "Test";
        it.lastname = "Test Last Name";
        it.photoMimeType = "img/png";
        it.email = "user1_vincles@vincles-bcn.cat";
        it.phone = "123456789";
        it.gender = false;
        it.liveInBarcelona = true;
        it.birthdate = new Date();
        it.setCreated(today);
        it.setUpdated(today);

        userDAO.save(it);
    }

    @Test
    public void testLoadMessageDatabase() {
        ResourceDAO resourceDAO = new ResourceDAOImpl();

        // Text Messages
        for (long i = 0; i < 5; i++) {
            Message it = new Message();
            it.setId(i);
            it.text = "test " + i;
            it.idUserFrom = 1l;
            it.idUserTo = 2l;
            it.sendTime = new Date();
            it.metadataTipus = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE.toString();

            messageDAO.save(it);

            Resource resource = new Resource();
            resource.message = it;
            resource.filename = "video_sample.3gp";
            resourceDAO.save(resource);
        }
    }

    @Test
    public void testDeleteMessagesAndResources() {
        testDeleteResources();
        testDeleteChats();
        testDeleteMessages();
    }

    @Test
    public void testDeleteMessages() {
        List<Message> items = messageDAO.getAll();
        for (Message it : items) {
            messageDAO.delete(it);
        }
        Log.i(TAG, "deleteMessages()");
        items = messageDAO.getAll();
        assertTrue(items.size() == 0);
    }

    @Test
    public void testDeleteChats() {
        List<Chat> items = chatDAO.getAll();
        for (Chat it : items) {
            chatDAO.delete(it);
        }
        Log.i(TAG, "deleteChats()");
        items = chatDAO.getAll();
        assertTrue(items.size() == 0);
    }

    @Test
    public void testDeleteResources() {
        ResourceDAO resourceDAO = new ResourceDAOImpl();

        List<Resource> items = resourceDAO.getAll();
        for (Resource it : items) {
            resourceDAO.delete(it);
        }
        Log.i(TAG, "deleteResources()");
        items = resourceDAO.getAll();
        assertTrue(items.size() == 0);
    }

    @Test
    public void testDeleteTask() {
        TaskDAO taskDAO = new TaskDAOImpl();

        List<Task> items = taskDAO.getAll();
        for (Task it : items) {
            taskDAO.delete(it);
        }
        Log.i(TAG, "deleteTasks()");
        items = taskDAO.getAll();
        assertTrue(items.size() == 0);
    }

    @Test
    public void testDeletePushMessage() {
        PushMessageDAO pushMessageDAO = new PushMessageDAOImpl();

        List<PushMessage> items = pushMessageDAO.getAll();
        for (PushMessage it : items) {
            pushMessageDAO.delete(it);
        }
        Log.i(TAG, "deletePushMessages()");
        items = pushMessageDAO.getAll();
        assertTrue(items.size() == 0);
    }

    @Test
    public void testDeleteFeed() {
        FeedItemDAO feedDAO = new FeedItemDAOImpl();

        List<FeedItem> items = feedDAO.getAll();
        for (FeedItem it : items) {
            feedDAO.delete(it);
        }
        Log.i(TAG, "deleteFeed()");
        items = feedDAO.getAll();
        assertTrue(items.size() == 0);
    }

    @Test
    public void testDeleteVinclesGroup() {
        GroupDAO groupDAO = new GroupDAOImpl();

        List<VinclesGroup> items = groupDAO.getAll();
        for (VinclesGroup it : items) {
            groupDAO.delete(it);
        }
        Log.i(TAG, "deleteVinclesGroup()");
        items = groupDAO.getAll();
        assertTrue(items.size() == 0);
    }

    @Test
    public void testActiveGroup() {
        GroupDAO groupDAO = new GroupDAOImpl();

        List<VinclesGroup> items = groupDAO.getAll();
        for (VinclesGroup it : items) {
            it.active = true;
            groupDAO.save(it);
        }
    }

    @Test
    public void testDeleteAllChatByGroup() {
        List<Chat> items = chatDAO.findByChatId(1l);
        for (Chat it : items) {
            chatDAO.delete(it);
        }
    }

    @Test
    public void testAcceptGroup() {
        GroupDAO groupDAO = new GroupDAOImpl();
        
        List<VinclesGroup> items = groupDAO.getAll();
        for (VinclesGroup group : items) {
            group.active = true;
            groupDAO.save(group);
        }
    }

    @Test
    public void testResetUserGroup() {
        List<UserGroup> items = userGroupDAO.getAll();
        for (UserGroup it : items) {
            userGroupDAO.delete(it);
        }
    }

    @Test
    public void testDeleteDynamizer() {
        UserDAO userDAO = new UserDAOImpl();

        List<User> items = userDAO.getAll();
        for (User it : items) {
            if (it.isDynamizer) {
                userDAO.delete(it);
            }
        }
        Log.i(TAG, "deleteDynamizer()");
    }

    @Test
    public void testDeleteImageVincles() {
        File folder = new File(VinclesConstants.getImagePath());
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                f.delete();
            }
        }
        Boolean result = folder.delete();
        assertTrue(result);
        Log.i(TAG, "deleteImageVincles()");
    }

    @Test
    public void testMessageAndChat() {
        // Delete all messages
        List<Message> items = messageDAO.getAll();
        for (Message it : items) {
            messageDAO.delete(it);
        }
        // Delete all chat
        List<Chat> chatItems = chatDAO.getAll();
        for (Chat it : chatItems) {
            chatDAO.delete(it);
        }
        // Delete all resources
        List<Resource> resourceItems = resourceDAO.getAll();
        for (Resource it : resourceItems) {
            resourceDAO.delete(it);
        }

        // Check same id
        Long id = 1000l;
        Message message = new Message();
        message.setId(id);
        message.text = "Message";

        Chat chat = new Chat();
        chat.setId(id);
        chat.text = "Chat";

        messageDAO.save(message);
        chatDAO.save(chat);

        Resource resourceMessage = new Resource();
        resourceMessage.setId(1000l);
        resourceMessage.message = message;
        resourceMessage.type = "Resource of Communication";
        resourceDAO.save(resourceMessage);

        Resource resourceChat = new Resource();
        resourceChat.setId(1001l);
        resourceChat.chat = chat;
        resourceChat.type = "Resource of Chat";
        resourceDAO.save(resourceChat);

        List<Message> messageList = messageDAO.getAll();
        List<Chat> chatList = chatDAO.getAll();
        List<Resource> resourceList = resourceDAO.getAll();
        assertTrue(messageList.size() == 1);
        assertTrue(chatList.size() == 1);

        assertTrue(resourceList.size() == 2);
        Log.i(TAG, "saveMessageAndChat() - same ID");
    }
}