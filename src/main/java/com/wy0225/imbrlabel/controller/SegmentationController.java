package com.wy0225.imbrlabel.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.wy0225.imbrlabel.config.ServerConfig;
import com.wy0225.imbrlabel.context.BaseContext;
import com.wy0225.imbrlabel.pojo.DTO.SegmentationRequestDTO;
import com.wy0225.imbrlabel.pojo.VO.SegmentationResponseVO;
import com.wy0225.imbrlabel.utils.PathBuilder;
import com.wy0225.imbrlabel.utils.SSHClient;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;

@RestController
@RequestMapping("/api/segmentation")
@MultipartConfig
public class SegmentationController {
    private final ServerConfig serverConfig;
    private final PathBuilder pathBuilder;

    @Autowired
    public SegmentationController(ServerConfig serverConfig, PathBuilder pathBuilder) {
        this.serverConfig = serverConfig;
        this.pathBuilder = pathBuilder;
    }

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SegmentationResponseVO> uploadFile(
            @Valid SegmentationRequestDTO request) {
        /**
         * 1. dotList格式：[[65,95,81,0],[90,73,119,0],[70,85,15,0]]   不能有空格
         */
        SegmentationResponseVO response = new SegmentationResponseVO();
        Long userId=BaseContext.getCurrentId();
        System.out.println("dotlist:"+request.getDotList());
        try {
            // 1. 构建目录路径
            String mode=request.getDotList()==null?"auto":"manual";
            String uploadDir = pathBuilder.buildValidationPath(userId, request.getTaskName());
            String resultDir = pathBuilder.buildResultPath(userId, request.getTaskName(),mode);
            System.out.println("uploadDir:"+uploadDir);
            System.out.println("resultDir:"+resultDir);
            try (SSHClient sshClient = new SSHClient(serverConfig)) {
                // 3. 创建必要的目录
                sshClient.createDirectories(Arrays.asList(uploadDir, resultDir));

                // 4. 上传文件
                String remoteFilePath = uploadDir + "/" + request.getFile().getOriginalFilename();
                System.out.println("remoteFilePath:"+remoteFilePath);
                sshClient.uploadFile(request.getFile(), remoteFilePath);

                // 5. 执行Python脚本
                String output = sshClient.executePythonScript(
                        request.getNumClicks(),
                        userId,
                        request.getTaskName(),
                        request.getDotListAsObject()  // 传入的是string，但是要转为list，这里使用转换后的dotList
                );

                response.setSuccess(true);
                response.setMessage("success");
                response.setOutput(output);
                response.setFilePath(resultDir);

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError(e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

    }
}