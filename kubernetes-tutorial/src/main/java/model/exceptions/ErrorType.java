package model.exceptions;

import jakarta.xml.bind.annotation.XmlEnum;

@XmlEnum(String.class)
public enum ErrorType {
    INVALID_PARAMETER,
    NOT_FOUND,
    INSUFFICIENT_STOCK,
    INTERNAL_ERROR,
    BAD_REQUEST,
}
