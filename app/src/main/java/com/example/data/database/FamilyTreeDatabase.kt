package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FamilyTreeDao
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        TreeEntity::class,
        MemberEntity::class,
        RelationshipEntity::class,
        StoryEntity::class,
        DocumentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FamilyTreeDatabase : RoomDatabase() {
    abstract fun familyTreeDao(): FamilyTreeDao

    companion object {
        @Volatile
        private var INSTANCE: FamilyTreeDatabase? = null

        fun getDatabase(context: Context): FamilyTreeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamilyTreeDatabase::class.java,
                    "family_tree_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
