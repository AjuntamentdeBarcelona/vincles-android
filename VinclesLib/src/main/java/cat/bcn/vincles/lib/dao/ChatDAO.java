/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.User;

public interface ChatDAO extends GenericDAO<Chat> {
    public List<Chat> findByMeAndUserVincles(User currentUser, User userVincles);
    public List<Chat> findByUserVincles(User user);
    public List<Chat> findByChatId(Long chatId);
}
