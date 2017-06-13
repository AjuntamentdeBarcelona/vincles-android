/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import java.util.List;
import cat.bcn.vincles.lib.dao.PushMessageDAO;
import cat.bcn.vincles.lib.dao.PushMessageDAOImpl;
import cat.bcn.vincles.lib.vo.PushMessage;

public class PushModel {
    private List<PushMessage> pushMessageList;
    private int messageLimit = 500;
    private PushMessageDAO pushMessageDAO;

    public PushModel() {
        pushMessageDAO = new PushMessageDAOImpl();
    }

    public PushModel load() {
        refresh();
        return this;
    }

    public PushModel refresh() {
        return refresh(messageLimit);
    }

    public PushModel refresh(int limit) {
        pushMessageList = PushMessage.find(PushMessage.class, null, null, null, "ID DESC", "" + limit);
        return this;
    }

    public int count() {
        return pushMessageList.size();
    }

    public List<PushMessage> getList() {
        return pushMessageList;
    }

    public static PushMessage getLastMessage() {
        List<PushMessage> temp = PushMessage.find(PushMessage.class, null, null, null, "ID DESC", "1");
        if (temp  == null || temp.size() == 0) return null;
        else return temp.get(0);
    }

    public void deletePushMesssage(PushMessage item) {
        pushMessageDAO.delete(item);
    }
}
