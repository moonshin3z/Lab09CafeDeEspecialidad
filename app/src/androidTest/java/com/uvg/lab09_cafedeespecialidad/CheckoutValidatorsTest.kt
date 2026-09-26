package com.uvg.lab09_cafedeespecialidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutValidatorsTest {

    @Test
    fun `full name with at least three letters is valid`() {
        val result = validateFullName("Ana")

        assertNull(result)
    }

    @Test
    fun `full name accepts accents and letter enye`() {
        val result = validateFullName("Íñigo Núñez")

        assertNull(result)
    }

    @Test
    fun `full name ignores spaces and signs when counting letters`() {
        val result = validateFullName("  A-B C  ")

        assertNull(result)
    }

    @Test
    fun `full name with fewer than three letters is invalid`() {
        val result = validateFullName("A B")

        assertEquals(
            "Ingrese un nombre con al menos 3 letras.",
            result
        )
    }

    @Test
    fun `full name containing digits is invalid`() {
        val result = validateFullName("Ana 2")

        assertEquals(
            "El nombre no puede contener números.",
            result
        )
    }

    @Test
    fun `empty full name is invalid`() {
        val result = validateFullName("   ")

        assertEquals(
            "Ingrese un nombre con al menos 3 letras.",
            result
        )
    }

    @Test
    fun `phone with exactly eight digits is valid`() {
        val result = validatePhone("55123456")

        assertNull(result)
    }

    @Test
    fun `phone applies trim before validation`() {
        val result = validatePhone("  55123456  ")

        assertNull(result)
    }

    @Test
    fun `phone with fewer than eight digits is invalid`() {
        val result = validatePhone("5512345")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun `phone with more than eight digits is invalid`() {
        val result = validatePhone("551234567")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun `phone with internal spaces is invalid`() {
        val result = validatePhone("5512 3456")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun `phone with hyphen is invalid`() {
        val result = validatePhone("5512-3456")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun `phone with country prefix is invalid`() {
        val result = validatePhone("+50255123456")

        assertEquals(
            "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones.",
            result
        )
    }

    @Test
    fun `nit with exactly five digits is valid`() {
        val result = validateNit("12345")

        assertNull(result)
    }

    @Test
    fun `nit with more than five digits is valid`() {
        val result = validateNit("123456789")

        assertNull(result)
    }

    @Test
    fun `nit applies trim before validation`() {
        val result = validateNit("  12345  ")

        assertNull(result)
    }

    @Test
    fun `nit with fewer than five digits is invalid`() {
        val result = validateNit("1234")

        assertEquals(
            "Ingrese al menos 5 dígitos.",
            result
        )
    }

    @Test
    fun `nit containing letters is invalid`() {
        val result = validateNit("1234A")

        assertEquals(
            "Ingrese al menos 5 dígitos.",
            result
        )
    }

    @Test
    fun `nit containing hyphen is invalid`() {
        val result = validateNit("1234-5")

        assertEquals(
            "Ingrese al menos 5 dígitos.",
            result
        )
    }

    @Test
    fun `business name with three characters is valid`() {
        val result = validateBusinessName("ABC")

        assertNull(result)
    }

    @Test
    fun `business name with normal company name is valid`() {
        val result = validateBusinessName("Guzmán Inversiones S.A.")

        assertNull(result)
    }

    @Test
    fun `business name applies trim before counting characters`() {
        val result = validateBusinessName("  ABC  ")

        assertNull(result)
    }

    @Test
    fun `business name with fewer than three trimmed characters is invalid`() {
        val result = validateBusinessName("  AB  ")

        assertEquals(
            "Ingrese una razón social de al menos 3 caracteres.",
            result
        )
    }

    @Test
    fun `empty business name is invalid`() {
        val result = validateBusinessName("   ")

        assertEquals(
            "Ingrese una razón social de al menos 3 caracteres.",
            result
        )
    }
}