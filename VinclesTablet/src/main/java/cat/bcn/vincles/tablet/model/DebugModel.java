/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

public class DebugModel {
    private static DebugModel ourInstance = new DebugModel();

    public static DebugModel getInstance() {
        return ourInstance;
    }

    private DebugModel() {
    }
}
