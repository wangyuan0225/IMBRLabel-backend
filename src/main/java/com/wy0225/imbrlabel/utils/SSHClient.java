package com.wy0225.imbrlabel.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.wy0225.imbrlabel.config.ServerConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class SSHClient implements AutoCloseable {
    private final Session session;
    private final ServerConfig config;

    public SSHClient(ServerConfig config) throws JSchException {
        this.config = config;

        JSch jsch = new JSch();
        session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
        session.setPassword(config.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
    }

    /**
     * 创建远程目录
     */
    public void createDirectories(List<String> dirs) throws JSchException, SftpException {
        try (SftpChannelWrapper wrapper = new SftpChannelWrapper(session)) {
            ChannelSftp sftp = wrapper.getChannel();
            for (String dir : dirs) {
                createDirectoryRecursively(sftp, dir);
            }
            System.out.println("目录创建成功：" + dirs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     */
    public void uploadFile(MultipartFile file, String remotePath) throws JSchException, SftpException, IOException {
        try (SftpChannelWrapper wrapper = new SftpChannelWrapper(session)) {
            ChannelSftp sftp = wrapper.getChannel();

            // 获取目录路径和文件名
            String remoteDir = remotePath.substring(0, remotePath.lastIndexOf('/'));
            String fileName = remotePath.substring(remotePath.lastIndexOf('/') + 1);

            System.out.println("远程目录: " + remoteDir);
            System.out.println("文件名: " + fileName);

            // 切换到目标目录
            sftp.cd(remoteDir);

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                sftp.put(inputStream, fileName);  // 只使用文件名
                System.out.println("文件上传成功：" + remotePath);
            } catch (Exception e) {
                System.out.println("文件上传失败：" + e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行Python脚本
     */
    public String executePythonScript(int numClicks, Long userId, String taskName, List<List<Integer>> dotList)
            throws JSchException, IOException {
        Channel channel = session.openChannel("exec");

        // 构建Python命令
        String command = buildPythonCommand(numClicks, userId, taskName, dotList);
        System.out.println("构建成功");
        ((ChannelExec) channel).setCommand(command);

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(((ChannelExec) channel).getErrStream()))) {

            channel.connect();

            // 读取标准输出
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);
            }

            // 读取错误输出
            while ((line = errorReader.readLine()) != null) {
                output.append("ERROR: ").append(line).append("\n");
                System.out.println(line);
            }

            return output.toString();
        } finally {
            channel.disconnect();
        }
    }

    /**
     * 递归创建目录
     */
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

    /**
     * 构建Python命令
     */
    private String buildPythonCommand(int numClicks, Long userId, String taskName, List<List<Integer>> dotList)
            throws IOException {
        // 处理dotList参数
        System.out.println("构建命令...");
        String dotListParam = null;
        if (dotList != null && !dotList.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            dotListParam = objectMapper.writeValueAsString(dotList);
        }

        // 构造命令
        String command;
        if (dotListParam == null) {
            command = String.format(
                    "cd %s/code/SAM-Med3D && " +
                            "export CUDA_VISIBLE_DEVICES=5 && " +
                            "%s %s \"%s\" %s %s",
                    config.getBasePath(),
                    config.getPythonEnv(),
                    config.getPythonScript(),
                    numClicks,
                    userId,
                    taskName
            );
        } else {
            command = String.format(
                    "cd %s/code/SAM-Med3D && " +
                            "export CUDA_VISIBLE_DEVICES=5 && " +
                            "%s %s \"%s\" %s %s %s",
                    config.getBasePath(),
                    config.getPythonEnv(),
                    config.getPythonScript(),
                    numClicks,
                    userId,
                    taskName,
                    dotListParam
            );
        }

        System.out.println("执行命令：" + command);
        return command;
    }

    @Override
    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}