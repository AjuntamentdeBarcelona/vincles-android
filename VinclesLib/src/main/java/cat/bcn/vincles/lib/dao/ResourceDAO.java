/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;

public interface ResourceDAO extends GenericDAO<Resource> {
    public List<Resource> getActiveResourceList();
}
