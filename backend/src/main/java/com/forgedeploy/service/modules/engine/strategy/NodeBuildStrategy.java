package com.forgedeploy.service.modules.engine.strategy;

import com.forgedeploy.service.modules.engine.dto.enums.ProjectType;
import org.springframework.stereotype.Component;

@Component
public class NodeBuildStrategy implements BuildStrategy {

    @Override
    public ProjectType getSupportedType() {
        return ProjectType.NODE;
    }

    @Override
    public String getDefaultImage() {
        return "node:20-alpine";
    }

    @Override
    public String getDefaultBuildCommand() {
        return "npm clean install && npm run build";
    }
}
