package org.redhat.services.service.api;//

import org.redhat.services.exception.KieContainerRequestException;
import org.redhat.services.model.dto.Release;

public interface ExecutorService {

    public void scanLatestKIEBase(String containerId) throws KieContainerRequestException;

    public void loadLatestKIEBase(Release release, String containerId) throws KieContainerRequestException;

}
