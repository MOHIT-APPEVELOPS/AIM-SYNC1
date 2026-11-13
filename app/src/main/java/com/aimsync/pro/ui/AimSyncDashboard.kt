package com.aimsync.pro.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.aimsync.pro.engine.GyroCalibrator
import com.aimsync.pro.system.SystemOptimizer
import kotlin.math.roundToInt

/**
 * Programmatic (no XML, no Compose) dashboard for AimSync Pro.
 * MainActivity only calls build() and stop().
 */
class AimSyncDashboard(private val activity: Activity) {

    private companion object {
        val BG = 0xFF0B0E14.toInt()
        val CARD = 0xFF151A24.toInt()
        val CYAN = 0xFF00F5D4.toInt()
        val PURPLE = 0xFF9B5DE5.toInt()
        val GREY = 0xFFAAAAAA.toInt()
        const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
        const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    private val density = activity.resources.displayMetrics.density
    private val calibrator = GyroCalibrator(activity)

    private lateinit var gyroProgress: ProgressBar
    private lateinit var gyroStatus: TextView
    private lateinit var touchRateText: TextView

    private fun dp(value: Int): Int = (value * density).roundToInt()

    private fun makeText(content: String, size: Float, color: Int, bold: Boolean = false): TextView {
        val tv = TextView(activity)
        tv.text = content
        tv.textSize = size
        tv.setTextColor(color)
        if (bold) tv.setTypeface(null, Typeface.BOLD)
        return tv
    }

    private fun makeCard(title: String): LinearLayout {
        val card = LinearLayout(activity)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(16), dp(14), dp(16), dp(14))
        val bg = GradientDrawable()
        bg.setColor(CARD)
        bg.cornerRadius = dp(14).toFloat()
        bg.setStroke(dp(1), 0x3300F5D4)
        card.background = bg
        val lp = LinearLayout.LayoutParams(MATCH, WRAP)
        lp.setMargins(0, 0, 0, dp(14))
        card.layoutParams = lp
        card.addView(makeText(title, 15f, CYAN, true))
        return card
    }

    private fun makeButton(label: String, color: Int, textColor: Int, onClick: () -> Unit): Button {
        val b = Button(activity)
        b.text = label
        b.setTextColor(textColor)
        val bg = GradientDrawable()
        bg.setColor(color)
        bg.cornerRadius = dp(10).toFloat()
        b.background = bg
        val lp = LinearLayout.LayoutParams(MATCH, WRAP)
        lp.topMargin = dp(8)
        b.layoutParams = lp
        b.setOnClickListener { onClick() }
        return b
    }

    fun build(): View {
        val content = LinearLayout(activity)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(16), dp(28), dp(16), dp(32))

        content.addView(makeText("\uD83C\uDFAF AIMSYNC PRO", 24f, CYAN, true))
        val subtitle = makeText("BGMI game setup helper for Oppo / realme", 13f, GREY)
        subtitle.setPadding(0, 0, 0, dp(14))
        content.addView(subtitle)

        // Honest note
        val noteCard = makeCard("Important")
        val note = makeText(
            "An app cannot change touch response inside BGMI. " +
                "Your phone's own Game Space / Game Mode controls that. " +
                "Use the buttons below to set it up quickly.",
            13f, Color.WHITE
        )
        note.setPadding(0, dp(6), 0, 0)
        noteCard.addView(note)
        content.addView(noteCard)

        // Game setup
        val setupCard = makeCard("Game setup (do once)")
        setupCard.addView(makeButton("1. OPEN GAME SPACE (TOUCH BOOST)", CYAN, Color.BLACK) {
            SystemOptimizer.openGameBoostSettings(activity)
        })
        setupCard.addView(makeButton("2. BATTERY: DON'T OPTIMIZE BGMI", PURPLE, Color.WHITE) {
            SystemOptimizer.openBatterySettings(activity)
        })
        setupCard.addView(makeButton("3. GAMING DND ON / OFF", PURPLE, Color.WHITE) {
            SystemOptimizer.toggleGamingDnd(activity)
        })
        setupCard.addView(makeButton("4. TOUCH & HOLD DELAY", PURPLE, Color.WHITE) {
            SystemOptimizer.openTouchDelaySettings(activity)
        })
        setupCard.addView(makeButton("5. POINTER / INPUT SETTINGS", PURPLE, Color.WHITE) {
            SystemOptimizer.openPointerSpeedSettings(activity)
        })
        val tips = makeText(
            "In Game Space: add BGMI, turn on Performance / High performance mode and any " +
                "Touch boost or Touch response option. Also set Display > Refresh rate to High, " +
                "turn off battery saver, and remove screen protector or glove mode.",
            12f, GREY
        )
        tips.setPadding(0, dp(10), 0, 0)
        setupCard.addView(tips)
        content.addView(setupCard)

        // Gyro test
        val gyroCard = makeCard("Gyroscope stability test")
        gyroProgress = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal)
        gyroProgress.max = 100
        val gp = LinearLayout.LayoutParams(MATCH, WRAP)
        gp.topMargin = dp(8)
        gyroCard.addView(gyroProgress, gp)
        gyroStatus = makeText("Keep the phone flat and still, then tap the button.", 13f, Color.WHITE)
        gyroCard.addView(gyroStatus)
        gyroCard.addView(makeButton("TEST GYRO", CYAN, Color.BLACK) { startGyro() })
        gyroCard.addView(
            makeText("This only measures gyro drift. It does not change the game.", 11f, GREY)
        )
        content.addView(gyroCard)

        // Touch rate
        val touchCard = makeCard("Live touch sampling rate")
        touchRateText = makeText("-- Hz", 22f, CYAN, true)
        touchRateText.setPadding(0, dp(6), 0, dp(6))
        touchCard.addView(touchRateText)
        val touchView = TouchRateView(activity)
        val tvBg = GradientDrawable()
        tvBg.setColor(0xFF0B0E14.toInt())
        tvBg.cornerRadius = dp(10).toFloat()
        touchView.background = tvBg
        touchView.onRate = { hz -> touchRateText.text = "$hz Hz" }
        touchCard.addView(touchView, LinearLayout.LayoutParams(MATCH, dp(140)))
        touchCard.addView(makeButton("REQUEST MAX REFRESH RATE (THIS APP)", CYAN, Color.BLACK) {
            SystemOptimizer.requestMaxRefreshRate(activity)
        })
        touchCard.addView(
            makeText("Test only. Works inside this app, not inside BGMI.", 11f, GREY)
        )
        content.addView(touchCard)

        val scroll = ScrollView(activity)
        scroll.setBackgroundColor(BG)
        scroll.isFillViewport = true
        scroll.addView(content)
        return scroll
    }

    fun stop() {
        calibrator.stop()
    }

    private fun startGyro() {
        val sm = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
            gyroStatus.text = "No gyroscope found on this device."
            return
        }
        gyroProgress.progress = 0
        gyroStatus.text = "Testing... keep the phone flat and still."
        calibrator.startCalibration { progress, drift, done ->
            activity.runOnUiThread {
                gyroProgress.progress = (progress * 100f).roundToInt()
                if (done) {
                    val rating = when {
                        drift < 0.01f -> "very stable"
                        drift < 0.03f -> "acceptable"
                        else -> "high drift, try again on a flat surface"
                    }
                    gyroStatus.text = "Done. Drift: " + "%.4f".format(drift) + " rad/s (" + rating + ")"
                }
            }
        }
    }
}
