package io.github.hanjoongcho.easypassword.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager

import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.andrognito.patternlockview.utils.ResourceUtils
import com.andrognito.rxpatternlockview.RxPatternLockView
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent
import io.github.hanjoongcho.easypassword.R
import io.github.hanjoongcho.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_pattern_lock.*

import io.reactivex.functions.Consumer

class PatternLockActivity : AppCompatActivity() {

    private var mMode: Int? = -1

    private val mPatternLockViewListener = object : PatternLockViewListener {
        override fun onStarted() {
            Log.d(javaClass.name, "Pattern drawing started")
        }

        override fun onProgress(progressPattern: List<PatternLockView.Dot>) {
            Log.d(javaClass.name, "Pattern progress: " + PatternLockUtils.patternToString(patterLockView, progressPattern))
        }

        override fun onComplete(pattern: List<PatternLockView.Dot>) {
            Log.d(javaClass.name, "Pattern complete: " + PatternLockUtils.patternToString(patterLockView, pattern))
        }

        override fun onCleared() {
            Log.d(javaClass.name, "Pattern has been cleared")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_pattern_lock)

        mMode = intent.getIntExtra(MODE, -1)
        when (mMode) {
            UNLOCK -> guide_message.text = getString(R.string.pattern_lock_activity_unlock_message)
            SETTING_LOCK -> guide_message.text = getString(R.string.pattern_lock_activity_setting_lock_message)
            VERIFY -> guide_message.text = getString(R.string.pattern_lock_activity_verify_message)
        }

        patterLockView.dotCount = 3
        patterLockView.dotNormalSize = ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_size).toInt()
        patterLockView.dotSelectedSize = ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_selected_size).toInt()
        patterLockView.pathWidth = ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_path_width).toInt()
        patterLockView.isAspectRatioEnabled = true
        patterLockView.aspectRatio = PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS
        patterLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT)
        patterLockView.dotAnimationDuration = 150
        patterLockView.pathEndAnimationDuration = 100
        patterLockView.correctStateColor = ResourceUtils.getColor(this, R.color.white)
        patterLockView.isInStealthMode = false
        patterLockView.isTactileFeedbackEnabled = true
        patterLockView.isInputEnabled = true
        patterLockView.addPatternLockListener(mPatternLockViewListener)

        RxPatternLockView.patternComplete(patterLockView).subscribe({ patternLockCompleteEvent ->
            Log.d(javaClass.name, "Complete: " + patternLockCompleteEvent.pattern.toString())
//            val builder: AlertDialog.Builder = AlertDialog.Builder(this@PatternLockActivity)
//            builder.setMessage(patternLockCompleteEvent.pattern.toString())
//            builder.setPositiveButton("OK", DialogInterface.OnClickListener({ _, _ ->
//                finish()
//                AccountSelectionActivity.start(this@PatternLockActivity)
//            }))
//            builder.create().show()
            when (mMode) {
                UNLOCK -> {
                    val savedPattern = CommonUtils.loadStringPreference(this@PatternLockActivity, PatternLockActivity.SAVED_PATTERN, PatternLockActivity.SAVED_PATTERN_DEFAULT)
                    if (savedPattern == patternLockCompleteEvent.pattern.toString()) {
                        AccountSelectionActivity.start(this@PatternLockActivity)
                        finish()
                    } else {
                        patterLockView.clearPattern()
                        val unlockBuilder: AlertDialog.Builder = AlertDialog.Builder(this@PatternLockActivity)
                        unlockBuilder.setMessage(getString(R.string.pattern_lock_activity_verify_reject))
                        unlockBuilder.setPositiveButton("OK", DialogInterface.OnClickListener({ _, _ ->
                            finish()
                        }))
                        unlockBuilder.setCancelable(false)
                        unlockBuilder.create().show()
                    }
                }
                SETTING_LOCK -> {
                    val intent = Intent(this, PatternLockActivity::class.java)
                    intent.putExtra(PatternLockActivity.MODE, PatternLockActivity.VERIFY)
                    intent.putExtra(PatternLockActivity.REQUEST_PATTERN, patternLockCompleteEvent.pattern.toString())
                    startActivity(intent)
                    finish()
                }
                VERIFY -> {
                    if (intent.getStringExtra(PatternLockActivity.REQUEST_PATTERN) == patternLockCompleteEvent.pattern.toString()) {
                        CommonUtils.saveStringPreference(this@PatternLockActivity, PatternLockActivity.SAVED_PATTERN, patternLockCompleteEvent.pattern.toString())
                        AccountSelectionActivity.start(this@PatternLockActivity)
                        finish()
                    } else {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@PatternLockActivity)
                        builder.setMessage(getString(R.string.pattern_lock_activity_verify_reject))
                        builder.setPositiveButton("OK", DialogInterface.OnClickListener({ _, _ ->
                            intent.putExtra(PatternLockActivity.MODE, PatternLockActivity.SETTING_LOCK)
                            startActivity(intent)
                            finish()
                        }))
                        builder.create().show()
                    }
                }
            }
        })
//                .subscribe(object : Consumer<PatternLockCompleteEvent> {
//                    @Throws(Exception::class)
//                    override fun accept(patternLockCompleteEvent: PatternLockCompleteEvent) {
////                        Log.d(javaClass.name, "Complete: " + patternLockCompleteEvent.pattern.toString())
//                        val builder: AlertDialog.Builder = AlertDialog.Builder(applicationContext)
//                        builder.setMessage(patternLockCompleteEvent.pattern.toString())
//                        builder.setPositiveButton("OK", null)
//                        builder.create().show()
//                        patterLockView.clearPattern()
//                    }
//                })

        RxPatternLockView.patternChanges(patterLockView)
                .subscribe(object : Consumer<PatternLockCompoundEvent> {
                    @Throws(Exception::class)
                    override fun accept(event: PatternLockCompoundEvent) {
                        when (event.eventType) {
                            PatternLockCompoundEvent.EventType.PATTERN_STARTED -> Log.d(javaClass.name, "Pattern drawing started")
                            PatternLockCompoundEvent.EventType.PATTERN_PROGRESS -> Log.d(javaClass.name, "Pattern progress: " + PatternLockUtils.patternToString(patterLockView, event.pattern))
                            PatternLockCompoundEvent.EventType.PATTERN_COMPLETE -> Log.d(javaClass.name, "Pattern complete: " + PatternLockUtils.patternToString(patterLockView, event.pattern))
                            PatternLockCompoundEvent.EventType.PATTERN_CLEARED ->  Log.d(javaClass.name, "Pattern has been cleared")
                        }
                    }
                })
    }

    companion object {
        const val MODE = "mMode"
        const val REQUEST_PATTERN = "request_pattern"
        const val SAVED_PATTERN = "saved_pattern"
        const val SAVED_PATTERN_DEFAULT = "NA"
        const val UNLOCK = 1
        const val SETTING_LOCK = 2
        const val VERIFY = 3
    }
}
