package com.wy0225.imbrlabel.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.wy0225.imbrlabel.context.BaseContext;
import jakarta.servlet.annotation.MultipartConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/segmentation")
@MultipartConfig
public class SegmentationController {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "numClicks", defaultValue = "5") int numClicks,
            @RequestParam("taskName") String taskName) {
        try {
            System.out.println("服务器配置...");
            // 服务器配置信息
            String host = " ";
            String username = "";
            int port = ;
            String password = " ";
            String pythonPath = "/usr/local/anaconda3/bin/python";
            String pythonScript = " ";


            // 构造文件路径：data/validation/{user_id}/{taskname}/.nii.gz
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String remoteBasePath = "/IMBR_Data/Student-home/2023M_ShiGuangze";
            Long userId = BaseContext.getCurrentId();
            String uploadDir = String.format("%s/code/SAM-Med3D/data/validation/%d/%s",
                    remoteBasePath, userId, taskName);
            String remoteFilePath = uploadDir + "/" + originalFilename;

            // 连接服务器
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            System.out.println("服务器连接成功");

            // 1. SFTP上传文件
            Channel channelSftp = session.openChannel("sftp");
            channelSftp.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channelSftp;

            // 递归创建目录
            try {
                createDirectoryRecursively(sftpChannel, uploadDir);
                System.out.println("目录创建成功：" + uploadDir);
            } catch (SftpException e) {
                System.out.println("目录创建失败：" + e.getMessage());
                if (e.id != ChannelSftp.SSH_FX_FAILURE) {
                    throw e;
                }
            }

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                sftpChannel.put(inputStream, remoteFilePath);
                System.out.println("文件上传成功：" + remoteFilePath);
            }
            sftpChannel.disconnect();

            // 2. SSH执行Python命令
            Channel channel = session.openChannel("exec");

            // 构造完整的命令，包括环境激活和目录切换
            String command = String.format("cd /IMBR_Data/Student-home/2023M_ShiGuangze/code/SAM-Med3D && export CUDA_VISIBLE_DEVICES=5 && %s %s \"%s\" %s %s",
                    pythonPath,
                    pythonScript,
                    numClicks,
                    userId,
                    taskName
            );

            System.out.println("执行命令：" + command);

            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            // 获取命令输出
            InputStream in = channel.getInputStream();
            channel.connect();

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);
            }

            // 等待命令执行完成
            channel.disconnect();
            session.disconnect();

            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("message", "File uploaded and processed successfully");
            response.put("output", output.toString());
            return ResponseEntity.ok(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response.toString());
        }
    }

    // 递归创建目录的辅助方法
    private void createDirectoryRecursively(ChannelSftp sftpChannel, String dirPath) throws SftpException {
        String[] dirs = dirPath.split("/");
        String currentPath = "";

        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            currentPath += "/" + dir;
            try {
                sftpChannel.cd(currentPath);
            } catch (SftpException e) {
                try {
                    sftpChannel.mkdir(currentPath);
                } catch (SftpException e2) {
                    if (e2.id != ChannelSftp.SSH_FX_FAILURE) {
                        throw e2;
                    }
                }
            }
        }
    }
}