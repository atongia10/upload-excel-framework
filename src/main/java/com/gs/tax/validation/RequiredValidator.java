package com.gs.tax.validation;

public class RequiredValidator implements Validator {

    @Override
    public String validate(Object value, String rule) {
        if (value == null) {
            return "Field is required";
        } else if (value instanceof String && ((String) value).trim().isEmpty()) {
            return "Field is required";
        }
        // No need to check for non-String types; existence is sufficient
        return "";
    }
}
