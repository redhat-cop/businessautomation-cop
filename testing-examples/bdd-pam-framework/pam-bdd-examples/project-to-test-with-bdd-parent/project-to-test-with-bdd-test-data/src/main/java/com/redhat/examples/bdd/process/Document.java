package com.redhat.examples.bdd.process;

import java.io.Serializable;
import java.util.Objects;

public class Document implements Serializable {

    static final long serialVersionUID = 1L;

    private String documentType;

    private String documentId;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return "Document{" +
                "documentType='" + documentType + '\'' +
                ", documentId='" + documentId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;
        Document document = (Document) o;
        return Objects.equals(getDocumentType(), document.getDocumentType()) && Objects.equals(getDocumentId(), document.getDocumentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDocumentType(), getDocumentId());
    }
}
