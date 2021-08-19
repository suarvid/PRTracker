package se.umu.arsu0013.prtracker

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import se.umu.arsu0013.prtracker.database.LiftRepository
import java.util.*

private const val TAG = "LiftDetailViewModel"
class LiftDetailViewModel : ViewModel() {

    private val liftRepository = LiftRepository.getInstance()
    private val liftIdLiveData = MutableLiveData<UUID>()


    // viewModels should not expose MutableLiveData
    // switchMap used to automatically get a new Lift when the liftId is updated,
    // so the Fragment only has to observe the data once
    var liftLiveData: LiveData<Lift?> =
        Transformations.switchMap(liftIdLiveData) { liftId ->
            liftRepository.getLiftById(liftId)
        }

    fun loadLift(liftId: UUID) {
        liftIdLiveData.value = liftId
    }

    fun saveLift(lift: Lift) {
        Log.d(TAG, "Saved lift with Id ${lift.id}")
        liftRepository.updateLift(lift)
    }

}