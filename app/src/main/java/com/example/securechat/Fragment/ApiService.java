package com.example.securechat.Fragment;

import com.example.securechat.Notification.MyResponse;
import com.example.securechat.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAoy8QGTk:APA91bEU3iUocdBhLClWw2pnqP6Cmbh-tciQGHJUFwmFeib9BzPMpDa_HnZ4W5mnpJFdCQTMShJLMEQbbyPWL88YUVjvr5aKsitQgKHapdjMFz30Pug3YtYRcop5s96PYbJjXBSX0zff"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> snedNotification(@Body Sender body);
}
