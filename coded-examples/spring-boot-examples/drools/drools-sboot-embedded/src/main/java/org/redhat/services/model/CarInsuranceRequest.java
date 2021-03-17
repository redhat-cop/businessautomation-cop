package org.redhat.services.model;

import java.io.Serializable;

import com.redhat.demos.decisiontable.Driver;
import com.redhat.demos.decisiontable.Policy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CarInsuranceRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Driver driver;
    private Policy policy;
}
