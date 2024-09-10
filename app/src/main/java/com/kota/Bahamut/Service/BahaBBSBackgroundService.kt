package com.kota.Bahamut.Service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.Telnet.TelnetClient

class BahaBBSBackgroundService : Service() {
    private var myClient: TelnetClient? = null
    private var myController: ASNavigationController? = null

    // android.app.Service
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    // android.app.Service
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            myClient = TelnetClient.getClient()
            myController = ASNavigationController.getCurrentController()
        } catch (e:Exception) {
            Log.i("BahaBBS", "BackgroundService start fail.")
        }
        Log.i("BahaBBS", "BackgroundService start.")
        return super.onStartCommand(intent, flags, startId)
    }

    // android.app.Service
    override fun onDestroy() {
        super.onDestroy()
        myClient = null
        myController = null
        Log.i("BahaBBS", "BackgroundService finish.")
    }
}
