package com.redhat.cop.pam.example3;

import org.kie.api.remote.Remotable;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Remotable
@XmlType(name="CanOpenAccountResults")
public enum CanOpenAccountResults implements Serializable {
    ALLOW, //
    NOT_ALLOW;
}
