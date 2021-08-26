package se.umu.arsu0013.prtracker.database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import se.umu.arsu0013.prtracker.Lift
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "lift-database"
private const val TAG = "LiftRepository"
class LiftRepository private constructor(context: Context) {

    private val database: LiftDatabase = Room.databaseBuilder(
        context.applicationContext,
        LiftDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val liftDao = database.liftDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getLifts(): LiveData<List<Lift>> = liftDao.getLifts()

    fun getLiftById(id: UUID): LiveData<Lift?> = liftDao.getLiftById(id)

    fun getLiftsOverWeight(weight: Int): LiveData<List<Lift>> = liftDao.getLiftsOverWeight(weight)

    fun getLiftsWithExercise(exercise: String) = liftDao.getLiftsWithExercise(exercise)

    fun updateLift(lift: Lift) {
        executor.execute {
            Log.d(TAG, "Saved lift with Id ${lift.id}")
            liftDao.updateLift(lift)
        }
    }

    fun addLift(lift: Lift) {
        executor.execute {
            liftDao.addLift(lift)
        }
    }

    fun deleteLift(lift: Lift) {
        executor.execute {
            liftDao.deleteLift(lift)
        }
    }

    companion object {
        private var INSTANCE: LiftRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = LiftRepository(context)
            }
        }

        // might be better to just initialize it instead of throwing an exception?
        // although that requires a context object, so might not always work as we want it to
        fun getInstance(): LiftRepository {
            return INSTANCE ?:
            throw IllegalStateException("LiftRepository must be initialized before access")
        }
    }
}