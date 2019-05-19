package com.hikki.sergey_natalenko.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hikki.sergey_natalenko.todolist.database.NoteBaseHelper;
import com.hikki.sergey_natalenko.todolist.database.NoteCursorWrapper;
import com.hikki.sergey_natalenko.todolist.database.NoteDbSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.hikki.sergey_natalenko.todolist.database.NoteDbSchema.*;

public class NoteLab {
    private static NoteLab sNoteLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static NoteLab get(Context context){
        if (sNoteLab == null){
            sNoteLab = new NoteLab(context);
        }
        return sNoteLab;
    }
    private NoteLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new NoteBaseHelper(mContext)
                .getWritableDatabase();
    }

    public void addNote(Note n){
        ContentValues values = getContentValues(n);
        mDatabase.insert(NoteTable.NAME, null, values);
    }

    public void removeNote(Note n){
        String id = n.getId().toString();
        mDatabase.delete(NoteTable.NAME, NoteTable.Cols.UUID + " = ?",new String[] { id });
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();

        NoteCursorWrapper cursor = queryNotes(null, null);

        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                notes.add(cursor.getNote());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return notes;
    }

    public Note getNote(UUID id){
        NoteCursorWrapper cursor = queryNotes(
                NoteTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );
        try {
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getNote();
        } finally {
            cursor.close();
        }
    }

    public File getPhotoFile(Note note){
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, note.getPhotoFilename());
    }

    public void updateNote(Note note){
        String uuidString = note.getId().toString();
        ContentValues values = getContentValues(note);

        mDatabase.update(NoteTable.NAME, values,
                NoteTable.Cols.UUID + " = ?",
                new String[] { uuidString});
    }

    private NoteCursorWrapper queryNotes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                NoteTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new NoteCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Note note){
        ContentValues values = new ContentValues();

        values.put(NoteTable.Cols.UUID, note.getId().toString());
        values.put(NoteTable.Cols.TITLE, note.getTitle());
        values.put(NoteTable.Cols.CONTENT, note.getContent());
        values.put(NoteTable.Cols.DATE, note.getDate().getTime());
        values.put(NoteTable.Cols.SOLVED, note.isSolved()? 1 : 0);
        values.put(NoteTable.Cols.URGENT, note.isUrgent()? 1 : 0);

        return values;
    }
}
