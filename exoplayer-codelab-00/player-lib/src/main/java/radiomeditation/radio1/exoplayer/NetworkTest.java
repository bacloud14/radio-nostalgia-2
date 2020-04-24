package radiomeditation.radio1.exoplayer;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkTest {

    static boolean resultWeb = true;
    static CountDownLatch countDownLatch = new CountDownLatch(1);
    public static boolean pingTestWeb(String endPoint) throws InterruptedException {

        AndroidNetworking.head(endPoint)
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        // do anything with response
                        Log.i("INFO", "INFO: " + response.code());
                        if (response.isSuccessful()) {
                            resultWeb = true;
                        }else{
                            resultWeb = false;
                        }
                        countDownLatch.countDown();
                    }
                    @Override
                    public void onError(ANError anError) {
                        resultWeb = false;
                        Log.i("Error", "Failed to connect: " + anError.getMessage());
                        countDownLatch.countDown();
                    }
                });
        countDownLatch.await();
        return resultWeb;
    }
}