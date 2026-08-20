package org.fdroid.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.fdroid.R
import org.fdroid.search.SavedSearch
import org.fdroid.ui.FDroidContent

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.pastSearches(
  savedSearches: List<SavedSearch>,
  onSearch: (String) -> Unit,
  onClearSavedSearches: () -> Unit,
) {
  item {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = stringResource(R.string.search_history),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp).weight(1f),
      )
      TextButton(onClick = onClearSavedSearches, modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(stringResource(R.string.clear))
      }
    }
  }
  items(savedSearches) { item ->
    ListItem(
      leadingContent = {
        Icon(
          Icons.Default.History,
          contentDescription = null,
          modifier =
            Modifier.clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceContainer)
              .padding(8.dp),
        )
      },
      onClick = { onSearch(item.query) },
      modifier = Modifier.fillMaxWidth().animateItem(),
    ) {
      Text(item.query)
    }
  }
}

@Preview
@Composable
private fun Preview() {
  FDroidContent {
    val savedSearches =
      listOf(SavedSearch(1, "foo"), SavedSearch(2, "foo bar"), SavedSearch(3, "foobar"))
    LazyColumn {
      pastSearches(savedSearches = savedSearches, onClearSavedSearches = {}, onSearch = {})
    }
  }
}
