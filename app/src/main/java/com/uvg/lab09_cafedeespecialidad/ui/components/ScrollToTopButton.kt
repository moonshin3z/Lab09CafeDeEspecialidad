package com.uvg.lab09_cafedeespecialidad.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch

@Composable
fun ScrollToTopButton(
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    threshold: Int = 2
) {
    val isVisible by remember(gridState, threshold) {
        derivedStateOf {
            gridState.firstVisibleItemIndex >= threshold
        }
    }

    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    gridState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.semantics {
                contentDescription = "Volver arriba"
            }
        ) {
            Text("↑")
        }
    }
}