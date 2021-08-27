package se.umu.arsu0013.prtracker

import android.content.Context
import android.util.Log
import android.widget.AdapterView

private const val TAG = "SortLiftListener"

class SortLiftManager {

    fun onItemSelected(
        context: Context,
        parent: AdapterView<*>?,
        position: Int,
        lifts: List<Lift>?
    ): List<Lift>? {

        val selectedString = parent?.getItemAtPosition(position).toString()
        Log.d(TAG, selectedString)

        return when (selectedString) {
            context.getString(R.string.exercise_alphabetical) -> sortByAlphabetical(lifts)
            context.getString(R.string.heaviest_first) -> sortByHeaviestFirst(lifts)
            context.getString(R.string.lightest_first) -> sortByLightestFirst(lifts)
            context.getString(R.string.most_recent) -> sortByMostRecentFirst(lifts)
            context.getString(R.string.least_recent) -> sortByLeastRecentFirst(lifts)
            else -> sortByMostRecentFirst(lifts)
        }
    }

    fun sortByMostRecentFirst(lifts: List<Lift>?): List<Lift>? {
        return lifts?.sortedWith(compareByDescending { it.date })
    }

    fun sortByLeastRecentFirst(lifts: List<Lift>?): List<Lift>? {
        return lifts?.sortedWith(compareBy { it.date })
    }


    fun sortByLightestFirst(lifts: List<Lift>?): List<Lift>? {
        return lifts?.sortedWith(LightFirstWeightComparator())

        //return lifts?.sortedWith(compareBy { it.weight })
    }


    fun sortByHeaviestFirst(lifts: List<Lift>?): List<Lift>? {
        return lifts?.sortedWith(LightFirstWeightComparator())?.reversed()

        //return lifts?.sortedWith(compareByDescending { it.weight })
    }

    fun sortByAlphabetical(lifts: List<Lift>?): List<Lift>? {
        return lifts?.sortedWith(compareBy { it.exercise })
    }

    // Custom Comparator to correctly compare pounds and kilograms
    private inner class LightFirstWeightComparator : Comparator<Lift> {
        override fun compare(p0: Lift?, p1: Lift?): Int {
            var p0Weight = p0?.weight
            var p1Weight = p1?.weight
            if (p0Weight != null && p0?.weightType == WeightType.POUNDS) {
                p0Weight /= 2.204
            }

            if (p1Weight != null && p1?.weightType == WeightType.POUNDS) {
                p1Weight /= 2.204
            }

            return p1Weight?.let { p0Weight?.compareTo(it) }!!
        }
    }


}