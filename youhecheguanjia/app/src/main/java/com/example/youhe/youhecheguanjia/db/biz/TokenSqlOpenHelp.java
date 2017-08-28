package com.example.youhe.youhecheguanjia.db.biz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/9/19 0019.
 */
public class TokenSqlOpenHelp extends SQLiteOpenHelper {
    private static String NAME = "token.db";
    private static int VERSION = 1;

    public TokenSqlOpenHelp(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql = "create table tokens( eid integer primary key autoincrement not null,token varchar(100) not null);";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
