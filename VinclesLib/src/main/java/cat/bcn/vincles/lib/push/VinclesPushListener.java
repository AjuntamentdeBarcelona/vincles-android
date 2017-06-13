/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.push;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.PushMessage;

public interface VinclesPushListener {
    public void onPushMessageReceived(PushMessage pushMessage);
    public void onPushMessageError(long idPush, Throwable t);
}
