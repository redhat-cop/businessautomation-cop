package org.redhat.services.model.dto;

import lombok.*;
import org.kie.api.builder.KieScanner;
import org.kie.api.runtime.KieContainer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KJAR {

    private String groupId;
    private String artifactId;
    private String version;
    private String containerId;
    private Integer scanningInterval;
    private KieContainer container;
    private KieScanner scanner;

}
