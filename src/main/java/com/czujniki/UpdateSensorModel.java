package com.czujniki;

public class UpdateSensorModel {
    private String operation;
    private String value;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UpdateSensorModel(String operation, String value) {
        this.operation = operation;
        this.value = value;
    }
}
