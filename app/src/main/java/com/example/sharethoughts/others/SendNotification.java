package com.example.sharethoughts.others;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sharethoughts.network.ApiClient;
import com.example.sharethoughts.network.ApiService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendNotification {


    public static final String REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content-Type";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS="registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;


    private final Context context;

    public SendNotification(Context context){
        this.context = context;
    }

    private void showToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                getRemoteMsgHeaders(),messageBody
        ).enqueue(new Callback<String>(){
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure")==1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification send successfully");
                }else{
                    showToast("Errorr : "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    public static HashMap<String, String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders= new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAjZiNUek:APA91bEfGwZx0LgAQbFjiQnePbcNeqUbwm62ZAf_ldeEDYG1rMQTcF9kmYjeLhSfw36JEqN2UnoWYGA8Ja2iCoyHtqlEXw6o8BRrvhavQy7ydKFT-oFIzQ37l39Dqc3FSUAe5202MC-q"
                );
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
                );
        }
        return remoteMsgHeaders;
    }

}
