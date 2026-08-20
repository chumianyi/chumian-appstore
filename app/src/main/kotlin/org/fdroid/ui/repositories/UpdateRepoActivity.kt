package org.fdroid.ui.repositories

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import org.fdroid.repo.RepoUpdateWorker

private val TAG = UpdateRepoActivity::class.java.simpleName

/**
 * This activity allows OS components to trigger a repository update. One known use-case for this is
 * to do an initial repository update during SetupWizard, so app data is available when needed, e.g.
 * for restoring app backups.
 */
class UpdateRepoActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (intent.action != "org.fdroid.action.UPDATE_REPOS") {
      Log.w(TAG, "Unknown action: ${intent.action}")
      return
    }
    Log.i(TAG, "Intent received, updating repos...")
    RepoUpdateWorker.updateNow(this)
    finish()
  }
}
