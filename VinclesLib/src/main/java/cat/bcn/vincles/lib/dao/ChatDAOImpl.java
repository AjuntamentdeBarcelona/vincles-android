/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.User;

public class ChatDAOImpl extends GenericDAOImpl<Chat> implements ChatDAO {
    public ChatDAOImpl() {
        super(Chat.class);
    }

    @Override
    public List<Chat> getAll() {
        List<Chat> items = Chat.find(Chat.class, null, null, null, "SEND_TIME DESC", null);
        return items;
    }

    @Override
    public List<Chat> findByMeAndUserVincles(User currentUser, User userVincles) {
        String currentUserId = String.valueOf(currentUser.getId());
        String userVinclesId = String.valueOf(userVincles.getId());
        String[] whereArgs = {currentUserId, userVinclesId};
        List<Chat> items = Chat.find(Chat.class, "ID_USER_TO = ? AND ID_USER_FROM = ?", whereArgs, null, "SEND_TIME DESC", null);
        return items;
    }

    @Override
    public List<Chat> findByUserVincles(User user) {
        String idUser = String.valueOf(user.getId());
        String[] whereArgs = {idUser};
        List<Chat> items = Chat.find(Chat.class, "ID_USER_TO = ?", whereArgs, null, "SEND_TIME DESC", null);
        return items;
    }

    @Override
    public List<Chat> findByChatId(Long id) {
        String[] whereArgs = {id.toString()};
        List<Chat> items = Chat.find(Chat.class, "ID_CHAT = ?", whereArgs, null, "ID" +
                " DESC", null);
        return items;
    }
}