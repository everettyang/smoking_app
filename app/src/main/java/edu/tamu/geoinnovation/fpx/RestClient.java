package edu.tamu.geoinnovation.fpx;



import android.content.Context;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class RestClient {

    private static RetrofitPM REST_CLIENT;
    private static String ROOT =
//            "http://165.91.120.49"; // aarons comp
            "https://geocreep.tamu.edu";  // server
    private static String TAG = "RestClient";
    private static Set<String> authHeaders = new HashSet<>();
    private static Headers headers;

    static {
//        setupRestClient();
        setupPlainClient();
    }

    private RestClient() {}

    public static RetrofitPM get() {
        return REST_CLIENT;
    }

    public static void setupRestClient() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ROOT)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        REST_CLIENT = retrofit.create(RetrofitPM.class);
    }

    public static void setupPlainClient() {
        CookieJar cookieJar = new CookieJar() {
            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        };
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ROOT)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        REST_CLIENT = retrofit.create(RetrofitPM.class);
    }

    public static void setupRestClient(final Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

//                SharedPreferences preferences = context.getSharedPreferences(LoginActivity.PREFS_NAME, context.MODE_PRIVATE);
                // Customize the request
                Request.Builder builder = original.newBuilder();
                builder.headers(headers);

//                if (authHeaders != null) {
//                    for (String header : authHeaders) {
//                        builder.addHeader("Cookie", header);
//                    }
//                    builder.addHeader("Content-Type", "application/x-www-form-urlencoded");

//                    request = original.newBuilder()
//                            .header("Accept", "application/json")
//                            .header("Authorization", "auth-token")
//                            .method(original.method(), original.body())
//                            .build();


//                } else {
//                    request = original.newBuilder()
//                            .header("Accept", "application/json")
//                            .header("Authorization", "auth-token")
//                            .method(original.method(), original.body())
//                            .build();
//                }
//


                Response response = chain.proceed(builder.build());

                return response;
            }
        });

        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ROOT)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        REST_CLIENT = retrofit.create(RetrofitPM.class);
    }

    public static void setupLoginRestClient(final Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Customize the request
                Request request = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("Authorization", "auth-token")
                        .method(original.method(), original.body())
                        .build();


                Response response = chain.proceed(request);
                List<String> cookies = response.headers("Set-Cookie");
                headers = response.headers();
//                for(String cookie : cookies) {
//                    authHeaders.add(cookie);
////                    Log.d(TAG, cookie);
//                }


                return response;
            }
        });

        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ROOT)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        REST_CLIENT = retrofit.create(RetrofitPM.class);
    }

}
