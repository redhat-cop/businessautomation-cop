package org.redhat.services.model;

import java.io.Serializable;

@javax.xml.bind.annotation.XmlRootElement
public class AbortProcessWrapper implements Serializable {
    private static final long serialVersionUID = 775987707854977334L;

    private Boolean needSupervisorApproval;
    private Boolean needNotification;
    private String containerId;

    public Boolean getNeedSupervisorApproval() {
        return needSupervisorApproval;
    }

    public void setNeedSupervisorApproval(Boolean needSupervisorApproval) {
        this.needSupervisorApproval = needSupervisorApproval;
    }

    public Boolean getNeedNotification() {
        return needNotification;
    }

    public void setNeedNotification(Boolean needNotification) {
        this.needNotification = needNotification;
    }


    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((containerId == null) ? 0 : containerId.hashCode());
        result = prime * result + ((needNotification == null) ? 0 : needNotification.hashCode());
        result = prime * result + ((needSupervisorApproval == null) ? 0 : needSupervisorApproval.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbortProcessWrapper other = (AbortProcessWrapper) obj;
        if (containerId == null) {
            if (other.containerId != null)
                return false;
        } else if (!containerId.equals(other.containerId))
            return false;
        if (needNotification == null) {
            if (other.needNotification != null)
                return false;
        } else if (!needNotification.equals(other.needNotification))
            return false;
        if (needSupervisorApproval == null) {
            if (other.needSupervisorApproval != null)
                return false;
        } else if (!needSupervisorApproval.equals(other.needSupervisorApproval))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AbortProcessWrapper [needSupervisorApproval=" + needSupervisorApproval + ", needNotification="
                + needNotification + ", containerId=" + containerId + "]";
    }


}
