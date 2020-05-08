package io.github.mattpvaughn.tomat.features.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.Service
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.mattpvaughn.tomat.R
import io.github.mattpvaughn.tomat.application.APP_NAME
import io.github.mattpvaughn.tomat.application.CustomApplication
import io.github.mattpvaughn.tomat.application.MILLIS_PER_SECOND
import io.github.mattpvaughn.tomat.application.SECONDS_PER_MINUTE
import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.KEY_BREAK_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.KEY_NORMAL_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroFragment.Companion.ACTION_SEND_CLICK
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroService.State.*
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Service containing countdown timer for the app. The single source of truth for the [State]
 * and [secondsRemaining] in the countdown. Responsible for broadcasting any and all changes in
 * [State] and [secondsRemaining] via [LocalBroadcastManager]
 *
 * Note: The countdown and state ideally would have been extracted into a separate class, but I
 * ran out of time
 */
class PomodoroService : Service() {

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        createNotification()
    }

    @Inject
    lateinit var prefsRepo: PrefsRepo

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val localBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private var state: State = RESTING
    var pomodoroDurationSeconds: Int = Int.MAX_VALUE
    var breakDurationSeconds: Int = Int.MAX_VALUE
    private var secondsRemaining: Int = Int.MAX_VALUE

    private var countDownTimer: CountDownTimer = makeCountdownTimer(pomodoroDurationSeconds)

    private fun makeCountdownTimer(seconds: Int): CountDownTimer {
        // Use interval of 500ms instead of 1000ms b/c it skips last tick otherwise
        return object : CountDownTimer(seconds * MILLIS_PER_SECOND, MILLIS_PER_SECOND / 4) {
            override fun onFinish() {
                state = when (state) {
                    POMODORO_FINISHED, POMODORO_COUNTDOWN -> {
                        secondsRemaining = breakDurationSeconds
                        POMODORO_FINISHED
                    }
                    RESTING, BREAK_FINISHED, BREAK_COUNTDOWN -> {
                        secondsRemaining = pomodoroDurationSeconds
                        RESTING
                    }
                }
                shareStateWithUI()
            }

            override fun onTick(millisUntilFinished: Long) {
                val tempSecondsRemaining = (millisUntilFinished.toFloat() / 1000F).roundToInt()
                // ensure we only update every second, even though the timer ticks every quarter
                // second
                if (tempSecondsRemaining != secondsRemaining) {
                    secondsRemaining = tempSecondsRemaining
                    Log.i(
                        APP_NAME, "Seconds = $secondsRemaining, millis = $millisUntilFinished"
                    )
                    shareStateWithUI()
                    updateNotification()
                }
            }

        }
    }


    enum class State {
        RESTING, POMODORO_COUNTDOWN, POMODORO_FINISHED, BREAK_COUNTDOWN, BREAK_FINISHED;

        companion object {
            const val RESTING_STRING = "RESTING"
            const val POMODORO_COUNTDOWN_STRING = "POMODORO_COUNTDOWN"
            const val POMODORO_FINISHED_STRING = "POMODORO_FINISHED"
            const val BREAK_COUNTDOWN_STRING = "BREAK_COUNTDOWN"
            const val BREAK_FINISHED_STRING = "BREAK_FINISHED"
        }

        override fun toString(): String {
            return when (this) {
                RESTING -> RESTING_STRING
                POMODORO_COUNTDOWN -> POMODORO_COUNTDOWN_STRING
                POMODORO_FINISHED -> POMODORO_FINISHED_STRING
                BREAK_COUNTDOWN -> BREAK_COUNTDOWN_STRING
                BREAK_FINISHED -> BREAK_FINISHED_STRING
            }
        }
    }

    companion object {
        const val COUNTDOWN_BROADCAST_NAME = "countdown broadcast"

        const val NOTIFICATION_CHANNEL_ID = "Tomat"
        const val NOTIFICATION_ID = 2022011
        const val NOTIFICATION_CHANNEL_NAME = "Tomat Timer"
        const val NOTIFICATION_CHANNEL_DESC = "Countdown for Tomat Pomodoro timer"
        const val NOTIFICATION_TITLE = "Tomat Timer"
        const val NOTIFICATION_TEXT_PLACEHOLDER = "Time remaining: "

        const val EXTRA_COUNTDOWN_STATE = "pom state"
        const val EXTRA_SECONDS_REMAINING = "timeleft"
    }

    private val clickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(APP_NAME, "goober")
            if (intent != null && intent.action == ACTION_SEND_CLICK) {
                handleClick()
            }
        }

    }

    fun handleClick() {
        when (state) {
            RESTING -> startPomodoroCountDown()
            POMODORO_COUNTDOWN -> goToRestingState()
            POMODORO_FINISHED -> startBreakCountdown() // should automatically start
            BREAK_COUNTDOWN -> skipBreak()
            BREAK_FINISHED -> goToRestingState()
        }
    }

    private fun skipBreak() {
        countDownTimer.cancel()
        state = RESTING
        secondsRemaining = pomodoroDurationSeconds
        countDownTimer = makeCountdownTimer(pomodoroDurationSeconds)
        shareStateWithUI()
    }

    private fun startBreakCountdown() {
        countDownTimer.cancel()
        state = BREAK_COUNTDOWN
        secondsRemaining = breakDurationSeconds
        countDownTimer = makeCountdownTimer(breakDurationSeconds)
        countDownTimer.start()
    }


    private fun goToRestingState() {
        countDownTimer.cancel()
        state = RESTING
        secondsRemaining = pomodoroDurationSeconds
        countDownTimer.cancel()
        shareStateWithUI()
    }

    private fun startPomodoroCountDown() {
        countDownTimer.cancel()
        state = POMODORO_COUNTDOWN
        countDownTimer = makeCountdownTimer(pomodoroDurationSeconds)
        countDownTimer.start()
    }

    private val stateIntent = Intent(COUNTDOWN_BROADCAST_NAME).apply {
        putExtra(EXTRA_COUNTDOWN_STATE, state.toString())
        putExtra(EXTRA_SECONDS_REMAINING, 0)
    }

    private fun shareStateWithUI() {
        stateIntent.putExtra(EXTRA_COUNTDOWN_STATE, state.toString())
        stateIntent.putExtra(EXTRA_SECONDS_REMAINING, secondsRemaining)
        localBroadcastManager.sendBroadcast(stateIntent)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        (application as CustomApplication).appComponent.inject(this)

        super.onCreate()

        Log.i(APP_NAME, "PomodoroService created")
        pomodoroDurationSeconds =
            prefsRepo.getInt(KEY_NORMAL_TIMER_DURATION_MINUTES) * SECONDS_PER_MINUTE
        breakDurationSeconds =
            prefsRepo.getInt(KEY_BREAK_TIMER_DURATION_MINUTES) * SECONDS_PER_MINUTE

        secondsRemaining = pomodoroDurationSeconds
        countDownTimer = makeCountdownTimer(secondsRemaining)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(clickReceiver, IntentFilter(ACTION_SEND_CLICK))

        createNotificationChannel()
        val notification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID, notification)
        updateNotification()
        shareStateWithUI()

        sharedPreferences.registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener { prefs: SharedPreferences?, key: String? ->
            when (key) {
                KEY_NORMAL_TIMER_DURATION_MINUTES -> {
                    pomodoroDurationSeconds =
                        prefsRepo.getInt(KEY_NORMAL_TIMER_DURATION_MINUTES) * SECONDS_PER_MINUTE
                    // update time left if we are about to start a new pomodoro cycle
                    if (state == BREAK_FINISHED || state == RESTING) {
                        secondsRemaining = pomodoroDurationSeconds
                    }
                }
                KEY_BREAK_TIMER_DURATION_MINUTES -> {
                    breakDurationSeconds =
                        prefsRepo.getInt(KEY_BREAK_TIMER_DURATION_MINUTES) * SECONDS_PER_MINUTE
                    if (state == POMODORO_FINISHED) {
                        secondsRemaining = breakDurationSeconds
                    }
                }
                else -> throw NoWhenBranchMatchedException()
            }
            shareStateWithUI()
        })

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(clickReceiver)
        stopForeground(true)
        super.onDestroy()
    }

    private fun updateNotification() {
        notificationBuilder.setContentText(
            "$NOTIFICATION_TEXT_PLACEHOLDER${DateUtils.formatElapsedTime((secondsRemaining / 1000).toLong())}"
        )
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun createNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            priority = PRIORITY_DEFAULT
            setSmallIcon(R.drawable.tomato)
            setContentTitle(NOTIFICATION_TITLE)
            setContentText(NOTIFICATION_TEXT_PLACEHOLDER)
            setAutoCancel(true)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val descriptionText = NOTIFICATION_CHANNEL_DESC
            val importance = IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

