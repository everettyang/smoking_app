package edu.tamu.geoinnovation.fpx;


import edu.tamu.geoinnovation.fpx.Modules.AppModule;
import edu.tamu.geoinnovation.fpx.Modules.AppModuleResponse;
import edu.tamu.geoinnovation.fpx.Modules.BasicServerResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

public interface RetrofitPM {
    @FormUrlEncoded
    @POST("geocreep.tamu.edu/Rest/Login/")
    Call<BasicServerResponse> loginUserLocal(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("Rest/Login/")
    Call<BasicServerResponse> loginUserServer(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("geocreep.tamu.edu/Rest/SensorData/PushSteps/")
    Call<BasicServerResponse> pushSensorDataFlatLocal(@Field("collection") String collectionName, @Field("json") String jsonRepresentation);

    @FormUrlEncoded
    @POST("geocreep.tamu.edu/Rest/SensorData/PushBatch/")
    Call<BasicServerResponse> pushSensorDataBatchLocal(@Field("collection") String collectionName, @Field("json") String jsonRepresentation);

    @FormUrlEncoded
    @POST("Rest/SensorData/PushSteps/")
    Call<BasicServerResponse> pushSensorDataFlatServer(@Field("collection") String collectionName, @Field("json") String jsonRepresentation);

    @FormUrlEncoded
    @POST("Rest/SensorData/PushSteps/")
    Call<BasicServerResponse> pushSignup(@Field("email") String email, @Field("password") String password, @Field("uin") String uin, @Field("firstName") String firstName, @Field("lastName") String lastName);

    @FormUrlEncoded
    @POST("Rest/SensorData/PushSteps/")
    Observable<BasicServerResponse> pushSensorDataFlatServerObservable(@Field("collection") String collectionName, @Field("json") String jsonRepresentation);

    @GET("Rest/Apps/Get/")
    Call<AppModuleResponse> getAppGuidsServer();

    @GET("geocreep.tamu.edu/Rest/Apps/Get/")
    Call<AppModuleResponse> getAppGuidsLocal();

}






