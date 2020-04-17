package cat.bcn.vincles.mobile.Client.NetworkUsage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.PostDataUsageRequest;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import okhttp3.ResponseBody;

public class DataUsageUtils implements PostDataUsageRequest.OnResponse, BaseRequest.RenewTokenFailed {

    private Context context;
    private final static String DATA_CONSUMPTION_PREFS = "data_consumption_prefs";
    private final static String DATA_CONSUMPTION_TAG = "data_consumption_tag";
    private final static int TODAY = 1;
    private final static int BEFORE_TODAY = 2;
    private UserPreferences userPreferences;
    private String accessToken;

    public DataUsageUtils(Context context) {
        this.context = context;
        userPreferences = new UserPreferences();
        this.accessToken = userPreferences.getAccessToken();
    }

    public DataUsageUtils() {
        userPreferences = new UserPreferences();
        this.accessToken = userPreferences.getAccessToken();
    }



    public void addDataUsage(String tag, long data, String type){

        if (tag.equals("undefined")){
            return;
        }

        long formatedDateToday = getDataUsageFormatedDate(TODAY);

        DataUsage dataUsage = getTodayDataUsage();


       // Log.d(DATA_CONSUMPTION_TAG, "DATE: " + formatedDateToday);



        //Get item by tag and update
        DataUsageItem dataUsageItem = null;
        for (DataUsageItem item : dataUsage.getDataUsageItems()){
            if (item.getTag().equals(tag)){
                if (type.equals("request")){
                    item.setUp(item.getUp()+data);
                }else if(type.equals("response")){
                    item.setDown(item.getDown()+data);
                }


                Log.d(DATA_CONSUMPTION_TAG, "TAG: " + item.getTag() );
                Log.d(DATA_CONSUMPTION_TAG, "type: " + type );
                Log.d(DATA_CONSUMPTION_TAG, "UP: " + item.getUp() );
                Log.d(DATA_CONSUMPTION_TAG, "DOWN: " + item.getDown() );

                dataUsageItem = item;

            }
        }

        //if it doesn't exist create it and add it to the today array
        if (dataUsageItem == null){
            long up = 0;
            long down = 0;
            if (type.equals("request")){
                up = data;
            }else if(type.equals("response")){
                down = data;
            }
            dataUsageItem = new DataUsageItem(tag, up, down);
            dataUsage.getDataUsageItems().add(dataUsageItem);
        }

        Log.d(DATA_CONSUMPTION_TAG, "TAG: " + dataUsageItem.getTag() );
        Log.d(DATA_CONSUMPTION_TAG, "type: " + type );
        Log.d(DATA_CONSUMPTION_TAG, "UP: " + dataUsageItem.getUp() );
        Log.d(DATA_CONSUMPTION_TAG, "DOWN: " + dataUsageItem.getDown() );

        Gson gson = new Gson();



        //Save today datausages again
        SharedPreferences sharedPrefs = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPrefs.edit();

        //Convert dataUsage to jsonString
        String json = gson.toJson(dataUsage);

        Log.d(DATA_CONSUMPTION_TAG, "___/SAVE/___");
        Log.d(DATA_CONSUMPTION_TAG, "JSON:" + json );

        editor.putString(DATA_CONSUMPTION_PREFS+"-"+ formatedDateToday, json);
        editor.apply();
    }

    private DataUsage getTodayDataUsage() {

        return getTodayDataUsageFromSharedPreferences();
    }

    private DataUsage getTodayDataUsageFromSharedPreferences(){

        long formatedDateToday = getDataUsageFormatedDate(TODAY);


        return getDataUsageFromKey(DATA_CONSUMPTION_PREFS +"-"+ formatedDateToday, formatedDateToday);
    }

    private DataUsage getDataUsageFromKey(String key, long formatedDateToday) {

        SharedPreferences sharedPrefs = getSharedPreferences();

        String json = sharedPrefs.getString(key, "");

        Log.d(DATA_CONSUMPTION_TAG, "___/GET/___");
        Log.d(DATA_CONSUMPTION_TAG, "JSON:" + json );


        return returnFormattedDataUsageFromJsonString(json, formatedDateToday);
    }

    private DataUsage returnFormattedDataUsageFromJsonString(String json, long formatedDateToday) {
        Gson gson = new Gson();
        if (json.equals("")){
            DataUsage dataUsage = new DataUsage();
            dataUsage.setDate(formatedDateToday);
            return dataUsage;
        }
        DataUsage returnDataUsage = new DataUsage();
        try {
            Type type = new TypeToken<DataUsage>() {}.getType();
            returnDataUsage = gson.fromJson(json, type);


        }catch (Exception e){
            returnDataUsage.setDate(formatedDateToday);
            Log.e(getClass().getSimpleName(), "getTodayDataUsageFromSharedPreferences ERROR");
        }

        return returnDataUsage;
    }

    private List<DataUsage> getDataUsageFromSharedPreferences() {

        SharedPreferences sharedPrefs = getSharedPreferences();

        ArrayList<DataUsage> arrayList = new ArrayList<>();

        Map<String, ?> allEntries = sharedPrefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String[] separated = entry.getKey().split("_");
            long time = 0;
            if (separated.length > 0){
                try{
                    time = Long.parseLong(separated[1]);
                }catch (Exception e){
                    Log.d(DATA_CONSUMPTION_TAG, "getDataUsageFromSharedPreferences: ERROR");
                }
            }
            DataUsage dataUsage = returnFormattedDataUsageFromJsonString(entry.getValue().toString(), time);
            arrayList.add(dataUsage);

           // Log.d(getClass().getSimpleName(), entry.getKey() + ": " + entry.getValue().toString());
        }


        return arrayList;
    }

    private SharedPreferences getSharedPreferences() {
        return MyApplication.getAppContext().getSharedPreferences(DATA_CONSUMPTION_PREFS, Activity.MODE_PRIVATE);
    }

    private long getDataUsageFormatedDate(int when) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (when == BEFORE_TODAY){
            calendar.add(Calendar.DATE, -1);
        }

        return calendar.getTimeInMillis();
    }

    public void isDataUsageToSend(){
        ArrayList<DataUsage> dataUsages = getDataUsageBeforeToday();
        if (dataUsages == null){
            if (BuildConfig.DEBUG)Log.d(this.getClass().getSimpleName(), "Data Usage is null");

            return;
        }
        if (dataUsages.size() == 0){
            if (BuildConfig.DEBUG)Log.d(this.getClass().getSimpleName(), "No Data Usage before today found");

            return;
        }


        doRequest(dataUsages);

    }

    private ArrayList<DataUsage> getDataUsageBeforeToday() {
        List<DataUsage> dataUsages = getDataUsageFromSharedPreferences();
        if (dataUsages == null){
            return null;
        }
        long formatedDateBeforeToday = getDataUsageFormatedDate(BEFORE_TODAY);

        ArrayList<DataUsage> usagesToSend = new ArrayList<>();

        for (int i = 0; i< dataUsages.size(); i++){
            if (dataUsages.get(i).getDate() < formatedDateBeforeToday){
                usagesToSend.add(dataUsages.get(i));
            }
        }

        return usagesToSend;
    }

    private void doRequest(ArrayList<DataUsage> dataUsages) {
        for (DataUsage dataUsage : dataUsages){
            PostDataUsageRequest postDataUsageRequest = new PostDataUsageRequest(dataUsage, this);
            postDataUsageRequest.addOnOnResponse(this);
            postDataUsageRequest.doRequest(this.accessToken);
        }

    }

    public void addCallDataUsage(String roomId, long inAudio, long inVideo, long outAudio, long outVideo) {
        Log.d(DATA_CONSUMPTION_TAG, "roomId: " + String.valueOf(roomId) );
        Log.d(DATA_CONSUMPTION_TAG, "inAudio: " + String.valueOf(inAudio) );
        Log.d(DATA_CONSUMPTION_TAG, "inVideo: " + String.valueOf(inVideo) );
        Log.d(DATA_CONSUMPTION_TAG, "outAudio: " + String.valueOf(outAudio) );
        Log.d(DATA_CONSUMPTION_TAG, "outVideo: " + String.valueOf(outVideo) );
        addDataUsage("VideoCall", outAudio+outVideo, "request");
        addDataUsage("VideoCall", inVideo+inAudio, "response");
    }

    public void init() {
        isDataUsageToSend();

        if (BuildConfig.DEBUG){
           printTodayDataUsage();
        }
    }

    private void printTodayDataUsage() {
        DataUsage dataUsage= getTodayDataUsage();

        List<DataUsage> dataUsageList = getDataUsageFromSharedPreferences();
        for (DataUsage usage :  dataUsageList){
            if (usage.getDate() == dataUsage.getDate()){
                Log.d(this.getClass().getSimpleName(), "TODAY: " + usage.getDate());
            }
            else{
                Log.d(this.getClass().getSimpleName(), "DATE: " + usage.getDate());
            }

            for (DataUsageItem dataUsageItem : usage.getDataUsageItems()){
                Log.d(this.getClass().getSimpleName(), "TAG: " + dataUsageItem.getTag() + ", Bytes UP: " + dataUsageItem.getUp()+ ", Bytes DOWN: " + dataUsageItem.getDown());
            }
        }
    }

    @Override
    public void onResponsePostDataUsageRequest(ResponseBody responseBody, long consumeCreatedTime) {
        try {
            Log.d(this.getClass().getSimpleName(), "Success: " + consumeCreatedTime);
            deleteDataUsage(consumeCreatedTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteDataUsage(long consumeCreatedTime) {
        SharedPreferences sharedPrefs = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(DATA_CONSUMPTION_PREFS +"-"+ consumeCreatedTime);
        editor.apply();
    }

    @Override
    public void onFailurePostDataUsageRequest(Object error) {
        Log.d(this.getClass().getSimpleName(), "Error: " + error.toString());
    }


    @Override
    public void onRenewTokenFailed() {
        Log.e(this.getClass().getSimpleName(), "onRenewTokenFailed");
    }
}
