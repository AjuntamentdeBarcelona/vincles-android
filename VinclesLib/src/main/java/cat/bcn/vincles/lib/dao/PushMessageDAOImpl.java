/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import java.util.List;

import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.User;

public class PushMessageDAOImpl extends GenericDAOImpl<PushMessage> implements PushMessageDAO {
    public PushMessageDAOImpl() {
        super(PushMessage.class);
    }
}