package se.umu.arsu0013.prtracker

import java.lang.IllegalArgumentException
import java.util.*

private const val TAG = "DateTextFormatter"
class DateTextFormatter {

    companion object {
        fun format(date: Date): String {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val monthText = when (month.toString().length) {
                1 -> "0${month+1}"
                2 -> "${month+1}"
                else -> {
                    throw IllegalArgumentException("Invalid Month Value")
                }
            }

            return "${day}/${monthText}/${year}"
        }
    }


}