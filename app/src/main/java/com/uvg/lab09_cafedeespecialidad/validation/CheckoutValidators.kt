package com.uvg.lab09_cafedeespecialidad.validation

private const val MINIMUM_NAME_LETTERS = 3
private const val REQUIRED_PHONE_DIGITS = 8
private const val MINIMUM_NIT_DIGITS = 5
private const val MINIMUM_BUSINESS_NAME_CHARACTERS = 3

fun validateFullName(value: String): String? {
    val trimmedValue = value.trim()

    if (trimmedValue.any { character -> character.isDigit() }) {
        return "El nombre no puede contener números."
    }

    val letterCount = trimmedValue.count { character ->
        character.isLetter()
    }

    if (letterCount < MINIMUM_NAME_LETTERS) {
        return "Ingrese un nombre con al menos 3 letras."
    }

    return null
}

fun validatePhone(value: String): String? {
    val trimmedValue = value.trim()

    if (!trimmedValue.matches(Regex("\\d{$REQUIRED_PHONE_DIGITS}"))) {
        return "Ingrese exactamente 8 dígitos, sin prefijo, espacios ni guiones."
    }

    return null
}

fun validateNit(value: String): String? {
    val trimmedValue = value.trim()

    if (!trimmedValue.matches(Regex("\\d{$MINIMUM_NIT_DIGITS,}"))) {
        return "Ingrese al menos 5 dígitos."
    }

    return null
}

fun validateBusinessName(value: String): String? {
    val trimmedValue = value.trim()

    if (trimmedValue.length < MINIMUM_BUSINESS_NAME_CHARACTERS) {
        return "Ingrese una razón social de al menos 3 caracteres."
    }

    return null
}