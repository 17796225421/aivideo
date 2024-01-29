package com.example.controller;

import com.example.model.VideoInfo;
import com.example.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoController {

    private VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/processVideo")
    public String processVideo(@RequestBody String videoUrl) {
        try {
            // 调用 VideoService 来处理视频
            return videoService.processVideo(videoUrl);
        } catch (Exception e) {
            // 异常处理
            return "Error processing video: " + e.getMessage();
        }
    }
}
