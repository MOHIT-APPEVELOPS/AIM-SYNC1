package com.aimsync.pro.system

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

object SystemOptimizer {

    private fun launch(context: Context, action: String) {
        val intent = Intent(action)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openPointerSpeedSettings(context: Context) {
        try {
            launch(context, Settings.ACTION_INPUT_METHOD_SETTINGS)
            Toast.makeText(
                context,
                "Look for 'Pointer speed' in this menu (location varies by phone)",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            try {
                launch(context, Settings.ACTION_SETTINGS)
            } catch (e2: Exception) {
                Toast.makeText(context, "Could not open Settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openTouchDelaySettings(context: Context) {
        try {
            launch(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)
            Toast.makeText(context, "Set Touch & Hold Delay to SHORT", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Open Accessibility > Touch & Hold Delay", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleGamingDnd(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            Toast.makeText(context, "Grant DND access first", Toast.LENGTH_LONG).show()
            try {
                launch(context, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open DND settings", Toast.LENGTH_SHORT).show()
            }
            return
        }
        if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            Toast.makeText(context, "Gaming DND ON", Toast.LENGTH_SHORT).show()
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            Toast.makeText(context, "Gaming DND OFF", Toast.LENGTH_SHORT).show()
        }
    }

    private val gameSpacePackages = listOf(
        "com.oplus.games",
        "com.coloros.gamespaceui",
        "com.vivo.gamewatch",
        "com.vivo.game"
    )

    /** Opens the phone's own Game Space / Game Mode (where touch boost lives), else Developer options. */
    fun openGameBoostSettings(context: Context) {
        for (pkg in gameSpacePackages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        "Look for Touch sampling / Touch boost / Performance options",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } catch (e: Exception) {
                    // try next package
                }
            }
        }
        try {
            launch(context, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            Toast.makeText(
                context,
                "Game Space not found. Check Developer options for touch / refresh rate settings",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Open your phone's Game Mode settings manually", Toast.LENGTH_LONG).show()
        }
    }

    /** Requests the highest refresh rate (same resolution) for this app's window only. */
    @Suppress("DEPRECATION")
    fun requestMaxRefreshRate(activity: Activity) {
        val display = activity.windowManager.defaultDisplay
        val current = display.mode
        var best: android.view.Display.Mode? = null
        for (m in display.supportedModes) {
            if (m.physicalWidth == current.physicalWidth && m.physicalHeight == current.physicalHeight) {
                if (best == null || m.refreshRate > best.refreshRate) best = m
            }
        }
        if (best == null) {
            Toast.makeText(activity, "No refresh rate modes found", Toast.LENGTH_SHORT).show()
            return
        }
        val lp = activity.window.attributes
        lp.preferredDisplayModeId = best.modeId
        activity.window.attributes = lp
        Toast.makeText(
            activity,
            "Requested " + best.refreshRate.toInt() + " Hz for this app. Slide in the touch box to test.",
            Toast.LENGTH_LONG
        ).show()
    }

    fun openBatterySettings(context: Context) {
        try {
            launch(context, Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            Toast.makeText(context, "Set BGMI to 'Don't optimize' / unrestricted", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open battery settings", Toast.LENGTH_SHORT).show()
        }
    }
}
