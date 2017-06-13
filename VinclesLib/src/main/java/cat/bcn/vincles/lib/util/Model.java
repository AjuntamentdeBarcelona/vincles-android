/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import cat.bcn.vincles.lib.push.VinclesPushListener;

public interface Model {
    void updateAccessToken(String token);
    String getAccessToken();
    Long getCurrentUserId();
}
