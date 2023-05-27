package com.example.waru

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class Database {
    object DBContract {
        object Entry : BaseColumns {
            const val table_name1 = "table_name1"
//            const val table_name2 = "table_name2"
            //const val table_name3 = "comment_table"
            const val date = "date"
            const val text = "text"
            const val sentimentScore = "sentimentScore"
            const val sentimentMagnitude = "sentimentMagnitude"

            //const val t_id = "t_id"
            //const val c_id = "c_id"
        }
    }

    class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "myDBfile.db"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS ${DBContract.Entry.table_name1} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                        "${DBContract.Entry.date} TEXT," +
                        "${DBContract.Entry.text} TEXT," +
                        "${DBContract.Entry.sentimentScore} TEXT," +
                        "${DBContract.Entry.sentimentMagnitude} TEXT)"

//            val table_name2 = "CREATE TABLE ${DBContract.Entry.table_name1} (" +
//                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
//                    "${DBContract.Entry.date} TEXT," +
//                    "${DBContract.Entry.text} TEXT," +
//                    "${DBContract.Entry.sentimentScore} TEXT," +
//                    "${DBContract.Entry.sentimentMagnitude} TEXT)"
//                "CREATE TABLE ${DBContract.Entry.table_name2} (" +
//                        "${DBContract.Entry.texts} TEXT," +
//                        "${DBContract.Entry.t_id} INTEGER," +
//                        "FOREIGN KEY (${DBContract.Entry.t_id}) REFERENCES ${DBContract.Entry.table_name1}(${BaseColumns._ID}))"
//
//            val COMMENT_TABLE =
//                "CREATE TABLE ${DBContract.Entry.table_name3} (" +
//                        "${DBContract.Entry.comment} TEXT," +
//                        "${DBContract.Entry.c_id} INTEGER," +
//                        "FOREIGN KEY (${DBContract.Entry.c_id}) REFERENCES ${DBContract.Entry.table_name1}(${BaseColumns._ID}))"
//
            db.execSQL(SQL_CREATE_ENTRIES)
//            db.execSQL(table_name2)
//            db.execSQL(COMMENT_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${DBContract.Entry.table_name1}"
//            val table_name2 = "DROP TABLE IF EXISTS ${DBContract.Entry.table_name2}"

            db.execSQL(SQL_DELETE_ENTRIES)
//            db.execSQL(table_name2)
            onCreate(db)
        }



        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

    }
}