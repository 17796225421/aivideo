package com.example.model;

import java.util.List;
import java.util.Map;

public class VideoInfo {
    private String title; // 视频标题
    private String description; // 视频简介
    private List<String> comments; // 用户评论
    private Map<Float, String> timestampText; // 时间戳文本（键为时间戳，值为文本）
    private String aggregatedText; // 聚合文本

    // 构造函数
    public VideoInfo(String title, String description, List<String> comments, Map<Float, String> timestampText, String aggregatedText) {
        this.title = title;
        this.description = description;
        this.comments = comments;
        this.timestampText = timestampText;
        this.aggregatedText = aggregatedText;
    }

    // getter 和 setter 方法
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public Map<Float, String> getTimestampText() {
        return timestampText;
    }

    public void setTimestampText(Map<Float, String> timestampText) {
        this.timestampText = timestampText;
    }

    public String getAggregatedText() {
        return aggregatedText;
    }

    public void setAggregatedText(String aggregatedText) {
        this.aggregatedText = aggregatedText;
    }

    // toString 方法，方便打印和调试
    @Override
    public String toString() {
        return "VideoInfo{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", comments=" + comments +
                ", timestampText=" + timestampText +
                ", aggregatedText='" + aggregatedText + '\'' +
                '}';
    }
}
