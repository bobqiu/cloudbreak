package com.sequenceiq.cloudbreak.reactor.api.event.orchestration;

import com.sequenceiq.cloudbreak.reactor.api.event.resource.AbstractClusterScaleRequest;

public class UpscaleAmbariRequest extends AbstractClusterScaleRequest {

    private final Integer scalingAdjustment;

    private final boolean primaryGatewayChanged;

    public UpscaleAmbariRequest(Long stackId, String hostGroupName, Integer scalingAdjustment, boolean primaryGatewayChanged) {
        super(stackId, hostGroupName);
        this.scalingAdjustment = scalingAdjustment;
        this.primaryGatewayChanged = primaryGatewayChanged;
    }

    public Integer getScalingAdjustment() {
        return scalingAdjustment;
    }

    public boolean isPrimaryGatewayChanged() {
        return primaryGatewayChanged;
    }
}
