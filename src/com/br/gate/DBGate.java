package com.br.gate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBGate extends SQLiteOpenHelper {	
	  private static final String DATABASE_NAME = "/mnt/sdcard/gate.db";
	  private static final int DATABASE_VERSION = 2;
	
	  	
	  public DBGate(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }
	
	
	  @Override
	  public void onCreate(SQLiteDatabase db) {
		  String create_tb_guests= "CREATE TABLE guests ( "  
	  				 + "id                INTEGER         PRIMARY KEY AUTOINCREMENT   NOT NULL,"
	  				 + "uid               INTEGER         NOT NULL UNIQUE,"
					 + "name              VARCHAR( 255 ),"
					 + "dh_entry          INTEGER(8)         DEFAULT ( 0 ),"
					 + "multiple_entries  BOOLEAN         NOT NULL DEFAULT ( 0 ),"
					 + "dh_slipper        INTEGER(8)        DEFAULT ( 0 ),"
					 + "multiple_slippers BOOLEAN         NOT NULL DEFAULT ( 0 ) );";
          db.execSQL(create_tb_guests);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	   	  Log.w(DBGate.class.getName(),
		          "Upgrading database from version " + oldVersion + " to "
		              + newVersion + ", which will destroy all old data");
	      db.execSQL("DROP TABLE IF EXISTS  guests ");
	      onCreate(db);
		
	  }


}
