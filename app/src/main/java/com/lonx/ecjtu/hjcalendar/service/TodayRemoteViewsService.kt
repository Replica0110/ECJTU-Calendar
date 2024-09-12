package com.lonx.ecjtu.hjcalendar.service

import android.content.Intent
import android.widget.RemoteViewsService

class TodayRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodayRemoteViewsFactory(this.applicationContext, intent)
    }
}