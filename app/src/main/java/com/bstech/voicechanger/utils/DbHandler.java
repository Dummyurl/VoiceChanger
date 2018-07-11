package com.bstech.voicechanger.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bstech.voicechanger.model.Record;

public class DbHandler extends SQLiteOpenHelper {


    private static final String FILE_DIR = Statistic.FOLDER_STORE + File.separator + ".db";
    private static final String NAME_DB = "BVoiceChanger";
    private static final int VERSION = 2;
    private static final String TBL_RECORD = "Record";
    private static final String CL_ID = "id";
    private static final String CL_TITLE = "Title";
    private static final String CL_FILE_PATH = "FilePath";
    private static final String CL_SIZE = "Size";
    private static final String CL_DATE_TIME = "DateTime";
    private static final String CL_DURATION = "Duration";
    private static final String CL_DOCUMENT_TREE = "DocumentTree" +
            "";
    private static final String TEXT = " TEXT ;";

    private static final String CREATE_TBL_RECORD = "CREATE TABLE IF NOT EXISTS  " + TBL_RECORD + " ("
            + CL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CL_TITLE + " TEXT, "
            + CL_FILE_PATH + " TEXT, "
            + CL_DURATION + " LONG, "
            + CL_SIZE + " TEXT, "
            + CL_DATE_TIME + " LONG, "
            + CL_DOCUMENT_TREE + " TEXT "
            + ")";
    private static DbHandler _instance = null;

    private Context mContext;

    public DbHandler(Context context) {
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + FILE_DIR
                + File.separator + NAME_DB, null, VERSION);
        mContext = context;
    }

    public static synchronized DbHandler getInstance(Context context) {
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + FILE_DIR);
        if (!dir.exists())
            dir.mkdirs();

        if (_instance == null) {
            //Flog.d("_instance == null");
            _instance = new DbHandler(context.getApplicationContext());
        }

        return _instance;
    }

    public void addRecord(String nameRecord, String filePath, long duration, String size, String document) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CL_TITLE, nameRecord);
        cv.put(CL_FILE_PATH, filePath);
        cv.put(CL_DURATION, duration);
        cv.put(CL_SIZE, size);
        cv.put(CL_DATE_TIME, System.currentTimeMillis());
        cv.put(CL_DOCUMENT_TREE, document);

        db.insert(TBL_RECORD, null, cv);
        db.close();
    }

    public List<Record> getRecords() {
        List<Record> records = new ArrayList<>();
        String mSelect = "SELECT * FROM " + TBL_RECORD;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery(mSelect, null);
        if (mCursor.moveToFirst()) {
            do {
                Record record = new Record();
                record.setId(mCursor.getInt(0));
                record.setTitle(mCursor.getString(1));
                record.setFilePath(mCursor.getString(2));
                record.setDuration(mCursor.getLong(3));
                record.setSize(mCursor.getString(4));
                record.setDateTime(mCursor.getLong(5));
                record.setUriFile(mCursor.getString(6));


                if (record.getUriFile() != null) {
                    if (!record.getUriFile().equals("")) {

                        DocumentFile file_uri = DocumentFile.fromTreeUri(mContext, Uri.parse(record.getUriFile()));
                        DocumentFile f = file_uri.findFile(new File(record.getFilePath()).getName());
                        Log.d("lynah", record.getTitle() + " - " + record.getFilePath() + " - " + record.getUriFile());

                        if (f == null) {

                            File file = new File(record.getFilePath());
                            if (!file.exists()) {
                                Log.d("lynah", "file not exist other" + record.getTitle());
                                deleteRecord(record.getId());
                            } else {

                                records.add(record);

                            }

                        } else {
                            if (!f.exists()) {
                                Log.d("lynah", "file not exist" + record.getTitle());
                                deleteRecord(record.getId());
                            } else {
                                Log.d("lynah", "add " + record.getTitle());

                                records.add(record);

                            }
                        }
                    } else {
                        File file = new File(record.getFilePath());
                        if (!file.exists()) {
                            Log.d("lynah", "file not exist other" + record.getTitle());
                            deleteRecord(record.getId());
                        } else {

                            records.add(record);

                        }
                    }
                } else {
                    File file = new File(record.getFilePath());
                    if (!file.exists()) {
                        deleteRecord(record.getId());
                    } else {
                        records.add(record);
                    }
                }

            } while (mCursor.moveToNext());
        }
        mCursor.close();
        db.close();
        return records;
    }


    public void addRecordUpdate(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CL_ID, record.getId());
        cv.put(CL_TITLE, record.getTitle());
        cv.put(CL_FILE_PATH, record.getFilePath());
        cv.put(CL_DURATION, record.getDuration());
        cv.put(CL_SIZE, record.getSize());
        cv.put(CL_DATE_TIME, record.getDateTime());

        db.insert(TBL_RECORD, null, cv);
        db.close();
    }

    public void deleteRecord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_RECORD, CL_ID + " = " + id + "", null);
    }

    public void updateTitle(String titleRecord, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CL_TITLE, titleRecord);
        db.update(TBL_RECORD, cv, CL_ID + " = " + id, null);
    }

    public void updateFilePath(String fileRecord, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CL_FILE_PATH, fileRecord);
        db.update(TBL_RECORD, cv, CL_ID + " = " + id, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TBL_RECORD);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
