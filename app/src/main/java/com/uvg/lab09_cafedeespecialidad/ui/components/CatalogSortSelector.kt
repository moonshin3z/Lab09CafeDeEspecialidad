package com.uvg.lab09_cafedeespecialidad.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.lab09_cafedeespecialidad.model.CatalogSortOrder

@Composable
fun CatalogSortSelector(
    sortOrder: CatalogSortOrder,
    onSortOrderChange: (CatalogSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ordenar por:",
            style = MaterialTheme.typography.bodyMedium
        )

        FilterChip(
            selected = sortOrder == CatalogSortOrder.NAME,
            onClick = {
                onSortOrderChange(CatalogSortOrder.NAME)
            },
            label = {
                Text("Nombre")
            }
        )

        FilterChip(
            selected = sortOrder == CatalogSortOrder.PRICE,
            onClick = {
                onSortOrderChange(CatalogSortOrder.PRICE)
            },
            label = {
                Text("Precio")
            }
        )
    }
}
