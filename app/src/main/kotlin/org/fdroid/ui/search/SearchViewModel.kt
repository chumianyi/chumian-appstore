package org.fdroid.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import org.fdroid.search.SearchHelper.isSearchable
import org.fdroid.search.SearchManager
import org.fdroid.settings.SettingsManager

@HiltViewModel
class SearchViewModel
@Inject
constructor(
  app: Application,
  private val searchManager: SearchManager,
  private val settingsManager: SettingsManager,
) : AndroidViewModel(app), SearchActions {

  val searchResults = searchManager.searchResults
  val savedSearchesFlow = searchManager.savedSearches
  val categories = searchManager.categories
  val autoShowKeyboard = settingsManager.showSearchKeyboardFlow

  override suspend fun onSearch(term: String) {
    if (term.isSearchable()) searchManager.search(term)
  }

  override fun onSearchCleared() = searchManager.onSearchCleared()

  override fun onClearSearchHistory() {
    viewModelScope.launch { searchManager.onClearSearchHistory() }
  }

  override fun setAutoShowKeyboard(autoShow: Boolean) {
    settingsManager.setShowSearchKeyboard(autoShow)
  }
}
