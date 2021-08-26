package se.umu.arsu0013.prtracker

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// The location can be a string for now, use position from something like Geocoding or a map
// if there is time.
@Entity
data class Lift(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var exercise: String = "",
    var weight: Int = 0,
    var weightType: WeightType = WeightType.KILOGRAMS,
    var date: Date = Date(),
    var location: String = "",
    var description: String = "",
    var videoPath: Uri = Uri.parse(""))


enum class WeightType {
    KILOGRAMS,
    POUNDS
}