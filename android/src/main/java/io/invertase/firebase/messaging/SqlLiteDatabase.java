package io.invertase.firebase.messaging;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;

public class SqlLiteDatabase extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "contactsManager";
  private static final String TABLE_CONTACTS = "contacts";
  private static final String KEY_ID = "id";
  private static final String KEY_NAME = "name";
  private static final String KEY_PH_NO = "phone_number";

  public SqlLiteDatabase(@Nullable Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
      + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
      + KEY_PH_NO + " TEXT" + ")";
    db.execSQL(CREATE_CONTACTS_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Drop older table if existed
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

    // Create tables again
    onCreate(db);
  }

  public void addContact(Contact contacts) {
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(KEY_NAME, contacts.getName()); // Contact Name
    values.put(KEY_PH_NO, contacts.getPhoneNumber()); // Contact Phone

    // Inserting Row
    db.insert(TABLE_CONTACTS, null, values);
    //2nd argument is String containing nullColumnHack
    db.close(); // Closing database connection
  }

  public Contact getContact(String phoneNumber) {
    SQLiteDatabase db = this.getReadableDatabase();
    Contact contact = new Contact(0,"","");
    Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
        KEY_NAME, KEY_PH_NO }, KEY_PH_NO + "=?",
      new String[] { String.valueOf(phoneNumber) }, null, null, null, null);
    if (cursor != null)
      cursor.moveToFirst();
    while (cursor.moveToNext()){
      contact = new Contact(Integer.parseInt(cursor.getString(0)),
        cursor.getString(1), cursor.getString(2));
    }

    // return contact
    return contact;
  }

  public void updateContact (Contact contacts){
    SQLiteDatabase db = this.getReadableDatabase();
    ContentValues values = new ContentValues();

    values.put(KEY_NAME, contacts.getName()); // Contact Name
    values.put(KEY_PH_NO, contacts.getPhoneNumber()); // Contact Phone

    String selection = KEY_PH_NO + "=?";
    String[] selectionArgs = { contacts.getPhoneNumber() };

    db.update(TABLE_CONTACTS,values,selection,selectionArgs);
  }

  public List<Contact> getAllContacts() {
    List<Contact> contactList = new ArrayList<Contact>();
    // Select All Query
    String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        Contact contact = new Contact();
        contact.setID(Integer.parseInt(cursor.getString(0)));
        contact.setName(cursor.getString(1));
        contact.setPhoneNumber(cursor.getString(2));
        // Adding contact to list
        contactList.add(contact);
      } while (cursor.moveToNext());
    }

    // return contact list
    return contactList;
  }
}

