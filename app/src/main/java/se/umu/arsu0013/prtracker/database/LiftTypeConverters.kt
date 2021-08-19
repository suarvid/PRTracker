package se.umu.arsu0013.prtracker.database

import androidx.room.TypeConverter
import se.umu.arsu0013.prtracker.WeightType
import java.lang.IllegalArgumentException
import java.util.*

class LiftTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisecondsSinceEpoch: Long?): Date? {
        return millisecondsSinceEpoch?.let { milliseconds ->
            Date(milliseconds)
        }
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toWeightType(type: String?): WeightType {
        return when (type) {
            "KILOGRAMS" ->  WeightType.KILOGRAMS
            "POUNDS" ->  WeightType.POUNDS
            else -> throw IllegalArgumentException("Invalid Weight Type")
        }
    }

    @TypeConverter
    fun fromWeightType(weightType: WeightType?): String {
        return weightType.toString()
    }


}