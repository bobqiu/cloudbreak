package com.sequenceiq.cloudbreak.service.rdsconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.rds.RdsType;
import com.sequenceiq.cloudbreak.domain.Blueprint;

@Component
public class AmbariRdsConfigProvider extends AbstractRdsConfigProvider {

    private static final String PILLAR_KEY = "ambari";

    @Value("${cb.ambari.database.port:5432}")
    private String port;

    @Value("${cb.ambari.database.user:ambari}")
    private String userName;

    @Value("${cb.ambari.database.db:ambari}")
    private String db;

    @Override
    protected String getDbUser() {
        return userName;
    }

    @Override
    protected String getDb() {
        return db;
    }

    @Override
    protected String getDbPort() {
        return port;
    }

    @Override
    protected String getPillarKey() {
        return PILLAR_KEY;
    }

    @Override
    protected RdsType getRdsType() {
        return RdsType.AMBARI;
    }

    @Override
    protected boolean isRdsConfigNeeded(Blueprint blueprint) {
        return true;
    }
}
