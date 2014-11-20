package com.br.gate;

import java.io.Serializable;
import java.sql.Date;


//String create_tb_guests= "CREATE TABLE guests ( "  
//			 + "id                INTEGER         PRIMARY KEY AUTOINCREMENT   NOT NULL,"
//			 + "uid               INTEGER         NOT NULL UNIQUE,"
//		 + "name              VARCHAR( 255 ),"
//		 + "dh_entry          DATETIME        DEFAULT ( NULL ),"
//		 + "multiple_entries  BOOLEAN         NOT NULL DEFAULT ( 0 ),"
//		 + "dh_slipper        DATETIME        DEFAULT ( NULL ),"
//		 + "multiple_slippers BOOLEAN         NOT NULL DEFAULT ( 0 ) );";

public class Guest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;
	int uid=0;
	String Name="";
	long DHEntry;
	boolean MultipleEntries=false;
	long DHSlipper;
	boolean MultipleSlippers=false;
	
	Guest(){
		
	}
	
	public String toString(){
		return "ID: " + id + " uid: " + uid + 
			   " Name: " + Name + " DHEntry: " + DHEntry + 
			   " Multiple Entries: " + MultipleEntries + 
			   " DHSlipper: " + DHSlipper + " Multiple Slippers: " + MultipleSlippers;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public long getDHEntry() {
		return DHEntry;
	}
	public void setDHEntry(long dHEntry) {
		DHEntry = dHEntry;
	}
	public boolean isMultipleEntries() {
		return MultipleEntries;
	}
	public void setMultipleEntries(boolean multipleEntries) {
		MultipleEntries = multipleEntries;
	}
	public long getDHSlipper() {
		return DHSlipper;
	}
	public void setDHSlipper(long dHSlipper) {
		DHSlipper = dHSlipper;
	}
	public boolean isMultipleSlippers() {
		return MultipleSlippers;
	}
	public void setMultipleSlippers(boolean multipleSlippers) {
		MultipleSlippers = multipleSlippers;
	}

	

}
