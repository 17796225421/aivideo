package com.example.service;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class WhisperService {

    private final OkHttpClient client;
    private final String apiUrl = "https://gpts.onechat.fun/v1/audio/transcriptions";
    private final String apiKey = "sk-bpj78lTVxM7Tzjlu27Cd182d28A841B8990bA7415a51B69d";

    public WhisperService() {
        // 初始化OkHttpClient，并设置超时时间
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // 连接超时时间
                .writeTimeout(60, TimeUnit.SECONDS)   // 写入超时时间
                .readTimeout(120, TimeUnit.SECONDS)   // 读取超时时间
                .build();
    }

    public String convertToVerbose(File mp3File) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", mp3File.getName(),
                        RequestBody.create(mp3File, MediaType.parse("audio/mp3")))
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("response_format", "verbose_json")
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 返回响应体的字符串，即转换后的verbose文件内容
            return response.body().string();
        }
    }
}
