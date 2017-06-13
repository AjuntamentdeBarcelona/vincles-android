/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.VinclesGroup;

public interface GroupDAO extends GenericDAO<VinclesGroup> {
    public List<VinclesGroup> getActiveList();
    public VinclesGroup getGroupByChat(Long idChat);
    public VinclesGroup getGroupByDynamizerChat(Long idDynamizerChat);
}
