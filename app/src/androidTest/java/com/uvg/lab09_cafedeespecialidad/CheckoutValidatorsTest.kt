package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.validation.validateBusinessName
import com.uvg.lab09_cafedeespecialidad.validation.validateFullName
import com.uvg.lab09_cafedeespecialidad.validation.validateNit
import com.uvg.lab09_cafedeespecialidad.validation.validatePhone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutValidatorsTest {

    @Test
    fun fullNameWithAtLeastThreeLettersIsValid() {
        val result = validateFullName("Ana")

        assertNull(result)
    }

    @Test
    fun fullNameAcceptsAccentsAndLetterEnye() {
        val result = validateFullName("Íñigo Núñez")

        assertNull(result)
    }

    @Test
    fun fullNameIgnoresSpacesAndSignsWhenCountingLetters() {
        val result = validateFullName("  A-B C  ")

        assertNull(result)
    }

    @Test
    fun fullNameWithFewerThanThreeLettersIsInvalid() {
        val result = validateFullName("A B")

        assertEquals(
            "Ingrese un nombre con al menos 3 letras.",
            result
        )
    }

    @Test
    fun fullNameContainingDigitsIsInvalid() {
        val result = validateFullName("Ana 2")

        assertEquals(
            "El nombre no puede contener números.",
            result
        )
    }

    @Test
    fun emptyFullNameIsInvalid() {
        val result = validateFullName("   ")

        assertEquals(
            "Ingrese un nombre con al menos 3 letras.",
            result
        )
    }

    @Test
    fun phoneWithExactlyEightDigitsIsValid() {
        val result = validatePhone("55123456")

        assertNull(result)
    }

    @Test
    fun phoneAppliesTrimBeforeValidation() {
        val result = validatePhone("  55123456  ")

        assertNull(result)
    }

    @Test
    fun phoneWithFewerThanEightDigitsIsInvalid() {
        val result = validatePhone("5512345")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun phoneWithMoreThanEightDigitsIsInvalid() {
        val result = validatePhone("551234567")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun phoneWithInternalSpacesIsInvalid() {
        val result = validatePhone("5512 3456")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun phoneWithHyphenIsInvalid() {
        val result = validatePhone("5512-3456")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun phoneWithCountryPrefixIsInvalid() {
        val result = validatePhone("+50255123456")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun nitWithExactlyFiveDigitsIsValid() {
        val result = validateNit("12345")

        assertNull(result)
    }

    @Test
    fun nitWithMoreThanFiveDigitsIsValid() {
        val result = validateNit("123456789")

        assertNull(result)
    }

    @Test
    fun nitAppliesTrimBeforeValidation() {
        val result = validateNit("  12345  ")

        assertNull(result)
    }

    @Test
    fun nitWithFewerThanFiveDigitsIsInvalid() {
        val result = validateNit("1234")

        assertEquals(
            "Ingrese al menos 5 dígitos.",
            result
        )
    }

    @Test
    fun nitContainingLettersIsInvalid() {
        val result = validateNit("1234A")

        assertEquals(
            "Ingrese al menos 5 dígitos.",
            result
        )
    }

    @Test
    fun nitContainingHyphenIsInvalid() {
        val result = validateNit("1234-5")

        assertEquals(
            "Ingrese al menos 5 dígitos.",
            result
        )
    }

    @Test
    fun businessNameWithThreeCharactersIsValid() {
        val result = validateBusinessName("ABC")

        assertNull(result)
    }

    @Test
    fun businessNameWithNormalCompanyNameIsValid() {
        val result = validateBusinessName("Guzmán Inversiones S.A.")

        assertNull(result)
    }

    @Test
    fun businessNameAppliesTrimBeforeCountingCharacters() {
        val result = validateBusinessName("  ABC  ")

        assertNull(result)
    }

    @Test
    fun businessNameWithFewerThanThreeTrimmedCharactersIsInvalid() {
        val result = validateBusinessName("  AB  ")

        assertEquals(
            "Ingrese una razón social de al menos 3 caracteres.",
            result
        )
    }

    @Test
    fun emptyBusinessNameIsInvalid() {
        val result = validateBusinessName("   ")

        assertEquals(
            "Ingrese una razón social de al menos 3 caracteres.",
            result
        )
    }
}
