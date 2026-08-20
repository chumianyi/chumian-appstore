package org.fdroid.ui.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import org.fdroid.R
import org.fdroid.ui.FDroidContent

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun CategoryList(
  categoryMap: Map<CategoryGroup, List<CategoryItem>>?,
  onNav: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(!categoryMap.isNullOrEmpty()) {
    Column(modifier = modifier) {
      Text(
        text = stringResource(R.string.main_menu__categories),
        style = MaterialTheme.typography.titleMediumEmphasized,
        fontSize = 20.sp,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      // we'll sort the groups here, because before we didn't have the context to get names
      val res = LocalResources.current
      val sortedMap =
        remember(categoryMap) {
          val comparator = compareBy<CategoryGroup> { res.getString(it.name) }
          categoryMap?.toSortedMap(comparator)
        }
      OutlinedCard(modifier = Modifier.padding(16.dp)) {
        // FIXME: A LazyColumn would be better, but we can't use this inside a scrollable column
        Column {
          sortedMap?.forEach { (group, categories) ->
            CategoryGroupRow(group, categories, onNav)
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
          }
        }
      }
    }
  }
}

@Composable
@PreviewLightDark
fun CategoryListPreview() {
  FDroidContent {
    val categories =
      mapOf(
        CategoryGroups.productivity to
          listOf(
            CategoryItem("App Store & Updater", "App Store & Updater"),
            CategoryItem("Browser", "Browser"),
            CategoryItem("Calendar & Agenda", "Calendar & Agenda"),
          ),
        CategoryGroups.media to
          listOf(
            CategoryItem("Cloud Storage & File Sync", "Cloud Storage & File Sync"),
            CategoryItem("Connectivity", "Connectivity"),
            CategoryItem("Development", "Development"),
            CategoryItem("doesn't exist", "Foo bar"),
          ),
      )
    CategoryList(categories, {})
  }
}
