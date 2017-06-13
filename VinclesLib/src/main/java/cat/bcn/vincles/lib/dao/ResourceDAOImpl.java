/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;

public class ResourceDAOImpl extends GenericDAOImpl<Resource> implements ResourceDAO {
    public ResourceDAOImpl() {
        super(Resource.class);
    }

    //CAUTION: Only Resources of Message (no Chat)!!!
    @Override
    public List<Resource> getActiveResourceList() {
        List<Resource> items = Resource.findWithQuery(Resource.class,
                "SELECT t1.* FROM RESOURCE t1" +
                        " WHERE t1.CHAT IS 0" +
                        " ORDER BY INCLUSION_TIME DESC");
        return items;
    }
}