/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import cat.bcn.vincles.lib.vo.UserGroup;

public class UserGroupDAOImpl extends GenericDAOImpl<UserGroup> implements UserGroupDAO {
    public UserGroupDAOImpl() {
        super(UserGroup.class);
    }

    @Override
    public Long save(UserGroup item) {
        // CAUTION: compound unique id with userId + groupId to don't duplicate records!!!
        Long uniqueId = Long.parseLong(item.userId + "" + item.groupId);
        item.setId(uniqueId);

        return super.save(item);
    }
}