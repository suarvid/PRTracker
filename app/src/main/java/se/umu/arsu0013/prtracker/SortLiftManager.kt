package se.umu.arsu0013.prtracker

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider

private const val TAG = "SortLiftListener"

class SortLiftManager {

    fun onItemSelected(
        context: Context,
        parent: AdapterView<*>?,
        position: Int,
        lifts: List<Lift>?
    ): List<Lift>? {
        //TODO: implement this properly for all possible sorting selections
        val selectedString = parent?.getItemAtPosition(position).toString()
        Log.d(TAG, selectedString)

        //TODO: This is a bit ugly, but might be ok since limited amount of options
        // Check that this works
        return when (selectedString) {
            context.getString(R.string.exercise_alphabetical) -> lifts?.sortedWith(compareBy { it.exercise })
            context.getString(R.string.heaviest_first) -> lifts?.sortedWith(compareBy { it.weight })
            context.getString(R.string.lightest_first) -> lifts?.sortedWith(compareByDescending { it.weight })
            context.getString(R.string.most_recent) -> lifts?.sortedWith(compareBy { it.date })
            context.getString(R.string.least_recent) -> lifts?.sortedWith(compareByDescending { it.date })
            else -> lifts
        }
    }

    fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}