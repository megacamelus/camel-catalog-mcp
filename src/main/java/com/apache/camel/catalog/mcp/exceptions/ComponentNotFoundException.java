package com.apache.camel.catalog.mcp.exceptions;

public class ComponentNotFoundException extends RuntimeException {
    public ComponentNotFoundException(String message) {
        super(message);
    }
}
