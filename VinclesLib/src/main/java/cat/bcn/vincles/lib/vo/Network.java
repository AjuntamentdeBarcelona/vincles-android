/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.orm.dsl.Ignore;
import com.orm.util.NamingHelper;

public class Network extends GenericObject {
    public String relationship = "";
    public User userVincles = new User();

    @Ignore
    public boolean selected;

    public void Network() {
        // CAUTION: Must be empty constructor!!!
    }

    public static void deleteAllUserRelatedInfo(long userId) {
        String[] whereArgs = {String.valueOf(userId), String.valueOf(userId)};
        Message.deleteAll(Message.class, "ID_USER_TO = ? OR ID_USER_FROM = ?", whereArgs);
        Task.deleteAll(Task.class, "network = ? OR owner= ?", new String[] {""+userId, ""+userId});

    }
}