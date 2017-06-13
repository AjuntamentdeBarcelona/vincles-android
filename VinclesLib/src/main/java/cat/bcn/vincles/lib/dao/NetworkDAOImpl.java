/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import com.orm.util.NamingHelper;

import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.Task;

import java.util.List;

public class NetworkDAOImpl extends GenericDAOImpl<Network> implements NetworkDAO {
    public NetworkDAOImpl() {
        super(Network.class);
    }

    public Network findByUserCalendar(long calendarId) {
        Network result = null;
        List<Network> items = Network.findWithQuery(Network.class,
                "SELECT * FROM Network n LEFT JOIN User s WHERE s.id_calendar = ? LIMIT 1", ""+calendarId);
        if (items != null && !items.isEmpty()) {
            result = items.get(0);
        }
        return result;
    }

    public Network findByUserId(long userId) {
        Network result = null;
        List<Network> items = Network.findWithQuery(Network.class,
                "SELECT * FROM " + NamingHelper.toSQLName(Network.class) + " n LEFT JOIN User s " +
                        "WHERE s.id = n.user_vincles AND s.id = ? LIMIT 1", ""+userId);
        if (items != null && !items.isEmpty()) {
            result = items.get(0);
        }
        return result;
    }

    public void removeNetwork(Network net) {
        delete(net);
    }
}