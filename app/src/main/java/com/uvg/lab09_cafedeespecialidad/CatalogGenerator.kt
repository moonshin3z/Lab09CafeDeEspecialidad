package com.uvg.lab09_cafedeespecialidad

import com.uvg.lab09_cafedeespecialidad.model.Product
import kotlin.random.Random

private const val CATALOG_SIZE = 500

fun generateCatalog(
    originals: List<Product>,
    seed: Int
): List<Product> {
    require(originals.isNotEmpty()) {
        "Se necesita al menos un producto original."
    }
    require(originals.size <= CATALOG_SIZE) {
        "El catálogo original no puede superar los $CATALOG_SIZE productos."
    }
    require(originals.map(Product::id).distinct().size == originals.size) {
        "Los productos originales deben tener IDs únicos."
    }

    val random = Random(seed)
    val origins = listOf(
        "Huehuetenango",
        "Antigua",
        "Cobán",
        "Atitlán",
        "Fraijanes"
    )
    val varieties = listOf(
        "Bourbon",
        "Caturra",
        "Geisha",
        "Pacamara",
        "Typica"
    )
    val processes = listOf(
        "Lavado",
        "Natural",
        "Honey"
    )
    val roasts = listOf(
        "claro",
        "medio",
        "oscuro"
    )
    val profileIds = originals.map(Product::profileId).distinct()

    val generatedProducts = (originals.size until CATALOG_SIZE).map { index ->
        val generatedNumber = index - originals.size + 1
        val id = "generated-coffee-$generatedNumber"
        val origin = origins.random(random)
        val variety = varieties.random(random)
        val process = processes.random(random)
        val roast = roasts.random(random)
        val stock = when (generatedNumber % 12) {
            0 -> 0
            1 -> 3
            else -> random.nextInt(from = 1, until = 13)
        }

        Product(
            id = id,
            name = "$variety de $origin $generatedNumber",
            description = buildString {
                append("Café de $origin con tueste $roast y proceso ")
                append(process.lowercase())
                append(".")
            },
            price = random.nextInt(
                from = 5500,
                until = 18001
            ) / 100.0,
            stock = stock,
            imageUrl = "https://picsum.photos/seed/$id/400/400",
            profileId = profileIds.random(random),
            technicalSheet = buildString {
                append("Origen: $origin · ")
                append("Variedad: $variety · ")
                append("Proceso: $process · ")
                append("Tueste: ${roast.replaceFirstChar(Char::uppercase)}.")
            }
        )
    }

    return originals + generatedProducts
}