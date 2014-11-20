package com.br.gate;

import java.io.IOException;




import java.util.Date;

import com.br.gate.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class ActPrincipalGate extends Activity 
{
    
    IntentFilter[] mFilters;
    String[][] mTechLists; 
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    
    RadioButton rbEntry;
    RadioButton rbSlipper;
    DBGate Db;
    
    
    final Handler handler = new Handler() 
    {
        public void handleMessage(Message msg) 
        {

            // Get the current value of the variable total from the message data
            // and update the progress bar.
        	  TextView status = (TextView) findViewById(R.id.tvStatus);
        	  TextView Nome = (TextView) findViewById(R.id.tvNome);
	      	  int Operation = msg.getData().getInt("Operation");
	      	  
             
	      	  Guest guest=null;
	      	  guest=(Guest)msg.getData().getSerializable("guest");	    	  			
  			  if(guest!=null)
  				  Nome.setText(guest.getName());
  			  else
  				Nome.setText("");
	    	  switch(Operation)
	    	  {  	  				
	    	  		case Controller.OP_ENTRY:	    	  	
	    	  			status.setText("Bem vindo!");
	    	  			MediaPlayer.create(getApplicationContext(), R.raw.entry).start();

	    	  		break;
	    	  		case Controller.OP_SLIPPER:
	    	  			status.setText("Chinelo liberado!");
	    	  			MediaPlayer.create(getApplicationContext(), R.raw.entry).start();
		    	  	break;		    	  		
		            default:
		            	 // showDialog(1);
		                  status.setText(msg.getData().getString("Message"));
		                  MediaPlayer.create(getApplicationContext(), R.raw.erro).start();
		            break;	
	    	  }
	    	  
	    	  dismissDialog(0);
	    	  
        }
    };
    
    
    
    
    public static String byteArrayToHex(byte[] a) {
    	   StringBuilder sb = new StringBuilder(a.length * 2);
    	   for(byte b: a)
    	      sb.append(String.format("%02x", b & 0xff));
    	   return sb.toString();
    	}
    public static int bytearray2int(byte[] by)
    {
	    int value = 0;
	    for (int i = 0; i < by.length; i++)
	    {
	       value = (value << 8) + (by[i] & 0xff);
	    }
	    return value;
    }
//----------------------------------------------------------------------------------------------------------------------
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_gate);
        
        Db=new DBGate(this.getApplicationContext());
        mAdapter = NfcAdapter.getDefaultAdapter(this);                
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
         
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] {
                ndef,
        };

        // Setup a tech list for all NfcF tags
       mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };
        
        Intent intent = getIntent();
        
        resolveIntent(intent); 

        
       rbEntry = (RadioButton) findViewById(R.id.rbEntry);
   	   rbSlipper = (RadioButton) findViewById(R.id.rbSlipper);
       
    }
//----------------------------------------------------------------------------------------------------------------------
    private void resolveIntent(Intent intent) 
    {
        
      
            // 1) Parse the intent and get the action that triggered this intent
         String action = intent.getAction();
            // 2) Check if it was triggered by a tag discovered interruption.
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) 
         {
                    //  3) Get an instance of the TAG from the NfcAdapter
             Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
               // 4) Get an instance of the Mifare classic card from this TAG intent
            
             
                    
            try 
            {   
            	 
            	
                 
                 
                 System.out.println("Date in long: " + System.currentTimeMillis());
            	 if(rbEntry.isChecked())
            	 {	 	                              	               
	                 //controller = new Controller(MifareClassic.get(tagFromIntent), handler, Controller.OP_PERSON, guest);
            		  new Controller(MifareClassic.get(tagFromIntent), handler, Controller.OP_ENTRY, null).start();
            	 }
            	 else if(rbSlipper.isChecked())
            	 {            		 	                                  	                                  

            		 new Controller(MifareClassic.get(tagFromIntent), handler, Controller.OP_SLIPPER, null).start();            		 
            	 }
            	 
            	 showDialog(0);
            	 
            	 
            	 //
                   
            }
            catch(Exception e) 
            { 
                    //Log.e(TAG, e.getLocalizedMessage());
                    //showAlert(3);
            }
        } 
    }
 //---------------------------------------------------------------------------------------------------------------------   
    @Override
    protected Dialog onCreateDialog(int id) 
    {
        switch(id)
        {
            case 0:
                ProgressDialog progDialog = new ProgressDialog(this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setMessage("Loading...");
            return progDialog;
            case 1:
                AlertDialog.Builder tmp = new AlertDialog.Builder(getBaseContext());
                //tmp.setMessage(Saldo).show();
                return null;
            default:
                return null;
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    
        @Override
        public void onResume() 
        {
            super.onResume();
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
//----------------------------------------------------------------------------------------------------------------------
        @Override
        public void onNewIntent(Intent intent) 
        {
            Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
            resolveIntent(intent);            
        }
//----------------------------------------------------------------------------------------------------------------------
        @Override
        public void onPause()
        {
            super.onPause();
            mAdapter.disableForegroundDispatch(this);
        }
//----------------------------------------------------------------------------------------------------------------------        
        
    
    
}