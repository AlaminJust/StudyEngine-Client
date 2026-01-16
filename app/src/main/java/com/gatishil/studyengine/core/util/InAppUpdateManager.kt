package com.gatishil.studyengine.core.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages in-app updates from Google Play Store.
 * Supports both immediate (forced) and flexible updates.
 */
@Singleton
class InAppUpdateManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private var installStateUpdatedListener: InstallStateUpdatedListener? = null

    sealed class UpdateState {
        data object Idle : UpdateState()
        data object Checking : UpdateState()
        data class UpdateAvailable(val isImmediate: Boolean) : UpdateState()
        data object Downloading : UpdateState()
        data object Downloaded : UpdateState()
        data object Installing : UpdateState()
        data class Error(val message: String) : UpdateState()
        data object NoUpdateAvailable : UpdateState()
    }

    /**
     * Check for available updates.
     * @param forceImmediate If true, will prefer immediate (forced) update if available.
     */
    fun checkForUpdates(forceImmediate: Boolean = true) {
        _updateState.value = UpdateState.Checking

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                    val isImmediateAllowed = appUpdateInfo.isImmediateUpdateAllowed
                    val isFlexibleAllowed = appUpdateInfo.isFlexibleUpdateAllowed

                    // Check if update has high priority (priority >= 4 means forced)
                    val updatePriority = appUpdateInfo.updatePriority()
                    val shouldForceUpdate = forceImmediate || updatePriority >= 4

                    if (shouldForceUpdate && isImmediateAllowed) {
                        _updateState.value = UpdateState.UpdateAvailable(isImmediate = true)
                    } else if (isFlexibleAllowed) {
                        _updateState.value = UpdateState.UpdateAvailable(isImmediate = false)
                    } else {
                        _updateState.value = UpdateState.NoUpdateAvailable
                    }
                }
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    // Resume the update if it was already started
                    _updateState.value = UpdateState.UpdateAvailable(isImmediate = true)
                }
                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                    // Update already downloaded, just need to install
                    _updateState.value = UpdateState.Downloaded
                }
                else -> {
                    _updateState.value = UpdateState.NoUpdateAvailable
                }
            }
        }.addOnFailureListener { exception ->
            _updateState.value = UpdateState.Error(exception.message ?: "Failed to check for updates")
        }
    }

    /**
     * Start the update process.
     * @param activity The activity to use for the update flow.
     * @param requestCode The request code for the update flow result.
     */
    fun startUpdate(activity: Activity, requestCode: Int = UPDATE_REQUEST_CODE) {
        val currentState = _updateState.value
        if (currentState !is UpdateState.UpdateAvailable) return

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val updateType = if (currentState.isImmediate) {
                AppUpdateType.IMMEDIATE
            } else {
                AppUpdateType.FLEXIBLE
            }

            // Register listener for flexible updates
            if (updateType == AppUpdateType.FLEXIBLE) {
                registerInstallStateListener()
            }

            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                AppUpdateOptions.newBuilder(updateType).build(),
                requestCode
            )

            _updateState.value = if (currentState.isImmediate) {
                UpdateState.Installing
            } else {
                UpdateState.Downloading
            }
        }.addOnFailureListener { exception ->
            _updateState.value = UpdateState.Error(exception.message ?: "Failed to start update")
        }
    }

    /**
     * Complete the update for flexible updates.
     * This will restart the app with the new version.
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * Resume an update that was interrupted.
     * Should be called in onResume of your activity.
     */
    fun resumeUpdateIfNeeded(activity: Activity, requestCode: Int = UPDATE_REQUEST_CODE) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when {
                // For immediate updates, if the update is available, resume it
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        requestCode
                    )
                }
                // For flexible updates, if downloaded, prompt to install
                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                    _updateState.value = UpdateState.Downloaded
                }
            }
        }
    }

    /**
     * Handle the result from the update flow.
     */
    fun handleUpdateResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                // User accepted the update
                if (_updateState.value is UpdateState.Downloading) {
                    // Flexible update started
                }
            }
            Activity.RESULT_CANCELED -> {
                val currentState = _updateState.value
                if (currentState is UpdateState.UpdateAvailable && currentState.isImmediate) {
                    // User cancelled an immediate update - for forced updates, we may want to exit
                    _updateState.value = UpdateState.Error("Update is required to continue")
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
            else -> {
                _updateState.value = UpdateState.Error("Update failed with result code: $resultCode")
            }
        }
    }

    private fun registerInstallStateListener() {
        unregisterInstallStateListener()

        installStateUpdatedListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    if (totalBytesToDownload > 0) {
                        _downloadProgress.value = bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()
                    }
                    _updateState.value = UpdateState.Downloading
                }
                InstallStatus.DOWNLOADED -> {
                    _updateState.value = UpdateState.Downloaded
                    _downloadProgress.value = 1f
                }
                InstallStatus.INSTALLING -> {
                    _updateState.value = UpdateState.Installing
                }
                InstallStatus.INSTALLED -> {
                    _updateState.value = UpdateState.Idle
                    unregisterInstallStateListener()
                }
                InstallStatus.FAILED -> {
                    _updateState.value = UpdateState.Error("Installation failed")
                    unregisterInstallStateListener()
                }
                InstallStatus.CANCELED -> {
                    _updateState.value = UpdateState.Idle
                    unregisterInstallStateListener()
                }
                else -> {}
            }
        }

        installStateUpdatedListener?.let {
            appUpdateManager.registerListener(it)
        }
    }

    private fun unregisterInstallStateListener() {
        installStateUpdatedListener?.let {
            appUpdateManager.unregisterListener(it)
        }
        installStateUpdatedListener = null
    }

    fun cleanup() {
        unregisterInstallStateListener()
    }

    companion object {
        const val UPDATE_REQUEST_CODE = 1001
    }
}

