package cl.duoc.pichangapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fila horizontal scrolleable reutilizable para secciones tipo "Eventos cerca de ti".
 * Genérica sobre el tipo de dato; cada item se dibuja con [itemContent].
 */
@Composable
fun <T> HorizontalCardRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    itemSpacing: Dp = 12.dp,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        items(items = items, key = key) { item ->
            itemContent(item)
        }
    }
}
