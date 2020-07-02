package com.redhat.services.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kie.server.api.model.ReleaseId;

@Slf4j
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Kjar {

    private String groupId;
    private String artifactId;
    private String version;
    private String containerId;
    private String alias;

    public ReleaseId getReleaseId() {
        return new ReleaseId(groupId, artifactId, version);

    }
}
