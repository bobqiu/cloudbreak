package com.sequenceiq.cloudbreak.controller.validation.environment;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.sequenceiq.cloudbreak.api.model.rds.RDSConfigJson;
import com.sequenceiq.cloudbreak.api.model.stack.cluster.ClusterRequest;
import com.sequenceiq.cloudbreak.controller.validation.ValidationResult;
import com.sequenceiq.cloudbreak.domain.LdapConfig;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.domain.environment.EnvironmentAwareResource;
import com.sequenceiq.cloudbreak.domain.environment.Region;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.view.EnvironmentView;
import com.sequenceiq.cloudbreak.service.ldapconfig.LdapConfigService;
import com.sequenceiq.cloudbreak.service.proxy.ProxyConfigService;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;

@Component
public class ClusterCreationEnvironmentValidator {
    @Inject
    private ProxyConfigService proxyConfigService;

    @Inject
    private LdapConfigService ldapConfigService;

    @Inject
    private RdsConfigService rdsConfigService;

    public ValidationResult validate(ClusterRequest clusterRequest, Stack stack) {
        ValidationResult.ValidationResultBuilder resultBuilder = ValidationResult.builder();
        EnvironmentView stackEnv = stack.getEnvironment();
        if (stackEnv != null && !CollectionUtils.isEmpty(stackEnv.getRegionSet())
                && stackEnv.getRegionSet().stream().noneMatch(region -> region.getName().equals(stack.getRegion()))) {
            resultBuilder.error(String.format("[%s] region is not enabled in [%s] environment. Enabled environments: [%s]", stack.getRegion(),
                    stackEnv.getName(), stackEnv.getRegionSet().stream().map(Region::getName).sorted().collect(Collectors.joining(","))));
        }
        Long workspaceId = stack.getWorkspace().getId();
        validateLdapConfig(workspaceId, clusterRequest, stackEnv, resultBuilder);
        validateProxyConfig(workspaceId, clusterRequest, stackEnv, resultBuilder);
        validateRdsConfigs(workspaceId, clusterRequest, stackEnv, resultBuilder);
        return resultBuilder.build();
    }

    private void validateLdapConfig(Long workspaceId, ClusterRequest request, EnvironmentView stackEnv,
            ValidationResult.ValidationResultBuilder resultBuilder) {
        if (request.getLdapConfig() != null) {
            validateEnvironments(request.getLdapConfig().getName(), LdapConfig.class.getSimpleName(), request.getLdapConfig().getEnvironments(), stackEnv,
                    resultBuilder);
        } else if (request.getLdapConfigName() != null) {
            validateEnvironmentAwareResource(ldapConfigService.getByNameForWorkspaceId(request.getLdapConfigName(), workspaceId), stackEnv, resultBuilder);
        } else if (request.getLdapConfigId() != null) {
            validateEnvironmentAwareResource(ldapConfigService.get(request.getLdapConfigId()), stackEnv, resultBuilder);
        }
    }

    private void validateProxyConfig(Long workspaceId, ClusterRequest request, EnvironmentView stackEnv,
            ValidationResult.ValidationResultBuilder resultBuilder) {
        if (StringUtils.isNotBlank(request.getProxyName())) {
            validateEnvironmentAwareResource(proxyConfigService.getByNameForWorkspaceId(request.getProxyName(), workspaceId), stackEnv, resultBuilder);
        }
    }

    private void validateRdsConfigs(Long workspaceId, ClusterRequest request, EnvironmentView stackEnv,
            ValidationResult.ValidationResultBuilder resultBuilder) {
        if (request.getRdsConfigIds() != null) {
            for (Long rdsConfigId : request.getRdsConfigIds()) {
                validateEnvironmentAwareResource(rdsConfigService.get(rdsConfigId), stackEnv, resultBuilder);
            }
        }
        if (request.getRdsConfigNames() != null) {
            for (String rdsConfigName : request.getRdsConfigNames()) {
                validateEnvironmentAwareResource(rdsConfigService.getByNameForWorkspaceId(rdsConfigName, workspaceId), stackEnv, resultBuilder);
            }
        }
        if (request.getRdsConfigJsons() != null) {
            for (RDSConfigJson rdsConfig : request.getRdsConfigJsons()) {
                validateEnvironments(rdsConfig.getName(), RDSConfig.class.getSimpleName(), rdsConfig.getEnvironments(), stackEnv, resultBuilder);
            }
        }
    }

    private void validateEnvironments(String resourceName, String resourceType, Set<String> environments, EnvironmentView stackEnv,
            ValidationResult.ValidationResultBuilder resultBuilder) {
        if (stackEnv == null) {
            if (!CollectionUtils.isEmpty(environments)) {
                resultBuilder.error(String.format("Stack without environment cannot use %s %s resource which attached to an environment.",
                        resourceName, resourceType));
            }
        } else {
            if (!CollectionUtils.isEmpty(environments)
                    && environments.stream().noneMatch(resEnv -> resEnv.equals(stackEnv.getName()))) {
                resultBuilder.error(String.format("Stack cannot use %s %s resource which is not attached to %s environment and not global.",
                        resourceName, resourceType, stackEnv.getName()));
            }
        }
    }

    private <T extends EnvironmentAwareResource> void validateEnvironmentAwareResource(T resource,
            EnvironmentView stackEnv, ValidationResult.ValidationResultBuilder resultBuilder) {
        if (stackEnv == null) {
            if (!CollectionUtils.isEmpty(resource.getEnvironments())) {
                resultBuilder.error(String.format("Stack without environment cannot use %s %s resource which attached to an environment.",
                        resource.getName(), resource.getClass().getSimpleName()));
            }
        } else {
            if (!CollectionUtils.isEmpty(resource.getEnvironments())
                    && resource.getEnvironments().stream().noneMatch(resEnv -> resEnv.getName().equals(stackEnv.getName()))) {
                resultBuilder.error(String.format("Stack cannot use %s %s resource which is not attached to %s environment and not global.",
                        resource.getName(), resource.getClass().getSimpleName(), stackEnv.getName()));
            }
        }
    }
}
