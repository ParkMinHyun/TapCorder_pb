package com.example.osm.appdesign21.DB_Excel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SpotsDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_SPOT_NAME="_spotName";
    public static final String KEY_SPOT_ADDRESS="_spotAddr";
    public static final String KEY_SPOT_TELNUM="_spotTelNum";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private static final String DATABASE_CREATE = "create table pol (_id integer primary key autoincrement, "
            + "_spotName text, _spotAddr text, _spotTelNum text);";

    private static final String DATABASE_NAME = "pol_Station";
    private static final String DATABASE_TABLE = "pol";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        //버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS pol");
            onCreate(db);
        }

        //최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }
    }

    public SpotsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public SpotsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    //레코드 생성
    public long createSpot(String id, String name, String addr, String tel_num) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_SPOT_NAME, name);
        initialValues.put(KEY_SPOT_ADDRESS, addr);
        initialValues.put(KEY_SPOT_TELNUM, tel_num);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    //모든 레코드 반환
    public Cursor fetchAllSpots() {
        return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_SPOT_NAME, KEY_SPOT_ADDRESS, KEY_SPOT_TELNUM
        }, null, null, null, null, null);
    }

    //특정 레코드 반환-NO USE
    public Cursor fetchSpot(long rowId) throws SQLException {

        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_SPOT_NAME, KEY_SPOT_ADDRESS, KEY_SPOT_TELNUM
        }, KEY_ROWID
                + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


}