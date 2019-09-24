package com.example.matha.medwayhealth.Common;

import android.location.Location;

import com.example.matha.medwayhealth.Model.User;
import com.example.matha.medwayhealth.Remote.FCMClient;
import com.example.matha.medwayhealth.Remote.IFCMService;
import com.example.matha.medwayhealth.Remote.IGoogleAPI;
import com.example.matha.medwayhealth.Remote.RetrofitClient;


/**
 * Created by HP on 12/8/2017.
 */

public class Common {



    public static final String drivers="Drivers";
    public static final String user_drivers="DriversInformation";
    public static final String user_rider="RidersInformation";
    public static final String pickup_request="PickupRequest";
    public static final String token_tbl="Tokens";
    public static User currentUser;
    public static final String UserField="usr";
    public static final String PasField="pwd";
    public static Location mLastLocation=null;


    public static final String baseURL="https://maps.googleapis.com";
    public static final String fcmURL="https://fcm.googleapis.com/";


    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }


}
