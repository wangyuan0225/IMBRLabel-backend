package com.wy0225.imbrlabel.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PathBuilder {
    @Value("${server.base.path}")
    private String basePath;

    public String buildValidationPath(Long userId, String taskName) {
        return String.format("%s/code/SAM-Med3D/data/validation/%d/%s",
                basePath, userId, taskName);
    }

    public String buildResultPath(Long userId, String taskName,String mode) {
        return String.format("%s/code/SAM-Med3D/visualization/%d/pred/%s/%s",
                basePath, userId, taskName,mode);
    }
}
