package com.frontendlovers.sanmarcos.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Test Unitarios
class RunChilenoValidatorTest {

    private final RunChilenoValidator validator = new RunChilenoValidator();

    @Test
    void aceptaRunsValidos() {
        assertTrue(validator.isValid("11.111.111-1", null));
        assertTrue(validator.isValid("11111111-1", null));   // sin formato
        assertTrue(validator.isValid("22.222.222-2", null));
        assertTrue(validator.isValid("12.345.678-5", null));
        assertTrue(validator.isValid("6.000.000-K", null));  // dígito K
        assertTrue(validator.isValid("6000000-k", null));    // k minúscula
    }

    @Test
    void rechazaDigitoVerificadorIncorrecto() {
        assertFalse(validator.isValid("12345678-K", null));  // el correcto es 5
        assertFalse(validator.isValid("12.345.678-9", null));
        assertFalse(validator.isValid("11.111.111-2", null));
    }

    @Test
    void rechazaFormatosInvalidos() {
        assertFalse(validator.isValid("1234567", null));     // sin dígito
        assertFalse(validator.isValid("abcdefgh-5", null));  // letras en el cuerpo
        assertFalse(validator.isValid("123456789-0", null)); // cuerpo de 9 dígitos
    }

    @Test
    void ignoraNulosYVacios() {
        assertTrue(validator.isValid(null, null));           // los evalúa @NotBlank
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }
}
