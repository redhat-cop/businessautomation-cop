package org.redhat.services.model.dto;

import java.io.Serializable;

public class Release implements Serializable {

    private static final long serialVersionUID = -891308818969822484L;
    private String groupId;
    private String artifactId;
    private String version;

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setGroupId( String groupId ) {
        this.groupId = groupId;
    }

    public void setArtifactId( String artifactId ) {
        this.artifactId = artifactId;
    }

    public void setVersion( String version ) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Release [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version
                + "]";
    }

}
