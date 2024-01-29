package com.example.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class YTDLService {

    private final String tempFolderPath = "C:\\Users\\zhouzihong\\Desktop\\aivideo\\tmp";

    public String generateMetadataInfo(String videoUrl) throws Exception {
        String metadataFilePath = tempFolderPath + "\\" + UUID.randomUUID();
        String command = "yt-dlp -o \"" + metadataFilePath + "\" --write-info-json --skip-download " + videoUrl;

        executeCommand(command);

        metadataFilePath += ".info.json";

        String metadataInfo = new String(Files.readAllBytes(Paths.get(metadataFilePath)));
        Files.delete(Paths.get(metadataFilePath));
        return metadataInfo;
    }

    public String generateVTTContent(String videoUrl) throws Exception {
        if (!isYouTubeUrl(videoUrl)) {
            return null;
        }

        String vttFileBasePath = tempFolderPath + "\\" + UUID.randomUUID();
        String command = "yt-dlp -o \"" + vttFileBasePath + "\" --write-sub --skip-download " + videoUrl;

        executeCommand(command);

        try {
            // 使用通配符寻找匹配的文件
            Path vttFilePath = Files.list(Paths.get(tempFolderPath))
                    .filter(path -> path.toString().startsWith(vttFileBasePath))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException("VTT file not found."));

            String vttContent = new String(Files.readAllBytes(vttFilePath));
            Files.delete(vttFilePath);
            return vttContent;
        } catch (IOException e) {
            // 异常处理
            throw new Exception("Error processing VTT file: " + e.getMessage());
        }
    }


    public File generateMP3File(String videoUrl) throws Exception {
        String mp3FilePath = tempFolderPath + "\\" + UUID.randomUUID() + ".mp3";
        String command = "yt-dlp -o \"" + mp3FilePath + "\" -x --audio-format mp3 --audio-quality 10 " + videoUrl;

        executeCommand(command);

        return new File(mp3FilePath);
    }

    private void executeCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        pb.redirectErrorStream(true);

        Process process = pb.start();
        printProcessOutput(process);
        process.waitFor();
    }

    private void printProcessOutput(Process process) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    private boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be");
    }
}
