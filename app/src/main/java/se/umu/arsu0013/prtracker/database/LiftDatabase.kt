package se.umu.arsu0013.prtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import se.umu.arsu0013.prtracker.Lift

@Database(entities = [ Lift::class ], version = 3)
@TypeConverters(LiftTypeConverters::class)
abstract class LiftDatabase : RoomDatabase() {
    abstract fun liftDao(): LiftDao
}