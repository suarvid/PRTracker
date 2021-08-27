package se.umu.arsu0013.prtracker

import androidx.lifecycle.ViewModel
import se.umu.arsu0013.prtracker.database.LiftRepository
import java.util.*

class LiftListViewModel : ViewModel() {
    private val liftRepository = LiftRepository.getInstance()
    val liftListLiveData = liftRepository.getLifts()

    fun addLift(lift: Lift) {
        liftRepository.addLift(lift)
    }

    fun deleteLift(lift: Lift) {
        liftRepository.deleteLift(lift)
    }

    fun deleteLift(position: Int) {
        liftRepository.deleteLift(liftListLiveData.value!![position])
    }

    fun deleteLiftWithId(id: UUID) {
        liftRepository.deleteLiftWithId(id)
    }


}