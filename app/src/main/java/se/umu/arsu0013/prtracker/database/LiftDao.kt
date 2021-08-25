package se.umu.arsu0013.prtracker.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import se.umu.arsu0013.prtracker.Lift
import java.util.*

@Dao
interface LiftDao {

    @Query("SELECT * FROM lift")
    fun getLifts(): LiveData<List<Lift>>

    @Query("SELECT * FROM lift WHERE id=(:id)")
    fun getLiftById(id: UUID): LiveData<Lift?>

    @Query("SELECT * from lift WHERE weight > (:weight)")
    fun getLiftsOverWeight(weight: Int): LiveData<List<Lift>>

    @Query("SELECT * from lift WHERE exercise = (:exercise)")
    fun getLiftsWithExercise(exercise: String): LiveData<List<Lift>>

    @Update
    fun updateLift(lift: Lift)

    @Insert
    fun addLift(lift: Lift)

    @Delete
    fun deleteLift(lift: Lift)

}