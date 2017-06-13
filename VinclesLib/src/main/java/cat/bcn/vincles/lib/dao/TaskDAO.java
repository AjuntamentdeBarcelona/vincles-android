/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.Task;

public interface TaskDAO extends GenericDAO<Task> {
    public List<Task> findByRangeDate(Date fromDate, Date toDate, Network network);
}
