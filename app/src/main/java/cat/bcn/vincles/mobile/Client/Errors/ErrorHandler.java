package cat.bcn.vincles.mobile.Client.Errors;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import cat.bcn.vincles.mobile.R;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class ErrorHandler {

    /**
     * Creates a VinclesError based on an HTTP response.
     * @param response
     * @return
     */
    public static VinclesError parseError(Response<?> response) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Converter converter = GsonConverterFactory.create(gson).responseBodyConverter(JsonObject.class, new Annotation[0], null);

        VinclesError[] errors = {new VinclesError()};
        JsonObject body = null;

        //Log.d("VIN-566", String.format("ErrorHandler.parseError(): status code %d", response.code()));

        try {
            body = (JsonObject) converter.convert(response.errorBody());

            JsonElement element = body.get("errors");
            if (element != null && !element.isJsonNull()) {
                if (element.isJsonArray()) {
                    errors = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(element.getAsJsonArray(), VinclesError[].class);
                    //Log.d("VIN-566", String.format("ErrorHandler.parseError(): array of %d errors, first is \"%s - %s\"", errors.length, errors[0].getCode(), errors[0].getMessage()));
                } else {
                    errors = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(element.getAsJsonObject(), VinclesError[].class);
                    //Log.d("VIN-566", String.format("ErrorHandler.parseError(): %d errors, first is \"%s - %s\"", errors.length, errors[0].getCode(), errors[0].getMessage()));
                }
            } else {
                errors[0] = parseSingleError(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        errors[0].setHttpStatus(response.code());
        return errors[0];
    }

    /**
     * Creates a VinclesError based on an Throwable.
     * @param throwable
     * @return
     */
    public static VinclesError parseError(Throwable throwable) {
        VinclesError error = new VinclesError();

        // TODO: 5/14/19 Establish what information from the throwable should be passed to the VinclesError object
        error.setMessage(throwable.getMessage());

        return error;
    }

    private static VinclesError parseSingleError(JsonObject error) {
        VinclesError result = new VinclesError();
        JsonElement element = error.get("error");
        if (element != null && !element.isJsonNull()) {
            result.setCode(element.getAsString());
            //Log.d("VIN-566", String.format("ErrorHandler.parseSingleError(): error %s", element.getAsString()));
        }
        element = error.get("error_description");
        if (element != null && !element.isJsonNull()) {
            result.setMessage(element.getAsString());
            //Log.d("VIN-566", String.format("ErrorHandler.parseSingleError(): error description %s", element.getAsString()));
        }
        return result;
    }

    public static String getErrorByCode(Context context, Object error) {
        if (context == null) return "";
        String result/* = context.getResources().getString(R.string.error_default)*/;
        if (error instanceof Exception) {
            result = ((Exception) error).getMessage();
            if (error instanceof SocketTimeoutException) {
                result = context.getResources().getString(R.string.error_connection);
            } else if (error instanceof ConnectException) {
                result = context.getResources().getString(R.string.error_server);
            } else if (error instanceof IOException) {
                if (VinclesError.ERROR_LOGIN.equals(((IOException) error).getMessage())) {
                    result = context.getResources().getString(R.string.error_login);
                }
            } else {
                result = context.getResources().getString(R.string.error_default);
            }
        } else if (error instanceof Error) {
            result = ((Error) error).getMessage();
        } else {
            if (context==null) return "";
            String code = "";
            if (error instanceof VinclesError) {
                code = ((VinclesError)error).getCode();
            } else if (error != null) {
                code = (String) error;
            }
            switch (code) {
                // CODI INCORRECTE
                case "1001":
                    result = context.getResources().getString(R.string.error_1001);
                    break;
                case "1002":
                    result = context.getResources().getString(R.string.error_1002);
                    break;
                case "1003":
                    result = context.getResources().getString(R.string.error_1003);
                    break;
                case "1004":
                    result = context.getResources().getString(R.string.error_1004);
                    break;
                case "1005":
                    result = context.getResources().getString(R.string.error_1005);
                    break;
                case "1006":
                    result = context.getResources().getString(R.string.error_1006);
                    break;
                case "1101":
                    result = context.getResources().getString(R.string.error_1101);
                    break;
                case "1102":
                    result = context.getResources().getString(R.string.error_1102);
                    break;
                case "1103":
                    result = context.getResources().getString(R.string.error_1103);
                    break;
                case "1106":
                    result = context.getResources().getString(R.string.error_1106);
                    break;
                case "1107":
                    result = context.getResources().getString(R.string.error_1107);
                    break;
                case "1108":
                    result = context.getResources().getString(R.string.error_1108);
                    break;
                case "1109":
                    result = context.getResources().getString(R.string.error_1109);
                    break;
                case "1110":
                    result = context.getResources().getString(R.string.error_1110);
                    break;
                case "1111":
                    result = context.getResources().getString(R.string.error_1111);
                    break;
                case "1112":
                    result = context.getResources().getString(R.string.error_1112);
                    break;
                case "1113":
                    result = context.getResources().getString(R.string.error_1113);
                    break;
                case "1114":
                    result = context.getResources().getString(R.string.error_1114);
                    break;
                case "1301":
                    result = context.getResources().getString(R.string.error_1301);
                    break;
                case "1302":
                    result = context.getResources().getString(R.string.error_1302);
                    break;
                case "1310":
                    result = context.getResources().getString(R.string.error_1310);
                    break;
                case "1320":
                    result = context.getResources().getString(R.string.error_1320);
                    break;
                case "1321":
                    result = context.getResources().getString(R.string.error_1321);
                    break;
                case "1322":
                    result = context.getResources().getString(R.string.error_1322);
                    break;
                case "1401":
                    result = context.getResources().getString(R.string.error_1401);
                    break;
                case "1402":
                    result = context.getResources().getString(R.string.error_1402);
                    break;
                case "1501":
                    result = context.getResources().getString(R.string.error_1501);
                    break;
                case "1601":
                    result = context.getResources().getString(R.string.error_1601);
                    break;
                case "1602":
                    result = context.getResources().getString(R.string.error_1602);
                    break;
                case "1603":
                    result = context.getResources().getString(R.string.error_1603);
                    break;
                case "1604":
                    result = context.getResources().getString(R.string.error_1604);
                    break;
                case "1605":
                    result = context.getResources().getString(R.string.error_1605);
                    break;
                case "1606":
                    result = context.getResources().getString(R.string.error_1606);
                    break;
                case "1607":
                    result = context.getResources().getString(R.string.error_1607);
                    break;
                case "1701":
                    result = context.getResources().getString(R.string.error_1701);
                    break;
                case "1702":
                    result = context.getResources().getString(R.string.error_1702);
                    break;
                case "1703":
                    result = context.getResources().getString(R.string.error_1703);
                    break;
                case "1704":
                    result = context.getResources().getString(R.string.error_1704);
                    break;
                case "1801":
                    result = context.getResources().getString(R.string.error_1801);
                    break;
                case "1802":
                    result = context.getResources().getString(R.string.error_1802);
                    break;
                case "1901":
                    result = context.getResources().getString(R.string.error_1901);
                    break;
                case "1902":
                    result = context.getResources().getString(R.string.error_1902);
                    break;
                case "1903":
                    result = context.getResources().getString(R.string.error_1903);
                    break;
                case "1904":
                    result = context.getResources().getString(R.string.error_1904);
                    break;
                case "1905":
                    result = context.getResources().getString(R.string.error_1905);
                    break;
                case "1906":
                    result = context.getResources().getString(R.string.error_1906);
                    break;
                case "1907":
                    result = context.getResources().getString(R.string.error_1907);
                    break;
                case "1908":
                    result = context.getResources().getString(R.string.error_1908);
                    break;
                case "1909":
                    result = context.getResources().getString(R.string.error_1909);
                    break;
                case "1910":
                    result = context.getResources().getString(R.string.error_1910);
                    break;
                case "1911":
                    result = context.getResources().getString(R.string.error_1911);
                    break;
                case "2001":
                    result = context.getResources().getString(R.string.error_2001);
                    break;
                case "2002":
                    result = context.getResources().getString(R.string.error_2002);
                    break;
                case "2003":
                    result = context.getResources().getString(R.string.error_2003);
                    break;
                case "2004":
                    result = context.getResources().getString(R.string.error_2004);
                    break;
                case "2005":
                    result = context.getResources().getString(R.string.error_2005);
                    break;
                case "2006":
                    result = context.getResources().getString(R.string.error_2006);
                    break;
                case "2007":
                    result = context.getResources().getString(R.string.error_2007);
                    break;
                case "2008":
                    result = context.getResources().getString(R.string.error_2008);
                    break;
                case "2009":
                    result = context.getResources().getString(R.string.error_2009);
                    break;
                case "2101":
                    result = context.getResources().getString(R.string.error_2101);
                    break;
                case "2102":
                    result = context.getResources().getString(R.string.error_2102);
                    break;
                case "2103":
                    result = context.getResources().getString(R.string.error_2103);
                    break;
                case "2104":
                    result = context.getResources().getString(R.string.error_2104);
                    break;
                case "2105":
                    result = context.getResources().getString(R.string.error_2105);
                    break;
                case "2106":
                    result = context.getResources().getString(R.string.error_2106);
                    break;
                case "2201":
                    result = context.getResources().getString(R.string.error_2201);
                    break;
                case "2601":
                    result = context.getResources().getString(R.string.error_2601);
                    break;
                case "2602":
                    result = context.getResources().getString(R.string.error_2602);
                    break;
                case "2701":
                    result = context.getResources().getString(R.string.error_2701);
                    break;
                case "2801":
                    result = context.getResources().getString(R.string.error_2801);
                    break;
                case "2802":
                    result = context.getResources().getString(R.string.error_2802);
                    break;
                case "2803":
                    result = context.getResources().getString(R.string.error_2803);
                    break;
                case "2804":
                    result = context.getResources().getString(R.string.error_2804);
                    break;
                case "invalid_grant":
                    result = context.getResources().getString(R.string.error_login);
                    break;
                default: // VinclesError.ERROR_DEFAULT
                    result = context.getResources().getString(R.string.error_default);
                    break;
            }
        }

        return result;
    }
}
