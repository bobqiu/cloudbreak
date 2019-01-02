package com.sequenceiq.it.cloudbreak.newway.action;

import static com.sequenceiq.it.cloudbreak.newway.log.Log.logJSON;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.api.model.template.ClusterTemplateResponse;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.ClusterTemplateEntity;

public class ClusterTemplateListAction implements ActionV2<ClusterTemplateEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterTemplateListAction.class);

    @Override
    public ClusterTemplateEntity action(TestContext testContext, ClusterTemplateEntity entity, CloudbreakClient client) throws Exception {
        logJSON(LOGGER, " ClusterTemplateEntity get request:\n", entity.getRequest());
        Set<ClusterTemplateResponse> responses = client.getCloudbreakClient()
                .clusterTemplateV3EndPoint()
                .listByWorkspace(client.getWorkspaceId())
                .stream()
                .map(view -> {
                    ClusterTemplateResponse response = new ClusterTemplateResponse();
                    response.setStatus(view.getStatus());
                    response.setDatalakeRequired(view.getDatalakeRequired());
                    response.setId(view.getId());
                    response.setCloudPlatform(view.getCloudPlatform());
                    response.setDescription(view.getDescription());
                    response.setName(view.getName());
                    response.setType(view.getType());
                    return response;
                })
                .collect(Collectors.toSet());
        entity.setResponses(responses);
        logJSON(LOGGER, " ClusterTemplateEntity list successfully:\n", responses);
        return entity;
    }
}
