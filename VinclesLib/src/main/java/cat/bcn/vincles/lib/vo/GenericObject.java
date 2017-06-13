/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.orm.SugarRecord;

import java.util.Date;

public class GenericObject extends SugarRecord {
    public String title = "";
    public String description = "";
    private Date created = new Date();
    private Date updated = new Date();
    public String createdtime;
    public String updatedtime;
    public Date deleted; // Logic deletion!

    public void GenericObject() {
        // CAUTION: Must be empty constructor!!!
    }

    @Override
    public String toString() {
        return title;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
        this.createdtime = String.valueOf(created.getTime());
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
        this.updatedtime = String.valueOf(updated.getTime());
    }
}
