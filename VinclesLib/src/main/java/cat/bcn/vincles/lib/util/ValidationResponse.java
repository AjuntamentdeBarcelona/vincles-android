/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import com.mobsandgeeks.saripaar.ValidationError;

import java.util.List;

public interface ValidationResponse {
    public void onSuccess();

    public void onFailure(List<ValidationError> errors);

    public void onFailure(String error);
}