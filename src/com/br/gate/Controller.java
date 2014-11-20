package com.br.gate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * @author alexandre
 *
 */
 public class Controller  extends Thread
{
    MifareClassic mfc;
    Handler handle;
    Guest guest;
    
    
    byte []KeyA={(byte)0xAA, (byte)0xBB, (byte)0xFF, 0x01, 0x02, (byte)0xFF};
    byte []KeyB={(byte)0xAA, (byte)0xBB, (byte)0xFF, 0x01, 0x02, (byte)0xFE};
    //78778869
    byte []AccessCondition={(byte)0x78, (byte)0x77, (byte)0x88, 0x69};
    byte []IdCard={(byte)0xBB, (byte)0xAA, (byte)0xEE, (byte)0x12};

    public static final int OP_READ=1;
    public static final int OP_CLEAR=2;
    public static final int OP_PERSON=3;
    public static final int OP_ENTRY=4;
    public static final int OP_SLIPPER=5;
    public static final int OP_FAIL=6;
    
    int Operation;
    
    
//----------------------------------------------------------------------------------------------------------------------    
   public Controller(MifareClassic mfc, Handler handle, int Operation, Guest guest)
   {
       this.mfc=mfc;
       this.handle = handle;
       this.Operation=Operation;
       this.guest = guest;
       System.out.println("Guest received: " + guest);
   }
//----------------------------------------------------------------------------------------------------------------------
   @Override
   public void run()
   {
      System.out.println("Iniciou");   
      try 
      {
          mfc.connect();
          mfc.setTimeout(30000);          
          switch(Operation)
          {
          	case OP_READ:
          		this.Read();
          	break;
          	case OP_PERSON:
          		this.Person();
            break;
          	case OP_CLEAR:
          		this.Clear();
            break;
          	case OP_ENTRY:
          		this.Entry();
            break;
          	case OP_SLIPPER:
          		this.Slipper();
            break;              	  	  	  	  	
          }
          
          mfc.close();
          //getcarduid
          //mfc.getTag().getId()
          
          Message data = new Message();          
          Bundle b= new Bundle();                    
          b.putInt("Operation", Operation);                                     
          b.putByteArray("Uid", mfc.getTag().getId());      
          b.putSerializable("guest", guest);
          data.setData(b);
          
          handle.sendMessage(data);
          
          
          
      } 
      catch (Exception e) 
      {
    	  Message data = new Message();          
          Bundle b= new Bundle();
          b.putInt("Operation", OP_FAIL);
          b.putString("Message", e.getMessage());
          b.putByteArray("Uid", mfc.getTag().getId());      
          b.putSerializable("guest", guest);
          data.setData(b);
          handle.sendMessage(data);
          e.printStackTrace();
      }  
      

      
   }
 //----------------------------------------------------------------------------------------------------------------------
   private void Read() throws Exception
   {
	   byte [][]Sector;
	   this.guest = new Guest();
	   byte []Name=new byte[48];
	   
	   CheckSector0();	   
	   Sector=this.ReadSector(1, KeyA);
	   
	   int Size=0;
	   
	   LOOPX1:
	   for(int x=0;x<3;x++)
		   for(int y=0;y<16;y++)
		   {
			   if(Sector[x][y]==(byte)0xEE)
				   break LOOPX1;
			   else
				   Name[Size++]=Sector[x][y];
		   }
		   
	   byte []Tmp=new byte[Size];
	   
	   System.arraycopy(Name, 0, Tmp, 0, Size);
	   
	   this.guest.setName(new String(Tmp));
	   
	   Sector=this.ReadSector(3, KeyA);
		
	   byte[] Tmp2=new byte[8];
	   System.arraycopy(Sector[0], 0, Tmp2, 0, 8);
	   
	   guest.setDHEntry(ByteArrayToLong(Tmp2));

	   System.arraycopy(Sector[0], 8, Tmp2, 0, 8);	   
	   guest.setDHSlipper(ByteArrayToLong(Tmp2));
	   
	   if(Sector[1][0]==(byte)0xFF)
		   guest.setMultipleEntries(true);
	   else
		   guest.setMultipleEntries(false);
	   
	   if(Sector[1][1]==(byte)0xFF)
		   guest.setMultipleSlippers(true);
	   else
		   guest.setMultipleSlippers(false);
	   
	   System.out.println("Guest: " + guest);
   }      
//----------------------------------------------------------------------------------------------------------------------
   private void Person() throws IOException
   {
	   byte [][]Bytes;
	   byte []Key={(byte) 0xFF,(byte) 0xFF, (byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF};
	   byte []Tmp;
	   Bytes = new byte[4][16];
	   
	   
	   
	   for(int x=0;x<16;x++)
	   {
		   switch(x)
		   {
		   		case 0:		   			
		   			System.arraycopy(IdCard, 0, Bytes[1], 0, IdCard.length);
		   			for(int y=0;y<16;y++)		   
		 			   Bytes[2][y]=(byte)0xAA;
		   		break;
		   		case 1:
		   			
		   			byte[]Name=guest.getName().toString().getBytes();
		   			
		   			//Sector Name
		   			for(int y=0;y<16;y++)
		   				if(Name.length>y)
			 			   Bytes[0][y]=Name[y];
		   				else
			 			   Bytes[0][y]=(byte)0xEE;
		   			
		   			for(int y=0;y<16;y++)
		   				if(Name.length>y+16)
			 			   Bytes[1][y]=Name[y+16];
		   				else
			 			   Bytes[1][y]=(byte)0xEE;

		   			for(int y=0;y<16;y++)
		   				if(Name.length>y+32)
			 			   Bytes[2][y]=Name[y+32];
		   				else
			 			   Bytes[2][y]=(byte)0xEE;
		   			
		   		break;	
		   		case 3:			   					   					   			
		   			//Date and time entry and date time slipper
		   			for(int y=0;y<16;y++)		   
			 			   Bytes[0][y]=(byte)0x00;
		   			long value = 0;
		   			Tmp = longToBytes(guest.getDHEntry());
		   			
		   			System.arraycopy(Tmp, 0, Bytes[0], 0, 8);
		   			
		   			Tmp = longToBytes(guest.getDHSlipper());		   			
		   			System.arraycopy(Tmp, 0, Bytes[0], 8, 8);
		   			System.out.println("Guest recorded:" + guest);
		   			if(guest.isMultipleEntries())
		   				Bytes[1][0]=(byte)0xFF;
		   			else
		   				Bytes[1][0]=(byte)0x00;
		   			
		   			if(guest.isMultipleSlippers())
		   				Bytes[1][1]=(byte)0xFF;
		   			else
		   				Bytes[1][1]=(byte)0x00;
		   			
		   			//Multiple Entries and multiple slippers 
		   			for(int y=0;y<16;y++)		   
			 			   Bytes[2][y]=(byte)0x00;
		   			
		   			
	   			break;	

		   		default:
		   			DefaultPerson(Bytes);
		   		break;	
		   }
		   
   		   System.arraycopy(KeyA, 0, Bytes[3], 0, 6);
		   System.arraycopy(AccessCondition, 0, Bytes[3], 6, 4);
  		   System.arraycopy(KeyB, 0, Bytes[3], 10, 6);
		   
		   WriteSector(x, Bytes, Key);
	   }
   }
   
 //----------------------------------------------------------------------------------------------------------------------   
   private void DefaultPerson(byte [][]Bytes)
   {
	   for(int x=0;x<3;x++)
	   {
		   for(int y=0;y<16;y++)		   
			   Bytes[x][y]=(byte)0xAA;					   		   			   		 
	   }	   	   
   }

//----------------------------------------------------------------------------------------------------------------------
   private void Clear() throws Exception
   {
	   byte [][]Bytes;
	   byte []Key={(byte) 0xFF,(byte) 0xFF, (byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF};
	   byte []Tmp;
	   Bytes = new byte[4][16];
	   
	   
	   CheckSector0();
	   
	   for(int x=1;x<5;x++)
	   {
		   switch(x)
		   {
		
		   		case 1:
		   			
		   			byte[]Name=guest.getName().toString().getBytes();
		   			
		   			//Sector Name
		   			for(int y=0;y<16;y++)
		   				if(Name.length>y)
			 			   Bytes[0][y]=Name[y];
		   				else
			 			   Bytes[0][y]=(byte)0xEE;
		   			
		   			for(int y=0;y<16;y++)
		   				if(Name.length>y+16)
			 			   Bytes[1][y]=Name[y+16];
		   				else
			 			   Bytes[1][y]=(byte)0xEE;

		   			for(int y=0;y<16;y++)
		   				if(Name.length>y+32)
			 			   Bytes[2][y]=Name[y+32];
		   				else
			 			   Bytes[2][y]=(byte)0xEE;
		   			
		   		break;	
		   		case 3:			   					   					   			
		   			//Date and time entry and date time slipper
		   			for(int y=0;y<16;y++)		   
			 			   Bytes[0][y]=(byte)0x00;
		   			long value = 0;
		   			Tmp = longToBytes(guest.getDHEntry());
		   			System.out.println("Date entry recorded in card:" +guest.getDHEntry());
		   			System.arraycopy(Tmp, 0, Bytes[0], 0, 8);
		   			
		   			Tmp = longToBytes(guest.getDHSlipper());		   			
		   			System.arraycopy(Tmp, 0, Bytes[0], 8, 8);
		   			
		   			if(guest.isMultipleEntries())
		   				Bytes[1][0]=(byte)0xFF;
		   			else
		   				Bytes[1][0]=(byte)0x00;
		   			
		   			if(guest.isMultipleSlippers())
		   				Bytes[1][1]=(byte)0xFF;
		   			else
		   				Bytes[1][1]=(byte)0x00;
		   			
		   			//Multiple Entries and multiple slippers 
		   			for(int y=0;y<16;y++)		   
			 			   Bytes[2][y]=(byte)0x00;
		   			
		   			
	   			break;	
		   		case 2:
		   		continue;	

		   		default:
		   			DefaultPerson(Bytes);
		   		break;	
		   }
		   
   		   System.arraycopy(KeyA, 0, Bytes[3], 0, 6);
		   System.arraycopy(AccessCondition, 0, Bytes[3], 6, 4);
  		   System.arraycopy(KeyB, 0, Bytes[3], 10, 6);
		   
		   //WriteSector(x, Bytes, Key);
		   WriteSectorWithoutAccessBits(x, Bytes, KeyB);
	   }
	   
   }
//----------------------------------------------------------------------------------------------------------------------
   private void Entry() throws Exception
   {
	   Read();
	   
	   if(guest.getDHEntry()!=0 && !guest.isMultipleEntries())
		   throw new Exception("Entrada já efetuada!");	  	   	   
	   
	   guest.setDHEntry(System.currentTimeMillis());
	   
	   Clear();
   }
   
//----------------------------------------------------------------------------------------------------------------------
   private void Slipper() throws Exception
   {
	   Read();
	   
	   if(guest.getDHSlipper()!=0 && !guest.isMultipleSlippers())
		   throw new Exception("Já foi entregue!");
		
	   guest.setDHSlipper(System.currentTimeMillis());
	   
	   Clear();
   }            
//----------------------------------------------------------------------------------------------------------------------
   private void WriteSector(int Sector, byte [][]Bytes, byte[] Key) throws IOException
   {
	   mfc.authenticateSectorWithKeyA(Sector, Key);
	   for(int x=0;x<4;x++)
	   {
		   if(Sector==0 && x==0)
			   continue;
		   
		   mfc.writeBlock((Sector*4) + x, Bytes[x]);
	   }
	   
   }
   //----------------------------------------------------------------------------------------------------------------------
   private void WriteBlock(int Sector, int block, byte []Bytes, byte[] Key) throws IOException
   {
	   mfc.authenticateSectorWithKeyB(Sector, Key);
       if(Sector==0 && block==0)
	 	   return;
	   
	    mfc.writeBlock((Sector*4) + block, Bytes);
   
	   
   }
 //----------------------------------------------------------------------------------------------------------------------
   private void WriteSectorWithoutAccessBits(int Sector, byte [][]Bytes, byte[] Key) throws IOException
   {
	   mfc.authenticateSectorWithKeyB(Sector, Key);
	   for(int x=0;x<3;x++)
	   {
		   if(Sector==0 && x==0)
			   continue;
		   
		   mfc.writeBlock((Sector*4) + x, Bytes[x]);
	   }
	   
   }
 //----------------------------------------------------------------------------------------------------------------------
   private byte[][] ReadSector(int Sector,  byte[] Key) throws IOException
   {
	   byte [][]Bytes =new byte[4][16];
	   mfc.authenticateSectorWithKeyA(Sector, Key);
	   for(int x=0;x<4;x++)
	   {
		   if(Sector==0 && x==0)
			   continue;
		   
		   Bytes[x] = mfc.readBlock((Sector*4) + x);
	   }
	   
	   return Bytes;
   }   
//----------------------------------------------------------------------------------------------------------------------   
   private void CheckSector0()throws Exception
   {
	   byte [][]Sector;
	   
	   Sector = this.ReadSector(0, KeyA);
	   byte []IdCard={(byte)0xBB, (byte)0xAA, (byte)0xEE, (byte)0x12};
	   for(int x=0;x<IdCard.length;x++)		   	   
		   if(Sector[1][x]!=IdCard[x])
			   throw new Exception("Invalid card Id");	   
   }   
//----------------------------------------------------------------------------------------------------------------------
   private long ByteArrayToLong(byte[] by)
   {
	   
	  /* for (int i = 0; i < by.length; i++)
	   {
	      value += ((long) by[i] & 0xffL) << (8 * i);
	   }*/

	   
	   ByteBuffer buffer = ByteBuffer.allocate(8);
	   buffer.put(by);
	   
	   buffer.flip();//need flip 
	   return buffer.getLong();
	    
	   
   }
 //----------------------------------------------------------------------------------------------------------------------   
   public int[] ByteArrayToIntArray(byte[] bytes)
   {
       int [] retorno = new int[bytes.length];
       
       for(int x=0;x<bytes.length;x++)
       {
           if(bytes[x]>=0)
           {
             retorno[x]=bytes[x];
           }
           else
           {
             retorno[x]=256+bytes[x];  
           }
       }
       
       return retorno;
   }
//----------------------------------------------------------------------------------------------------------------------       
   public static byte[] longToBytes(long l) {
       ArrayList<Byte> bytes = new ArrayList<Byte>();
       
       for(int x=0;x<8;x++)
       {
           bytes.add((byte) (l % (0xff + 1)));
           l = l >> 8;
       }
       
       byte[] bytesp = new byte[8];
       
       for(int x=0;x<8;x++)
    	   bytesp[x]=0;
       
       
       for (int i = bytes.size() - 1, j = 0; i >= 0; i--, j++) {
           bytesp[j] = bytes.get(i);
       }
       return bytesp;
   }   
}
//----------------------------------------------------------------------------------------------------------------------
   



