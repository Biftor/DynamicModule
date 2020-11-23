package com.dummy.ondemand

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    @Volatile
    private lateinit var splitInstallManager: SplitInstallManager
    private var requestedModule: String = ""
    private var mSessionID = 0
    private val listener = SplitInstallStateUpdatedListener { state: SplitInstallSessionState ->
            if (state.sessionId() == mSessionID) {
                when (state.status()) {
                    SplitInstallSessionStatus.INSTALLED -> {
                        showToast("Module installed successfully")
                        var i = 0
                        while (i < state.moduleNames().size) {
                            showToast("Module installed moduleName ${state.moduleNames()[i]}")
                            i++
                        }
                    }
                    SplitInstallSessionStatus.DOWNLOADING -> showToast("Downloading")
                    SplitInstallSessionStatus.FAILED,
                    SplitInstallSessionStatus.CANCELED,
                    SplitInstallSessionStatus.CANCELING,
                    SplitInstallSessionStatus.DOWNLOADED,
                    SplitInstallSessionStatus.INSTALLING,
                    SplitInstallSessionStatus.PENDING,
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION,
                    SplitInstallSessionStatus.UNKNOWN -> showToast("Finished")
                }
            }
        }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "showToast: $s")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         splitInstallManager =
            SplitInstallManagerFactory.create(applicationContext)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val downloadFeature = Button(this)
        downloadFeature.text = "Install feature"
        root.addView(downloadFeature)
        downloadFeature.setOnClickListener {
            requestInstallModule("dynamicfeature")
        }
        
        val startFeatureActivity = Button(this)
        startFeatureActivity.text = "Start Feature activity"
        root.addView(startFeatureActivity)
        startFeatureActivity.setOnClickListener {
            try {
                val intent = Intent()
                intent.setClassName(
                    applicationContext.packageName,
                    "com.dummy.dynamicfeature.FeatureActivity"
                )
                startActivity(intent)
            } catch (ignore: Exception) {

            }
        }
        
        setContentView(root)
    }

    override fun onResume() {
        // Listener can be registered even without directly triggering a download.
        splitInstallManager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        // Make sure to dispose of the listener once it's no longer needed.
        splitInstallManager.unregisterListener(listener)
        super.onPause()
    }

    private fun requestInstallModule(module: String) {
        if (requestedModule != module) {
            showToast("Requesting to install $module")
            val request = SplitInstallRequest
                .newBuilder()
                .addModule(module)
                .build()
            splitInstallManager
                .startInstall(request).addOnSuccessListener { id ->
                    mSessionID = id
                }.addOnFailureListener { exception ->
                    try {
                        Log.e(
                            TAG,
                            "Error installing module: ",
                            exception
                        )
                        if (exception is SplitInstallException) {
                            when (exception.errorCode) {
                                SplitInstallErrorCode.NETWORK_ERROR -> showToast(
                                    "Installation for module: $module failed with error: Check your internet connection"
                                )
                                SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED -> {
                                    showToast(
                                        "Installation for module: $module failed with error: ACCESS_DENIED"
                                    )
                                    checkForActiveDownloads(module)
                                }
                                SplitInstallErrorCode.ACCESS_DENIED -> showToast(
                                    "Installation for module: $module failed with error: ACCESS_DENIED"
                                )
                                SplitInstallErrorCode.API_NOT_AVAILABLE -> showToast(
                                    "Installation for module: $module failed with error: API_NOT_AVAILABLE"
                                )
                                SplitInstallErrorCode.APP_NOT_OWNED -> showToast(
                                    "Installation for module: $module failed with error: APP_NOT_OWNED"
                                )
                                SplitInstallErrorCode.INCOMPATIBLE_WITH_EXISTING_SESSION -> showToast(
                                    "Installation for module: $module failed with error: INCOMPATIBLE_WITH_EXISTING_SESSION"
                                )
                                SplitInstallErrorCode.INSUFFICIENT_STORAGE -> showToast(
                                    "Installation for module: $module failed with error: INSUFFICIENT_STORAGE"
                                )
                                SplitInstallErrorCode.INTERNAL_ERROR -> showToast(
                                    "Installation for module: $module failed with error: INTERNAL_ERROR"
                                )
                                SplitInstallErrorCode.INVALID_REQUEST -> showToast(
                                    "Installation for module: $module failed with error: INVALID_REQUEST"
                                )
                                SplitInstallErrorCode.MODULE_UNAVAILABLE -> showToast(
                                    "Installation for module: $module failed with error: MODULE_UNAVAILABLE"
                                )
                                SplitInstallErrorCode.PLAY_STORE_NOT_FOUND -> showToast(
                                    "Installation for module: $module failed with error: PLAY_STORE_NOT_FOUND"
                                )
                                SplitInstallErrorCode.NO_ERROR -> showToast(
                                    "Installation for module: $module failed with error: NO_ERROR"
                                )
                                SplitInstallErrorCode.SERVICE_DIED -> showToast(
                                    "Installation for module: $module failed with error: SERVICE_DIED"
                                )
                                SplitInstallErrorCode.SESSION_NOT_FOUND -> showToast(
                                    "Installation for module: $module failed with error: SESSION_NOT_FOUND"
                                )
                                SplitInstallErrorCode.SPLITCOMPAT_COPY_ERROR -> showToast(
                                    "Installation for module: $module failed with error: SPLITCOMPAT_COPY_ERROR"
                                )
                                SplitInstallErrorCode.SPLITCOMPAT_EMULATION_ERROR -> showToast(
                                    "Installation for module: $module failed with error: SPLITCOMPAT_EMULATION_ERROR"
                                )
                                SplitInstallErrorCode.SPLITCOMPAT_VERIFICATION_ERROR -> showToast(
                                    "Installation for module: $module failed with error: SPLITCOMPAT_VERIFICATION_ERROR"
                                )
                            }
                        } else {
                            showToast(
                                "Error installing module: ${Log.getStackTraceString(exception)}"
                            )
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e(
                            TAG,
                            "Request install module: ",
                            exception
                        )
                    }
                    requestedModule = ""
                }
            requestedModule = module
        }
    }

    private fun checkForActiveDownloads(module: String) {
        splitInstallManager // Returns a SplitInstallSessionState object for each active session as a List.
            .sessionStates
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Check for active sessions.
                    if (task.result is SplitInstallSessionState) {
                        for (state in task.result) {
                            if (state.status() == SplitInstallSessionStatus.DOWNLOADING) {
                                // Cancel the request, or request a deferred installation.
                                splitInstallManager
                                    .deferredInstall(listOf(module))
                            }
                        }
                    }
                }
            }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.install(newBase)
    }

}