package com.wy0225.imbrlabel.method;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class usePython {

    public static String callPythonScript(String imagePath) {
        // TODO 以下替换
        // Conda 环境名称
        String condaEnvName = "sam";

        // Python项目根目录
        String targetDirectory = "E:/wangy/Documents/Python/segment-anything-main/";

        // Python脚本路径（用于生成坐标的脚本）
        String pythonScriptPath = "5_predictor_multimask_param.py";  // 修改为实际的脚本路径

        // 输入图片的目录
        String imageDirectory = Paths.get(imagePath).getParent().toString();

        // 输入图片的文件名
        String imageFileName = Paths.get(imagePath).getFileName().toString();

        // 输出坐标文件路径，与输入图片在同一目录，并以相同名称但不同的扩展名保存
        String outputExtension = ".txt"; // 输出文件的扩展名
        String outputPath = imageDirectory + "/" + imageFileName.replaceFirst("\\.[^\\.]+$", "") + outputExtension;

        // 规范化路径格式
        targetDirectory = targetDirectory.replace("\\", "/");
        imagePath = imagePath.replace("\\", "/");

        // 修改命令数组的构建方式
        String[] command = {
                "cmd.exe",
                "/c",
                "D:/Anaconda3/Scripts/activate.bat" + " && " +
                        "cd /d \"" + targetDirectory + "\" && " +
                        "conda activate " + condaEnvName + " && " +
                        "python \"" + pythonScriptPath + "\"" +
                        " --image_path \"" + imagePath + "\""
        };

        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);  // 合并错误和标准输出
            Process process = processBuilder.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("Python输出: " + line);
                }
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Python脚本执行失败，退出码: " + exitCode);
                throw new RuntimeException("Python脚本执行失败");
            }

            // 检查输出文件是否生成
            if (!Files.exists(Paths.get(outputPath))) {
                throw new RuntimeException("坐标文件未生成");
            }

            return outputPath;

        } catch (IOException | InterruptedException e) {
            log.error("执行Python脚本失败", e);
            throw new RuntimeException("执行Python脚本失败: " + e.getMessage());
        }
    }
}
