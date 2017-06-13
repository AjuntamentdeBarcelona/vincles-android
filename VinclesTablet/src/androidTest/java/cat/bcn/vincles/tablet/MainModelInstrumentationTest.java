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

import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.model.MainModel;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(AndroidJUnit4.class)
public class MainModelInstrumentationTest extends InstrumentationTestCase {
    private static final String TAG = "MainModelInstrumentationTest";
    private Instrumentation instrumentation;
    private MainModel mainModel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instrumentation = getInstrumentation();
        mainModel = MainModel.getInstance();
        mainModel.initialize(instrumentation.getTargetContext());
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
}