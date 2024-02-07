package com.gs.tax.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ValidatorFactory {
    private static final Map<String, Validator> validators = new HashMap<>();

    static {
        // Initialize and register validators with their respective keys
        validators.put("email", new EmailValidator());
        validators.put("required", new RequiredValidator());
        // Add other validators as needed
    }

    public static Validator getValidator(String rule) {
        // Return the requested validator, or a default no-op validator if the rule is unknown
        return validators.getOrDefault(rule, new Validator() {
            @Override
            public String validate(Object value, String rule) {
                // No-op validator always returns an empty string, indicating no validation error
                return "";
            }
        });
    }
}
