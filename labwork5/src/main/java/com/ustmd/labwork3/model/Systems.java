package com.ustmd.labwork3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Systems {
    ERP("Enterprise Resource Planning"),
    CRM("Customer Relationship Management"),
    WMS("Warehouse Management System"),
    @JsonProperty("Service 1")
    SERVICE_1("Service 1");

    private final String description;

    Systems(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
