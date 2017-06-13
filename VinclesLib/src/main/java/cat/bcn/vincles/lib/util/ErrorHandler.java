/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.annotation.Annotation;

import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class ErrorHandler {
    public static VinclesError parseError(Response<?> response) {
        Converter converter = GsonConverterFactory.create().responseBodyConverter(JsonObject.class, new Annotation[0], null);

        VinclesError[] errors = {new VinclesError()};
        JsonObject body = null;
        try {
            body = (JsonObject) converter.convert(response.errorBody());

            JsonElement element = body.get("errors");
            if (element != null && element.isJsonNull() == false) {
                if (element.isJsonArray()) {
                    errors = new GsonBuilder().create().fromJson(element.getAsJsonArray(), VinclesError[].class);
                } else {
                    errors = new GsonBuilder().create().fromJson(element.getAsJsonObject(), VinclesError[].class);
                }
            } else {
                errors[0] = parseSingleError(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errors[0];
    }

    public static VinclesError parseSingleError(JsonObject error) {
        VinclesError result = new VinclesError();
        JsonElement element = error.get("error");
        if (element != null && element.isJsonNull() == false) {
            result.setCode(element.getAsString());
        }
        element = error.get("error_description");
        if (element != null && element.isJsonNull() == false) {
            result.setMessage(element.getAsString());
        }
        return result;
    }
}
