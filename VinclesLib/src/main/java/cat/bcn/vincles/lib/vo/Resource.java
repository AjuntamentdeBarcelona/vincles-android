/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orm.dsl.Ignore;

import java.util.Date;

import cat.bcn.vincles.lib.util.VinclesConstants;
import okhttp3.MultipartBody;

public class Resource extends GenericObject {
    public String type;
    public String mimeType;
    public Message message;  //CAUTION: SugarORM don't allow Inheritance 'table per subclass'
    public Chat chat; //CAUTION: SugarORM don't allow Inheritance 'table per subclass'
    public String filename;
    public Date inclusionTime = new Date();

    @Ignore
    public MultipartBody.Part data;
    @Ignore
    public Bitmap bitmap;

    public void Resource() {
        // CAUTION: Must be empty constructor!!!
    }

    public static Resource fromJSON(JsonObject json) {
        Resource resource = new Resource();
        resource.setId(json.get("id").getAsLong());

        // Control Json null properties
        JsonElement typeElement = json.get("type");
        if (typeElement != null && typeElement.isJsonNull() == false) {
            resource.type = typeElement.getAsString();
        }
        JsonElement mimeTypeElement = json.get("mimeType");
        if (mimeTypeElement != null && mimeTypeElement.isJsonNull() == false) {
            resource.mimeType = mimeTypeElement.getAsString();
        }
        JsonElement filenameElement = json.get("filename");
        if (filenameElement != null && filenameElement.isJsonNull() == false) {
            resource.filename = filenameElement.getAsString();
        }
        JsonElement inclusionTimeElement = json.get("inclusionTime");
        if (inclusionTimeElement != null && inclusionTimeElement.isJsonNull() == false) {
            resource.inclusionTime = new Date(inclusionTimeElement.getAsLong());
        }

        return resource;
    }

    @Override
    public boolean equals(Object o) {
        boolean ret = false;
        try {
            if (o instanceof Resource) {
                ret = (((Resource)o).getId().longValue() == this.getId().longValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }
}
