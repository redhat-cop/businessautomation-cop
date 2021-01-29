package org.redhat.services.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.redhat.services.config.KJARRepositoryConfig;
import org.redhat.services.exception.KieContainerRequestException;
import org.redhat.services.model.dto.Release;
import org.redhat.services.rules.api.RuleExecutor;
import org.redhat.services.service.api.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExecutorServiceImpl implements ExecutorService {

    @Autowired
    private RuleExecutor ruleExecutor;

    @Autowired
    KJARRepositoryConfig kjarRepository;

    @Override
    public void loadLatestKIEBase(Release release, String containerId) throws KieContainerRequestException {
        log.info("Loading KIE Base with GAV: groupId={}, artifactId={}, version={} with containerId={}",
                release.getGroupId(), release.getArtifactId(), release.getVersion(), containerId);

        // Load Latest KIE Base and Execute
        kjarRepository.loadLatestKieBase(release, containerId);
    }

    @Override
    public void scanLatestKIEBase(String containerId) throws KieContainerRequestException {
        log.info("Scanning for latest KIE Base for ContainerId={}", containerId);
        kjarRepository.getKieScanner(containerId).scanNow();
    }


}
