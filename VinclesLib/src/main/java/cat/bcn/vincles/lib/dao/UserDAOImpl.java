/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.lib.util.Security;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.UserGroup;
import cat.bcn.vincles.lib.vo.VinclesGroup;

public class UserDAOImpl extends GenericDAOImpl<User> implements UserDAO {
    public UserDAOImpl() {
        super(User.class);
    }

    @Override
    public void setPassword(User user, String password) {
        try {
            Security sec = new Security();
            sec.loadPlainAESKey(sec.md5(user.getId().toString()));
            user.cipher = sec.AESencrypt(password);
            user.password = null;
            user.save();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public String getPassword(User user) {
        String pass = "";
        // IF PASSWORD IS FILLED THEN.. WE SHOULD MIGRATE TO NEW SYSTEM
        if (user.password != null && user.password.length() > 0)
            setPassword(user, user.password);

        // USUAL WORK HERE:
        try {
            Security sec = new Security();
            sec.loadPlainAESKey(sec.md5(user.getId().toString()));
            pass = sec.AESdecrypt(user.cipher);
        } catch (Exception e) { e.printStackTrace(); }

        return pass;
    }

    @Override
    public void deactivate(User user) {
        user.active = false;
        user.save();
    }

    @Override
    public List<User> getUserList(User user) {
        String idUser = String.valueOf(user.getId());
        String[] whereArgs = {idUser};

        List<User> items = User.findWithQuery(User.class, "Select t1.* from USER t1" +
                " WHERE t1.id != ? " +
                " AND t1.id NOT IN (SELECT t2.USER_ID FROM USER_GROUP t2 WHERE t2.USER_ID = t1.id)" +
                " AND t1.active > 0" +
                " AND t1.IS_DYNAMIZER = 0" +
                " ORDER BY ID", whereArgs);
        return items;
    }

    @Override
    public List<User> findUserByGroup(VinclesGroup group) {
        List<User> items = User.findWithQuery(User.class, "Select t1.* from USER t1 JOIN USER_GROUP t2 ON t2.GROUP_ID = ? AND t2.USER_ID = t1.ID", group.getId().toString());
        return items;
    }

    @Override
    public boolean isOnlyGroupUser(Long userId) {
        User tmp = User.findById(User.class, userId);
        if (tmp != null) return tmp.isUserVincles;
        else return false;
    }
}