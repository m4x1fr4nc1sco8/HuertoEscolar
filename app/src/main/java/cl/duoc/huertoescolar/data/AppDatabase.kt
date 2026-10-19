package cl.duoc.huertoescolar.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import cl.duoc.huertoescolar.model.Cultivo
import cl.duoc.huertoescolar.model.Sector
import cl.duoc.huertoescolar.model.Usuario

@Database(entities = [Usuario::class, Sector::class, Cultivo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}