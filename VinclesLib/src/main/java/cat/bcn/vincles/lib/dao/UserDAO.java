/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.VinclesGroup;

public interface UserDAO extends GenericDAO<User> {
    public void deactivate(User user);
    public String getPassword(User user);
    public void setPassword(User user, String password);
    public List<User> getUserList(User user);
    public List<User> findUserByGroup(VinclesGroup group);
    public boolean isOnlyGroupUser(Long user);
}
