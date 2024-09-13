package com.lonx.ecjtu.hjcalendar.service

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService

class TomorrowRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TomorrowRemoteViewsFactory(this.applicationContext, intent)
    }
}
