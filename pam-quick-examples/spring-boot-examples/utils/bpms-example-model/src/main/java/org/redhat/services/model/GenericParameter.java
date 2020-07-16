package org.redhat.services.model;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

@org.kie.api.remote.Remotable
public class GenericParameter implements Serializable {
    private static final long serialVersionUID = 3343139687123410362L;
    private String paramName;
    private String paramValue;

    public String getParamName() {
        return paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
        result = prime * result + ((paramValue == null) ? 0 : paramValue.hashCode());
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
        GenericParameter other = (GenericParameter) obj;
        if (paramName == null) {
            if (other.paramName != null)
                return false;
        } else if (!paramName.equals(other.paramName))
            return false;
        if (paramValue == null) {
            if (other.paramValue != null)
                return false;
        } else if (!paramValue.equals(other.paramValue))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "GenericParameter [paramName=" + paramName + ", paramValue=" + paramValue + "]";
    }

    public static GenericParameter fromString(String jsonObject) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject, GenericParameter.class);
    }
}
