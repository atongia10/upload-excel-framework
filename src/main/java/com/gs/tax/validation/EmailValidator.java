package com.gs.tax.validation;

import java.util.regex.Pattern;

public class EmailValidator implements Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    public String validate(Object value, String rule) {
        if (value instanceof String) {
            String stringValue = (String) value;
            if (!EMAIL_PATTERN.matcher(stringValue).matches()) {
                return "Invalid email format";
            }
        } else if (value != null) { // Non-string, non-null values are invalid for email fields
            return "Invalid email format";
        }
        return "";
    }
}
