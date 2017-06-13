/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.User;

public class MessageDAOImpl extends GenericDAOImpl<Message> implements MessageDAO {
    public MessageDAOImpl() {
        super(Message.class);
    }

    @Override
    public List<Message> getAll() {
        List<Message> items = Message.find(Message.class, null, null, null, "SEND_TIME DESC", null);
        return items;
    }

    @Override
    public List<Message> findByMeAndUserVincles(User currentUser, User userVincles) {
        String currentUserId = String.valueOf(currentUser.getId());
        String userVinclesId = String.valueOf(userVincles.getId());
        String[] whereArgs = {currentUserId, userVinclesId};
        List<Message> items = Message.find(Message.class, "ID_USER_TO = ? AND ID_USER_FROM = ?", whereArgs, null, "SEND_TIME DESC", null);
        return items;
    }

    @Override
    public List<Message> findByUserVincles(User user) {
        String idUser = String.valueOf(user.getId());
        String[] whereArgs = {idUser};
        List<Message> items = Message.find(Message.class, "ID_USER_TO = ?", whereArgs, null, "SEND_TIME DESC", null);
        return items;
    }
}