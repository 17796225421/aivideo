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

    private final String apiUrl = "https://api.onechat.fun/v1/chat/completions";
    private final String apiKey = "sk-c25d32ec3a3f64cec6d36b8da55449dac97301dfbd46fc0c";

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

            String responseBody = response.body().string();
            JSONObject responseJson = new JSONObject(responseBody);

            String gptContent = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            String timestampContent = buildTimestampContent(videoInfo);

            return "GPT 内容:\n" + gptContent + "\n\n视频详细时间线:\n" + timestampContent;
        }
    }

    private String buildTimestampContent(VideoInfo videoInfo) {
        StringBuilder timestampBuilder = new StringBuilder();
        videoInfo.getTimestampText().forEach((timestamp, text) ->
                timestampBuilder.append(formatTimestamp(String.valueOf(timestamp))).append(" ").append(text).append("\n"));
        return timestampBuilder.toString();
    }

    private String formatTimestamp(String timestamp) {
        // 这里可以根据具体的时间戳格式进行调整
        // 假设时间戳是以秒为单位的，我们需要将其转换为小时:分钟:秒的格式
        int totalSeconds = Integer.parseInt(timestamp);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }




    private JsonObject buildJsonRequestBody(VideoInfo videoInfo) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", "gpt-4-all");
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
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("视频标题: ").append(videoInfo.getTitle()).append("\n\n");
        contentBuilder.append("描述: ").append(videoInfo.getDescription()).append("\n\n");

        if (!videoInfo.getComments().isEmpty()) {
            contentBuilder.append("一些关键评论: ").append(String.join("; ", videoInfo.getComments())).append("\n\n");
        }

        if (!videoInfo.getTimestampText().isEmpty()) {
            contentBuilder.append("时间戳及对应文本摘要:\n");
            videoInfo.getTimestampText().forEach((timestamp, text) ->
                    contentBuilder.append("在 ").append(timestamp).append(" - ").append(text).append("\n"));
        }

        if (!videoInfo.getAggregatedText().isEmpty()) {
            contentBuilder.append("\n视频聚合文本:\n").append(videoInfo.getAggregatedText()).append("\n");
        }

        // 提示 GPT 模型按照特定格式回答
        contentBuilder.append("\n基于以上信息，请用中文提供一个关于整个视频内容的综合总结，接着按时间段分析视频中的重要事件或段落。" +
                "请确保分析清晰、准确，且明确指出每个时间段的主要内容。");
        return contentBuilder.toString();
    }


}
