package com.frontendlovers.sanmarcos.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RunChilenoValidator implements ConstraintValidator<RunChileno, String> {

    @Override
    public boolean isValid(String run, ConstraintValidatorContext context) {
        if (run == null || run.isBlank()) {
            return true; // @NotBlank se encarga de los vacíos
        }

        String clean = run.replace(".", "").replace("-", "").toUpperCase();
        if (!clean.matches("\\d{7,8}[0-9K]")) {
            return false;
        }

        String body = clean.substring(0, clean.length() - 1);
        char dv = clean.charAt(clean.length() - 1);

        int sum = 0;
        int multiplier = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }

        int expected = 11 - (sum % 11);
        char expectedDv = expected == 11 ? '0' : expected == 10 ? 'K' : (char) ('0' + expected);
        return dv == expectedDv;
    }
}