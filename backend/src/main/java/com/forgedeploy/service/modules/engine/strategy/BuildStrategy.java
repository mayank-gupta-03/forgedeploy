package com.forgedeploy.service.modules.engine.strategy;

import com.forgedeploy.service.modules.engine.dto.enums.ProjectType;

public interface BuildStrategy {
    ProjectType getSupportedType();

    String getDefaultImage();

    String getDefaultBuildCommand();
}
