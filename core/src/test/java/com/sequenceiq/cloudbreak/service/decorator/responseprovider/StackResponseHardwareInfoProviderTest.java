package com.sequenceiq.cloudbreak.service.decorator.responseprovider;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;

import com.google.common.collect.Sets;
import com.sequenceiq.cloudbreak.TestUtil;
import com.sequenceiq.cloudbreak.api.model.stack.StackResponse;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupType;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.cluster.host.HostMetadata;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.repository.HostMetadataRepository;
import com.sequenceiq.cloudbreak.service.hostgroup.HostGroupService;

public class StackResponseHardwareInfoProviderTest {

    @InjectMocks
    private StackResponseHardwareInfoProvider underTest;

    @Mock
    private HostMetadataRepository hostMetadataRepository;

    @Mock
    private HostGroupService hostGroupService;

    @Mock
    private ConversionService conversionService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProviderEntriesToStackResponseEmptyInstanceGroup() {

        Stack stack = new Stack();
        stack.setInstanceGroups(emptySet());

        StackResponse actual = underTest.providerEntriesToStackResponse(stack, new StackResponse());

        Assert.assertEquals(0L, actual.getHardwareInfoGroups().size());
    }

    @Test
    public void testProviderEntriesToStackResponseWhenOneInstanceMetadataPresented() {
        when(hostGroupService.getByClusterIdAndName(anyLong(), anyString())).thenReturn(TestUtil.hostGroup());

        Stack stack = TestUtil.stack();
        stack.setInstanceGroups(Sets.newHashSet(TestUtil.instanceGroup(1L, InstanceGroupType.GATEWAY, TestUtil.gcpTemplate(1L))));

        StackResponse actual = underTest.providerEntriesToStackResponse(stack, new StackResponse());

        Assert.assertEquals(1L, actual.getHardwareInfoGroups().size());
    }

    @Test
    public void testProviderEntriesToStackResponseClusterNull() {

        Stack stack = new Stack();
        InstanceMetaData instanceMetaData = new InstanceMetaData();
        stack.setInstanceGroups(getInstanceGroups(instanceMetaData));

        StackResponse actual = underTest.providerEntriesToStackResponse(stack, new StackResponse());

        Assert.assertEquals(1L, actual.getHardwareInfoGroups().size());

        Mockito.verify(hostMetadataRepository, Mockito.times(0)).findHostInClusterByName(anyLong(), anyString());
    }

    @Test
    public void testProviderEntriesToStackResponseClusterNotNullButFQDNNull() {
        when(hostMetadataRepository.findHostInClusterByName(anyLong(), anyString())).thenReturn(null);

        Stack stack = new Stack();
        InstanceMetaData instanceMetaData = new InstanceMetaData();
        stack.setInstanceGroups(getInstanceGroups(instanceMetaData));

        StackResponse actual = underTest.providerEntriesToStackResponse(stack, new StackResponse());

        Assert.assertEquals(1L, actual.getHardwareInfoGroups().size());

        Mockito.verify(hostMetadataRepository, Mockito.times(0)).findHostInClusterByName(anyLong(), anyString());
    }

    @Test
    public void testProviderEntriesToStackResponseClusterNotNullAndFQDNNotNull() {

        Stack stack = new Stack();
        Cluster cluster = new Cluster();
        cluster.setId(1L);
        stack.setCluster(cluster);
        InstanceMetaData instanceMetaData = new InstanceMetaData();
        instanceMetaData.setId(1L);
        instanceMetaData.setDiscoveryFQDN("fqdn");
        stack.setInstanceGroups(getInstanceGroups(instanceMetaData));

        StackResponse stackResponse = new StackResponse();
        StackResponse actual = underTest.providerEntriesToStackResponse(stack, stackResponse);

        Assert.assertEquals(1L, actual.getHardwareInfoGroups().size());

        Mockito.verify(hostMetadataRepository, Mockito.times(1)).findHostInClusterByName(1L, "fqdn");
    }

    @Test
    public void testProviderEntriesToStackResponseMultipleInstanceGroup() {

        Stack stack = new Stack();
        stack.setCluster(new Cluster());
        Set<InstanceGroup> instanceGroups = getInstanceGroups(new InstanceMetaData());
        instanceGroups.addAll(getInstanceGroups(new InstanceMetaData()));
        stack.setInstanceGroups(instanceGroups);

        StackResponse actual = underTest.providerEntriesToStackResponse(stack, new StackResponse());

        Assert.assertEquals(2L, actual.getHardwareInfoGroups().size());

        Mockito.verify(hostMetadataRepository, Mockito.times(0)).findHostInClusterByName(anyLong(), anyString());
    }

    @Test
    public void testProviderEntriesToStackResponseConvertsResult() {

        Stack stack = new Stack();
        Cluster cluster = new Cluster();
        cluster.setId(1L);
        stack.setCluster(cluster);
        InstanceMetaData instanceMetaData = new InstanceMetaData();
        instanceMetaData.setId(1L);
        instanceMetaData.setDiscoveryFQDN("fqdn");
        stack.setInstanceGroups(getInstanceGroups(instanceMetaData));

        HostMetadata hostMetadata = new HostMetadata();
        hostMetadata.setHostGroup(TestUtil.hostGroup());

        when(hostMetadataRepository.findHostInClusterByName(1L, "fqdn")).thenReturn(hostMetadata);

        StackResponse actual = underTest.providerEntriesToStackResponse(stack, new StackResponse());

        Assert.assertEquals(1L, actual.getHardwareInfoGroups().size());

        Mockito.verify(hostMetadataRepository, Mockito.times(1)).findHostInClusterByName(1L, "fqdn");

    }

    private Set<InstanceGroup> getInstanceGroups(InstanceMetaData... instanceMetaData) {
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setGroupName("master");
        for (InstanceMetaData instanceMetaDatum : instanceMetaData) {
            instanceMetaDatum.setInstanceGroup(instanceGroup);
        }
        instanceGroup.setInstanceMetaData(Sets.newHashSet(instanceMetaData));
        return Sets.newHashSet(instanceGroup);
    }
}
