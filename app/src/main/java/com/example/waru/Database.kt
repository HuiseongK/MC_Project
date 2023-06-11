package com.example.waru

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class Database {
    object DBContract {
        object Entry : BaseColumns {
            const val TABLE_NAME = "myDBfile"
            const val table_name1 = "table_name1"
            const val table_name2 = "table_name2"
            const val table_name3 = "comment_table"

            const val date = "date"
            const val text = "text"
            const val color = "color"
            const val sentimentScore = "sentimentScore"
            const val sentimentMagnitude = "sentimentMagnitude"

            // 임시로 코멘트 저장하는 테이블 생성(테스트용)
            const val table_name4 = "comment_result"
            const val comment = "comment"
        }
    }

    class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "myDBfile.db"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val SQL_CREATE_ENTRIES1 = "CREATE TABLE IF NOT EXISTS ${DBContract.Entry.table_name1} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                        "${DBContract.Entry.date} TEXT," +
                        "${DBContract.Entry.text} TEXT," +
                        "${DBContract.Entry.sentimentScore} TEXT)"

            val SQL_CREATE_ENTRIES2 = "CREATE TABLE IF NOT EXISTS ${DBContract.Entry.table_name2} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${DBContract.Entry.date} TEXT," +
                    "${DBContract.Entry.text} TEXT," +
                    "${DBContract.Entry.sentimentScore} TEXT)"

            val SQL_CREATE_ENTRIES3 = "CREATE TABLE IF NOT EXISTS ${DBContract.Entry.table_name3} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${DBContract.Entry.date} TEXT," +
                    "${DBContract.Entry.color} TEXT)"

            val SQL_CREATE_ENTRIES4 = "CREATE TABLE IF NOT EXISTS ${DBContract.Entry.table_name4} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${DBContract.Entry.date} TEXT," +
                    "${DBContract.Entry.comment} TEXT)"

            db.execSQL(SQL_CREATE_ENTRIES1)
            db.execSQL(SQL_CREATE_ENTRIES2)
            db.execSQL(SQL_CREATE_ENTRIES3)
            db.execSQL(SQL_CREATE_ENTRIES4)

        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${DBContract.Entry.table_name1}"
            val SQL_DELETE_ENTRIES2 = "DROP TABLE IF EXISTS ${DBContract.Entry.table_name2}"
            val SQL_DELETE_ENTRIES3 = "DROP TABLE IF EXISTS ${DBContract.Entry.table_name3}"
            val SQL_DELETE_ENTRIES4 = "DROP TABLE IF EXISTS ${DBContract.Entry.table_name4}"

            db.execSQL(SQL_DELETE_ENTRIES)
            db.execSQL(SQL_DELETE_ENTRIES2)
            db.execSQL(SQL_DELETE_ENTRIES3)
            db.execSQL(SQL_DELETE_ENTRIES4)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

    }
}