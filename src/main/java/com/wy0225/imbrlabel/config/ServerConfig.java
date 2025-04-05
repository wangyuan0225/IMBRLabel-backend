package com.wy0225.imbrlabel.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data  // 需要getset方法
@Configuration
public class ServerConfig {
    @Value("${server.ssh.host}")
    private String host;

    @Value("${server.ssh.port}")
    private int port;

    @Value("${server.ssh.username}")
    private String username;

    @Value("${server.ssh.password}")
    private String password;

    @Value("${server.base.path}")
    private String basePath;

    @Value("${server.python.env}")
    private String pythonEnv;

    @Value("${server.python.script}")
    private String pythonScript;
}
