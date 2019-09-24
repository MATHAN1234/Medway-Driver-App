package com.example.matha.medwayhealth.Remote;

import com.example.matha.medwayhealth.Model.FCMResponse;
import com.example.matha.medwayhealth.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAO6y5dOs:APA91bGecvHO18Vhgbi6Fb1y9a2pIn9oVtQEMoSnRS-g0Zz492GilNMSus2fu4xvOPUvAZFGeQcILc-N7kP0O1Nu3gGmOXR_hxg0kDFhA8Ye4-uinSZobRtEQiII0ebO3KiFIzBOXC61"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}


