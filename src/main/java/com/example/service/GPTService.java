package com.example.service;

import com.example.model.VideoInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GPTService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时
            .writeTimeout(60, TimeUnit.SECONDS) // 写入超时
            .readTimeout(120, TimeUnit.SECONDS) // 读取超时
            .build();

    private final String apiUrl = "https://gpts.onechat.fun/v1/chat/completions";
    private final String apiKey = "sk-bpj78lTVxM7Tzjlu27Cd182d28A841B8990bA7415a51B69d";

    public String processVideoInfo(VideoInfo videoInfo) throws IOException {
        JsonObject jsonObject = buildJsonRequestBody(videoInfo);

        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析 JSON 响应
            String responseBody = response.body().string();
            JSONObject responseJson = new JSONObject(responseBody);

            // 提取并返回 content 部分
            return responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }
    }



    private JsonObject buildJsonRequestBody(VideoInfo videoInfo) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", "gpt-4-0125-preview");
        jsonObject.addProperty("stream", false);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "system");
        message.addProperty("content", "You are a helpful assistant.");
        messages.add(message);

        message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", buildContentFromVideoInfo(videoInfo));
        messages.add(message);

        jsonObject.add("messages", messages);
        return jsonObject;
    }

    private String buildContentFromVideoInfo(VideoInfo videoInfo) {
        // 构建请求中的视频信息内容
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Video Title: ").append(videoInfo.getTitle()).append("\n");
        contentBuilder.append("Description: ").append(videoInfo.getDescription()).append("\n");
        contentBuilder.append("Comments: ").append(String.join(", ", videoInfo.getComments())).append("\n");
        contentBuilder.append("Timestamp Text: ").append(videoInfo.getTimestampText().toString()).append("\n");
        contentBuilder.append("Aggregated Text: ").append(videoInfo.getAggregatedText()).append("\n");
        return contentBuilder.toString();
    }
}
