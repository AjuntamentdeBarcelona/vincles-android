/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskDAOImpl extends GenericDAOImpl<Task> implements TaskDAO {
    public TaskDAOImpl() {
        super(Task.class);
    }

    @Override
    public List<Task> findByRangeDate(Date fromDate, Date toDate, Network network) {
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(fromDate);
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.MINUTE, 0);
        calFrom.set(Calendar.SECOND, 0);
        calFrom.set(Calendar.MILLISECOND, 0);

        Calendar calTo = Calendar.getInstance();
        calTo.setTime(toDate);
        calTo.set(Calendar.HOUR_OF_DAY, 0);
        calTo.set(Calendar.MINUTE, 0);
        calTo.set(Calendar.SECOND, 0);
        calTo.set(Calendar.MILLISECOND, 0);

        String fromDateString = String.valueOf(calFrom.getTime().getTime());
        String toDateString = String.valueOf(calTo.getTime().getTime());

        List<Task> items;
        if (network == null) {
            String[] whereArgs = {fromDateString, toDateString};
            items = Task.find(Task.class, "datetime >= ? AND datetime < ?", whereArgs, null, "datetime ASC", null);
        } else {
            String idNetwork = String.valueOf(network.getId());
            String[] whereArgs = {fromDateString, toDateString, idNetwork};
            items = Task.find(Task.class, "datetime >= ? AND datetime < ? AND network = ?", whereArgs, null, "datetime ASC", null);
        }
        return items;
    }
}