/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet;

import android.app.Instrumentation;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import cat.bcn.vincles.lib.dao.GroupDAO;
import cat.bcn.vincles.lib.dao.GroupDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.dao.UserGroupDAO;
import cat.bcn.vincles.lib.dao.UserGroupDAOImpl;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.UserGroup;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(AndroidJUnit4.class)
public class GroupModelInstrumentationTest extends InstrumentationTestCase {
    private static final String TAG = "MainModelInstrumentationTest";
    private Instrumentation instrumentation;
    private MainModel mainModel;
    private GroupModel groupModel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instrumentation = getInstrumentation();
        mainModel = MainModel.getInstance();
        mainModel.initialize(instrumentation.getTargetContext());
        groupModel = GroupModel.getInstance();
    }

    @Test
    public void testSaveUser() {
        User user = new User();
        user.name = "test";
        user.setId(10l);

        Long result = mainModel.saveUser(user);
        assertEquals(result, user.getId());

        UserDAO userDAO = new UserDAOImpl();
        List<User> items = userDAO.getAll();
        assertEquals(1, items.size());
    }

    @Test
    public void getUserListByGroup() {
        UserDAO userDAO = new UserDAOImpl();
        GroupDAO groupDAO = new GroupDAOImpl();
        UserGroupDAO userGroupDAO = new UserGroupDAOImpl();

        User user = new User();
        user.setId(100l);
        user.name = "Test 100";
        userDAO.save(user);

        VinclesGroup group = new VinclesGroup();
        group.setId(101l);
        group.name = "Test 101";
        group.description = "test 101 description";
        groupDAO.save(group);

        UserGroup userGroup = new UserGroup();
        userGroup.groupId = group.getId();
        userGroup.userId = user.getId();
        userGroupDAO.save(userGroup);

        List<User> result = groupModel.getUserListByGroup(group);
        assertEquals(1, result.size());

        userGroupDAO.delete(userGroup);
        groupDAO.delete(group);
        userDAO.delete(user);
    }
}