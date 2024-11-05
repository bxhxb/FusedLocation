package com.example.fusedlocation

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActivityRecognition(private val context: Context) {

    val activityRecognitionClient = ActivityRecognition.getClient(context)
    var latestStatus = ""
    var status = MutableStateFlow("")

    val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("#### got info onReceive, ${intent?.action}")
            intent?.let {
                if (intent.action == CUSTOM_INTENT_ACTIVITY_ACTION) {
                    val result = ActivityTransitionResult.extractResult(it)
                    result?.transitionEvents?.forEach { event ->
                        when (event.activityType) {
                            DetectedActivity.STILL -> {
                                if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                    latestStatus = "Enter the still status"
                                    status.value = latestStatus
                                } else if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                                    latestStatus = "Exit the still status"
                                    status.value = latestStatus
                                }
                            }
                            DetectedActivity.WALKING -> {
                                if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                    latestStatus = "Enter the walking status"
                                    status.value = latestStatus
                                } else if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                                    latestStatus = "Exit the walking status"
                                    status.value = latestStatus
                                }
                            }
                            DetectedActivity.IN_VEHICLE -> {
                                if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                    latestStatus = "Enter the in vehicle status"
                                    status.value = latestStatus
                                } else if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                                    latestStatus = "Exit the in vehicle status"
                                    status.value = latestStatus
                                }
                            }
                            else -> println("#### other activities detected!")
                        }

                        //println("#### the latest status is $latestStatus")
                        //Toast.makeText(context, "the lastest status is $latestStatus", Toast.LENGTH_LONG).show()
                        println("#### the latest status is ${status.value}")
                        Toast.makeText(context, "the lastest status is ${status.value}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private val pendingActivityUpdateIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_ACTIVITY_UPDATE,
            Intent(CUSTOM_INTENT_ACTIVITY_ACTION).apply {
                setPackage(context.packageName)
            },
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
            }
        )
    }

    private val pendingTransitionIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_ACTIVITY_TRANSITION,
            Intent(CUSTOM_INTENT_ACTIVITY_ACTION).apply {
                setPackage(context.packageName)
            },
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
            }
        )
    }

    private val activityTransitions: List<ActivityTransition> by lazy {
        listOf(
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
//            getUserActivity(
//                DetectedActivity.UNKNOWN, ActivityTransition.ACTIVITY_TRANSITION_ENTER
//            ),
//            getUserActivity(
//                DetectedActivity.UNKNOWN, ActivityTransition.ACTIVITY_TRANSITION_EXIT
//            ),
        )
    }

    private fun getUserActivity(detectedActivity: Int, transitionType: Int): ActivityTransition {
        return ActivityTransition.Builder().setActivityType(detectedActivity)
            .setActivityTransition(transitionType).build()

    }

    fun initRecognition(scope: LifecycleCoroutineScope) {
        scope.launch(Dispatchers.IO) {
            delay(1000)

            val intentFilter = IntentFilter(CUSTOM_INTENT_ACTIVITY_ACTION)
            context.registerReceiver(activityReceiver, intentFilter, Context.RECEIVER_EXPORTED)

            delay(1000)

            activityRecognitionClient.requestActivityTransitionUpdates(
                ActivityTransitionRequest(activityTransitions),
                pendingTransitionIntent
            )
                .addOnSuccessListener {
                    println("#### Add activity transition successfully")
                }.addOnFailureListener {
                    println("#### Failed to add activity transition + ${it}")
                }

            delay(1000)

            activityRecognitionClient.requestActivityUpdates(10000L, pendingActivityUpdateIntent
            ).addOnSuccessListener {
                println("#### Add activity updates successfully")
            }.addOnFailureListener {
                println("#### Failed to add activity updates + ${it}")
            }
        }
    }

    fun updateStatus(): StateFlow<String> = status
}