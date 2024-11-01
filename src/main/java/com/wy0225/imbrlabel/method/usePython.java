package com.wy0225.imbrlabel.method;

import com.wy0225.imbrlabel.context.BaseContext;
import com.wy0225.imbrlabel.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class usePython {

    public static String callPythonScript(String imagePath) {
        // TODO 以下替换
        // Conda 环境名称
        String condaEnvName = "x-anylabeling";

        // Python项目根目录
        String targetDirectory = "E:/wangy/Documents/Javaweb/wy0225/X-AnyLabeling-main/";

        // Python脚本路径（用于生成坐标的脚本）
        String pythonScriptPath = "anylabeling/services/auto_labeling.py";  // 修改为实际的脚本路径

        // 输出坐标文件路径
        String outputPath = "src/main/resources/static/coordinates.txt";

        // 构建命令，添加必要的参数
        String[] command = {
                "cmd", "/c",
                "cd " + targetDirectory + " && " +
                        "conda activate " + condaEnvName + " && " +
                        "python " + pythonScriptPath +
                        " --image_path \"" + imagePath + "\"" +
                        " --output_path \"" + outputPath + "\""
        };

        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);  // 合并错误和标准输出
            Process process = processBuilder.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
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
