package com.plusmobileapps.rickandmorty.androidapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T : Any> SearchFilterDropdown(
    selected: String?,
    modifier: Modifier = Modifier,
    contentDescription: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    onClearClicked: () -> Unit,
) {
    var showDropdown by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .padding(8.dp)
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { showDropdown = !showDropdown }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(selected ?: "None", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier
                .weight(1f)
                .padding(8.dp))
            IconButton(onClick = onClearClicked) {
                Icon(Icons.Default.Clear, contentDescription = contentDescription)
            }
        }

        DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
            items.forEach {
                DropdownMenuItem(text = { Text(itemLabel(it)) }, onClick = {
                    showDropdown = false
                    onItemSelected(it)
                })
            }
        }

    }
}