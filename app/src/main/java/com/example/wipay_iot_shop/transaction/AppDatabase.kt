package com.example.testpos.database.transaction

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wipay_iot_shop.transaction.FlagReverseDao
import com.example.wipay_iot_shop.transaction.FlagReverseEntity

@Database(entities = arrayOf(SaleEntity::class,ReversalEntity::class, FlagReverseEntity::class),version = 1)
abstract class AppDatabase : RoomDatabase()
{
    abstract  fun reversalDao():ReversalDao
    abstract  fun saleDao():SaleDao
    abstract  fun flagReverseDao():FlagReverseDao

    companion object{
        @Volatile private var instance : AppDatabase? = null

        fun getAppDatabase(context : Context) : AppDatabase?
        {
            if(instance == null){

                synchronized(AppDatabase::class)
                {

                    if(instance == null)
                    {
                        instance = Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"transactionDB").build()
                    }

                }
            }

        return instance
        }
    }


}