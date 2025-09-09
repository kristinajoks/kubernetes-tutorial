package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.glassfish.jersey.server.validation.ValidationError;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "validationErrors")
public class ValidationErrorResponse implements Serializable {

    @XmlElement(name = "validationError")
    @JsonProperty("validationErrors")
    private List<ValidationError> errors = new LinkedList<>();

    public ValidationErrorResponse() {
    }

    public ValidationErrorResponse(List<ValidationError> errors) {
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}
