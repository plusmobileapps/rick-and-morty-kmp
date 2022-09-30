package com.plusmobileapps.rickandmorty.androidapp.util

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

val LazyListState.isLastItemVisible: Boolean
    get() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

val LazyGridState.isLastItemVisible: Boolean
    get() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

val LazyListState.isFirstItemVisible: Boolean
    get() = firstVisibleItemIndex == 0

val LazyGridState.isFirstItemVisible: Boolean
    get() = firstVisibleItemIndex == 0

data class ScrollContext(
    val isTop: Boolean,
    val isBottom: Boolean,
)

@Composable
fun rememberScrollContext(listState: LazyListState): ScrollContext {
    val scrollContext by remember {
        derivedStateOf {
            ScrollContext(
                isTop = listState.isFirstItemVisible,
                isBottom = listState.isLastItemVisible
            )
        }
    }
    return scrollContext
}

@Composable
fun rememberScrollContext(listState: LazyGridState): ScrollContext {
    val scrollContext by remember {
        derivedStateOf {
            ScrollContext(
                isTop = listState.isFirstItemVisible,
                isBottom = listState.isLastItemVisible
            )
        }
    }
    return scrollContext
}


@Composable
fun InfiniteLoadingList(
    modifier: Modifier,
    listState: LazyListState,
    items: List<Any>,
    loadMore: () -> Unit,
    rowContent:  @Composable (Int, Any) -> Unit
) {
    val firstVisibleIndex = remember { mutableStateOf(listState.firstVisibleItemIndex) }
    LazyColumn(state = listState, modifier = modifier) {
        itemsIndexed(items) { index, item ->
            rowContent(index, item)
        }
    }
    if (listState.shouldLoadMore(firstVisibleIndex)) {
        loadMore()
    }
}

fun LazyListState.shouldLoadMore(rememberedIndex: MutableState<Int>): Boolean {
    val firstVisibleIndex = this.firstVisibleItemIndex
    if (rememberedIndex.value != firstVisibleIndex) {
        rememberedIndex.value = firstVisibleIndex
        return layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
    }
    return false
}