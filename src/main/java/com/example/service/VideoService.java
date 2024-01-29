package com.example.service;

import com.example.model.VideoInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class VideoService {
    private YTDLService ytdlService;
    private WhisperService whisperService;
    private GPTService gptService;

    public VideoService(YTDLService ytdlService, WhisperService whisperService, GPTService gptService) {
        this.ytdlService = ytdlService;
        this.whisperService = whisperService;
        this.gptService = gptService;
    }

    public String processVideo(String videoUrl) throws Exception {
        // 生成元数据信息String
        String metadataInfo = ytdlService.generateMetadataInfo(videoUrl);
        // 从元数据信息中提取标题、简介、用户评论
        String title = extractTitle(metadataInfo);
        String description = extractDescription(metadataInfo);
        List<String> comments = extractComments(metadataInfo);

        // 尝试生成VTT文件String
        String vttContent = ytdlService.generateVTTContent(videoUrl);
        Map<Float, String> timestampText;
        String aggregatedText;

        if (vttContent != null) {
            // 从VTT内容提取时间戳文本和聚合文本
            timestampText = extractTimestampTextFromVTT(vttContent);
            aggregatedText = aggregateText(timestampText);
        } else {
            // 生成MP3文件并将其转换为Verbose文件
            File mp3File = ytdlService.generateMP3File(videoUrl);
            try {
                String verboseContent = whisperService.convertToVerbose(mp3File);

                timestampText = extractTimestampTextFromVerbose(verboseContent);
                aggregatedText = aggregateText(timestampText);
            } finally {
                // 删除 MP3 文件
                if (mp3File != null && mp3File.exists()) {
                    boolean isDeleted = mp3File.delete();
                    if (!isDeleted) {
                        // 日志或处理删除失败的情况
                        System.out.println("Failed to delete MP3 file: " + mp3File.getPath());
                    }
                }
            }
        }

        // 创建VideoInfo实例
        VideoInfo videoInfo = new VideoInfo(title, description, comments, timestampText, aggregatedText);

        // 使用GPTService处理
        return gptService.processVideoInfo(videoInfo);
    }

    // 各个提取方法的实现...
    private String extractTitle(String metadataInfo) {
        // 解析 JSON 字符串
        JSONObject jsonObject = new JSONObject(metadataInfo);

        // 返回标题
        return jsonObject.getString("title");
    }

    private String extractDescription(String metadataInfo) {
        // 解析 JSON 字符串
        JSONObject jsonObject = new JSONObject(metadataInfo);

        // 返回描述
        return jsonObject.getString("description");
    }
    private List<String> extractComments(String metadataInfo) {
        // 准备一个列表来存储评论
        List<String> comments = new ArrayList<>();

        // 解析 JSON 字符串
        JSONObject jsonObject = new JSONObject(metadataInfo);

        // 检查是否存在 comments 数组
        if (!jsonObject.has("comments")) {
            return comments; // 返回空列表
        }

        JSONArray commentsArray = jsonObject.getJSONArray("comments");

        // 遍历评论数组
        for (int i = 0; i < commentsArray.length(); i++) {
            JSONObject commentObject = commentsArray.getJSONObject(i);

            // 检查每个评论对象是否有 text 字段
            if (commentObject.has("text")) {
                comments.add(commentObject.getString("text"));
            }
        }

        // 返回评论列表
        return comments;
    }
    private Map<Float, String> extractTimestampTextFromVTT(String vttContent) {
        Map<Float, String> timestampTextMap = new TreeMap<>();
        String[] lines = vttContent.split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("-->")) {
                String fullTimestamp = lines[i].trim();
                String startTime = fullTimestamp.split(" --> ")[0];
                float timestampKey = parseTimeToSeconds(startTime);

                String text = lines[++i].trim();
                timestampTextMap.put(timestampKey, text);
            }
        }

        return timestampTextMap;
    }
    private float parseTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        float seconds = Float.parseFloat(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    private Map<Float, String> extractTimestampTextFromVerbose(String verboseContent) {
        Map<Float, String> timestampTextMap = new TreeMap<>();
        JSONObject jsonObject = new JSONObject(verboseContent);
        JSONArray segments = jsonObject.getJSONArray("segments");

        for (int i = 0; i < segments.length(); i++) {
            JSONObject segment = segments.getJSONObject(i);
            float start = (float) segment.getDouble("start");
            String text = segment.getString("text");

            timestampTextMap.put(start, text);
        }

        return timestampTextMap;
    }

    private String aggregateText(Map<Float, String> timestampText) {

        StringBuilder aggregatedText = new StringBuilder();
        for (String text : timestampText.values()) {
            aggregatedText.append(text).append(" ");
        }

        return aggregatedText.toString().trim();
    }

}
