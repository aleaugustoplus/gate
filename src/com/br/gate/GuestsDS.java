package com.br.gate;

import java.sql.Date;
import java.util.logging.Logger;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class GuestsDS {
	  // Database fields
	  private SQLiteDatabase database;
	  private DBGate dbGate;

	  public GuestsDS(Context context) {
	      dbGate = new DBGate(context);
	  }
	  

	  public void open(){
	    database = dbGate.getWritableDatabase();
	  }

	  public void close() {
	    dbGate.close();
	  }

	  
	  public void InsertGuest(Guest guest){
		  this.open();
		  
		  
		  String insert=" INSERT into guests (uid, name, dh_entry, multiple_entries, dh_slipper, multiple_slippers ) VALUES ( ";
		  insert+= " " + guest.getUid() + ", '" + guest.getName() + "', " + guest.DHEntry + ", ";
		  if(guest.isMultipleEntries())
			  insert+="1, ";
		  else	  
			  insert+="0, ";
		  insert+=guest.getDHSlipper() + ", ";
		  if(guest.isMultipleSlippers())
			  insert+="1); ";
		  else	  
			  insert+="0); ";
		  
		  System.out.println(insert);
		  database.execSQL(insert);		  
		  this.close();
		  		  
	  }
	  public void InsertGuestIfNot(Guest guest){
		  if(GetGuestByUid(guest.getUid())==null)
				InsertGuest(guest);  
		  		  
	  }	  	  
	  public Guest GetGuestByUid(int Uid){
		  this.open();
		  
		  String select=" SELECT * FROM guests WHERE uid=" + Uid + " ;";
		  
		  Cursor cursor = database.rawQuery(select, null);
		  if(!cursor.moveToFirst())
			  return null;
		  Guest guest = Cursor2Guest(cursor);
		  cursor.close();
		  this.close();
		  return guest;
		  
	  }
	  Guest Cursor2Guest(Cursor cursor){
		  Guest guest = new Guest();
		  guest.setId(cursor.getInt(0));
		  guest.setUid(cursor.getInt(1));
		  guest.setName(cursor.getString(2));
		  
		  if(!cursor.isNull(3))
			  guest.setDHEntry(cursor.getLong(3));
		  else
			  guest.setDHEntry(0);
		  
		  
		  if(!cursor.isNull(5))
			  guest.setDHSlipper(cursor.getLong(5));
		  else
			  guest.setDHSlipper(0);
		  
		  
		//guest.setMultipleEntries(Boolean.valueOf(cursor.getInt(4)));
		  
		  return guest;
		  
	  }
	  
}
