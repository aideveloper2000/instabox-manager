package com.instabox.manager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://api.instabox.sh/";

    public static ApiService createApiService(String apiKey) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ApiKeyInterceptor(apiKey))
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }

    static class ApiKeyInterceptor implements Interceptor {
        private final String apiKey;

        ApiKeyInterceptor(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        }
    }
}
