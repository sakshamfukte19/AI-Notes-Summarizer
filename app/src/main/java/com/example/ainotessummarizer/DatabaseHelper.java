package com.example.ainotessummarizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SecureUserDB.db";
    private static final int DATABASE_VERSION = 6;

    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password_hash";
    public static final String COL_IS_LOGGED_IN = "is_logged_in";
    public static final String COL_IS_DELETED = "is_deleted";
    public static final String COL_FAILED_ATTEMPTS = "failed_attempts";
    public static final String COL_LOCK_TIME = "lock_time";
    public static final String COL_LAST_ACTIVE = "last_active";
    public static final String COL_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                        COL_PASSWORD + " TEXT NOT NULL, " +
                        COL_IS_LOGGED_IN + " INTEGER DEFAULT 0, " +
                        COL_IS_DELETED + " INTEGER DEFAULT 0, " +
                        COL_FAILED_ATTEMPTS + " INTEGER DEFAULT 0, " +
                        COL_LOCK_TIME + " INTEGER DEFAULT 0, " +
                        COL_LAST_ACTIVE + " INTEGER DEFAULT 0, " +
                        COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ---------------- PASSWORD HASHING ----------------

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash error");
        }
    }

    // ---------------- REGISTER ----------------

    public boolean registerUser(String email, String password) {
        if (isEmailExists(email)) return false;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EMAIL, email);
        cv.put(COL_PASSWORD, hashPassword(password));
        return db.insert(TABLE_USERS, null, cv) != -1;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS +
                        " WHERE " + COL_EMAIL + "=? AND " + COL_IS_DELETED + "=0",
                new String[]{email}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // ---------------- LOGIN (ONLY PASSWORD CHECK) ----------------

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_PASSWORD +
                        " FROM " + TABLE_USERS +
                        " WHERE " + COL_EMAIL + "=? AND " + COL_IS_DELETED + "=0",
                new String[]{email}
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }

        String storedHash = cursor.getString(0);
        cursor.close();

        return storedHash.equals(hashPassword(password));
    }

    // ---------------- SESSION ----------------

    public void logoutAllUsers(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_LOGGED_IN, 0);
        db.update(TABLE_USERS, cv, null, null);
    }

    public void logoutUser(String email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_LOGGED_IN, 0);
        db.update(TABLE_USERS, cv, COL_EMAIL + "=?", new String[]{email});
    }

    public User getLoggedInUser() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + "," + COL_EMAIL + "," + COL_CREATED_AT +
                        " FROM " + TABLE_USERS +
                        " WHERE " + COL_IS_LOGGED_IN + "=1 LIMIT 1",
                null
        );

        if (cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            );
            cursor.close();
            return user;
        }

        cursor.close();
        return null;
    }

    public void checkSessionTimeout(long timeoutMillis) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_EMAIL + "," + COL_LAST_ACTIVE +
                        " FROM " + TABLE_USERS +
                        " WHERE " + COL_IS_LOGGED_IN + "=1",
                null
        );

        if (cursor.moveToFirst()) {
            long lastActive = cursor.getLong(1);
            if (System.currentTimeMillis() - lastActive > timeoutMillis) {
                logoutUser(cursor.getString(0));
            }
        }
        cursor.close();
    }
}
