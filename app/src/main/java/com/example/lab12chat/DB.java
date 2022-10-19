package com.example.lab12chat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

//393 Gavrilov
public class DB extends SQLiteOpenHelper{
    public DB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String empty = "";
        String sql = "CREATE TABLE Messages (number INT, date TEXT, name TEXT, message TEXT);";
        db.execSQL(sql);
        sql = "CREATE TABLE INFO (name TEXT, ip TEXT, portSend INT, portRecieve INT);";
        db.execSQL(sql);
        sql = "INSERT INTO INFO VALUES('" + "" + "', '" + "" + "', " + 0000 + ", " + 0000 + ");";
        db.execSQL(sql);
    }

    //Добавление сообщения в БД
    public void addMessage(int number, String date, String name, String message)
    {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "INSERT INTO Messages VALUES(" + number + ", '" + date + "', '" + name + "', '" + message + "');";

        try
        {
            db.execSQL(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Сохранение информации о настройках в БД
    public void saveInfo(String newName, String newIp, int newPortSend, int newPortRecieve)
    {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "UPDATE  INFO SET name = '" + newName + "', ip = '" + newIp+ "', portSend = " + newPortSend + ", portRecieve = " + newPortRecieve + ";";

        try
        {
            db.execSQL(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Получение информации о настройках из БД
    public void getInfo(String[] s)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT name, ip, portSend, portRecieve FROM INFO;";
        Cursor cur = db.rawQuery(sql, null);
        if (cur.moveToFirst() == true)
        {
            do {
                s[0] = cur.getString(0);
                s[1] = cur.getString(1);
                s[2] = cur.getString(2);
                s[3] = cur.getString(3);
            }while (cur.moveToNext() == true);
        }
    }

    //Получение всех сообщений из БД
    public void getAllMessages(ArrayList<String> lst)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT number, date, name, message FROM Messages;";
        Cursor cur = db.rawQuery(sql, null);
        if (cur.moveToFirst() == true)
        {
            do {
                String s;
                s = cur.getString(0);
                s += " ";
                s += cur.getString(1);
                s += " ";
                s += cur.getString(2);
                s += ": ";
                s += cur.getString(3);
                s += "\n";
                lst.add(s);
            }while (cur.moveToNext() == true);
        }
    }

    //Удаление всех сообщений из БД
    public void deleteAllMessages()
    {
        String sql = "DELETE FROM Messages;";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
