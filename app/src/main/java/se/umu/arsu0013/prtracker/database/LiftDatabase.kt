package se.umu.arsu0013.prtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import se.umu.arsu0013.prtracker.Lift

@Database(entities = [ Lift::class ], version = 2)
@TypeConverters(LiftTypeConverters::class)
abstract class LiftDatabase : RoomDatabase() {
    abstract fun liftDao(): LiftDao
}

// added type of weight unit in second version of database
// could probably have skipped the migration as the app has zero users,
// but was a bit fun to try
val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Lift ADD COLUMN weightType TEXT NOT NULL DEFAULT 'KILOGRAMS'"
        )
    }
}