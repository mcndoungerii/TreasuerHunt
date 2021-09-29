/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.treasureHunt

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.android.treasureHunt.HuntMainActivity.Companion.ACTION_GEOFENCE_EVENT
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/*
 * Triggered by the Geofence.  Since we only have one active Geofence at once, we pull the request
 * ID from the first Geofence, and locate it within the registered landmark data in our
 * GeofencingConstants within GeofenceUtils, which is a linear string search. If we had  very large
 * numbers of Geofence possibilities, it might make sense to use a different data structure.  We
 * then pass the Geofence index into the notification, which allows us to have a custom "found"
 * message associated with each Geofence.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: Step 11 implement the onReceive method
        /**
         * A Broadcast Receiver can receive many types of actions. For this app,
         * you only need to know when the geofence has been entered.
         * Check that the intent's action is of type ACTION_GEOFENCE_EVENT
         */
        if(intent.action == ACTION_GEOFENCE_EVENT){
            //reate a variable called geofencingEvent and initialize it to GeofencingEvent with the intent passed in.
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            /**
             * If there is an error, you need to understand what went wrong.
             * Save a variable with the error message obtained through the geofences error code.
             * Log that message and return from the method
             */

            if(geofencingEvent.hasError()){
                val errorMessage = errorMessage(context,geofencingEvent.errorCode)
                Log.e(TAG,errorMessage)
                return
            }

            //Check if the geofenceTransition type is ENTER
            if(geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                /**
                 * If the triggeringGeofences array is not empty, set the fenceID to the first geofence's
                 * requestId. You would only have one geofence active at a time, so if the array
                 * is non-empty, there would only be one to interact with. If the array is empty,
                 * log a message and return.
                 */
                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() -> {
                        geofencingEvent.triggeringGeofences[0].requestId
                    }
                    else -> {
                        Log.e(TAG,"No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                /**
                 * Check that the geofence is consistent with the constants listed in GeofenceUtil.kt.
                 * If not, print a log and return
                 */
                val foundIndex = GeofencingConstants.LANDMARK_DATA.indexOfFirst {
                    it.id == fenceId
                }

                if (-1 == foundIndex){
                    Log.e(TAG,"Unknown Geofence: Abort Mission")
                    return
                }
                /**
                 * If your code execution has gotten this far, the user has entered a valid geofence.
                 * Send a notification telling them the good news!
                 */
                val notificationManager = ContextCompat.getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.sendGeofenceEnteredNotification(context,foundIndex)
            }
        }
    }
}

private const val TAG = "GeofenceReceiver"
