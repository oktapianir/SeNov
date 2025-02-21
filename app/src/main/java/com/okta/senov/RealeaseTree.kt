package com.okta.senov

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.logger.Logger
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Logger.Level.DEBUG.ordinal || priority == Logger.Level.VERBOSE.ordinal) {
            return
        }

        FirebaseCrashlytics.getInstance().log(message)
    }
}
