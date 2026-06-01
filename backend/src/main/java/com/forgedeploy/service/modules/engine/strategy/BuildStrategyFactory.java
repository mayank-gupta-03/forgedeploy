package com.forgedeploy.service.modules.engine.strategy;

import com.forgedeploy.service.modules.engine.dto.enums.ProjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BuildStrategyFactory {
    private final Map<ProjectType, BuildStrategy> strategyMap;

    @Autowired
    public BuildStrategyFactory(List<BuildStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(BuildStrategy::getSupportedType, strategy -> strategy));
    }

    public BuildStrategy getStrategy(ProjectType type) {
        return Optional.ofNullable(strategyMap.get(type)).orElseThrow(() -> new IllegalArgumentException("No build strategy found for type: " + type));
    }
}
