package com.forgedeploy.service.modules.engine.strategy;

import com.forgedeploy.service.modules.engine.dto.enums.ProjectType;
import org.springframework.stereotype.Component;

@Component
public class JavaBuildStrategy implements BuildStrategy {
    @Override
    public ProjectType getSupportedType() {
        return ProjectType.JAVA;
    }

    @Override
    public String getDefaultImage() {
        return "maven:3.9.16-eclipse-temurin-25-alpine";
    }

    @Override
    public String getDefaultBuildCommand() {
        return "mvn clean package -DskipTests";
    }

    @Override
    public String getDefaultOutputDirectory() {
        return "target";
    }
}
