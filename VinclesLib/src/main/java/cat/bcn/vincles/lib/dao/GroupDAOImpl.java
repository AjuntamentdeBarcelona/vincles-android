/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.VinclesGroup;

public class GroupDAOImpl extends GenericDAOImpl<VinclesGroup> implements GroupDAO {
    public GroupDAOImpl() {
        super(VinclesGroup.class);
    }

    @Override
    public List<VinclesGroup> getActiveList() {
        List<VinclesGroup> items = VinclesGroup.find(VinclesGroup.class, "ACTIVE = 1");
        return items;
    }

    @Override
    public VinclesGroup getGroupByChat(Long idChat) {
        String[] whereArgs = {idChat.toString()};
        List<VinclesGroup> items = VinclesGroup.find(VinclesGroup.class, "DELETED IS NULL AND ID_CHAT = ?", whereArgs, null, null, null);
        VinclesGroup result = null;
        if (items.size()>0) {
            result = items.get(0);
        }
        return result;
    }

    @Override
    public VinclesGroup getGroupByDynamizerChat(Long idDynamizerChat) {
        String[] whereArgs = {idDynamizerChat.toString()};
        List<VinclesGroup> items = VinclesGroup.find(VinclesGroup.class, "DELETED IS NULL AND ID_DYNAMIZER_CHAT = ?", whereArgs, null, null, null);
        VinclesGroup result = null;
        if (items.size()>0) {
            result = items.get(0);
        }
        return result;
    }
}