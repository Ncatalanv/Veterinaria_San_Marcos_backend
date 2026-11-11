package com.frontendlovers.sanmarcos.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = RunChilenoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface RunChileno {
    String message() default "RUN inválido; verifica el dígito verificador (ej: 12.345.678-5)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}