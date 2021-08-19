package se.umu.arsu0013.prtracker

import android.app.Application
import se.umu.arsu0013.prtracker.database.LiftRepository

class PRTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LiftRepository.initialize(this)
    }
}