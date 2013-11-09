package com.example.android.snake;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

   
public class DIC{
	 
	private SQLiteDatabase db;
	private Context context;  
	private String bookName;
	private Word[] mWordList;
	private final int maxwords = 50;
	private static final Random RNG = new Random();
	
	private final String DATABASE_PATH = android.os.Environment  
 										.getExternalStorageDirectory().getAbsolutePath()  
 										+ "/dictionary";  
	private final String DATABASE_FILENAME = "my_dic.db";  
	
	public DIC(Context context, String name){
		this.context = context;  
		this.bookName = name;
		db = openDatabase();
	}
	
	private SQLiteDatabase openDatabase()  
	{  
		 SQLiteDatabase database = null;
	     try  
	     {  
	         // 获得dictionary.db文件的绝对路径  
	         String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;  
	         File dir = new File(DATABASE_PATH);    
	         if (!dir.exists())  
	             dir.mkdir();  

	         if (!(new File(databaseFilename)).exists())  
	         {  
	             // 获得封装my_dic.db文件的InputStream对象  
	             InputStream is = context.getResources().openRawResource(R.raw.my_dic);  
	             FileOutputStream fos = new FileOutputStream(databaseFilename);  
	             byte[] buffer = new byte[8192];  
	             int count = 0;  
	             // 开始复制my_dic.db文件  
	             while ((count = is.read(buffer)) > 0)  
	             {  
	                 fos.write(buffer, 0, count);  
	             }  
	     
	             fos.close();  
	             is.close();  
	         }  
	         
	         database = SQLiteDatabase.openOrCreateDatabase(databaseFilename, null);  
	     }  
	     catch (Exception e)  
	     {  
	    	 int i = 0;
	    	 i = i + 1;
	     }  
	     return database;  
	 }  
	
	/*关闭数据源*/
	public void closeDB(){
		db.close();
	}
	
	/*插入数据*/
	public void insertDB(Word word,String wordlist,String MyCET){
		
		String wordspelling = word.getSpelling();
		String wordmeaning = word.getMeaning();
		String sql = "insert into My_Dic(wordlist,wordspelling,wordmeaning,CET) values('"+wordlist+"','"+wordspelling+"','"+wordmeaning+"','"+MyCET+"')";
		db.execSQL(sql);
	}
	
	/*清空所有数据*/
	public void deleteDB(){
		db.execSQL("delete from My_Dic");
	}
	
	/*根据条件获取数据*/
	public Boolean loadList(String list){
		mWordList = new Word[maxwords];
		Cursor myCursor = db.rawQuery("SELECT wordspelling, wordmeaning FROM My_Dic WHERE wordlist='"+list+"' and CET='"+bookName+"'" , null );
		int wordspellingColumn = myCursor.getColumnIndex("wordspelling");
        int wordmeaningColumn = myCursor.getColumnIndex("wordmeaning");
        int count = 0;
        if (myCursor.moveToFirst()) {
             /* 循环所有记录 */
             do {
                  String wordspelling = myCursor.getString(wordspellingColumn);
                  byte[] tmp = myCursor.getBlob(wordmeaningColumn);
                  String wordmeaning = "";
                  try {
                	  wordmeaning = new String(tmp, "GBK");
				} catch (Exception e) {
					// TODO: handle exception
				}
                                       
                  /* 放到 result */
                  mWordList[count] = new Word();
                  mWordList[count].setSpelling(wordspelling);
                  mWordList[count].setMeaning(wordmeaning);
                  count++;
                
             } while (myCursor.moveToNext()&&count<=maxwords); // end do while
             return true;
        }// end if
        return false;
	}
	
	public int getlistCount(){
		int listCount=0;
		String sql = "SELECT COUNT(DISTINCT wordlist) FROM My_Dic WHERE CET='" + bookName + "'" ;
		Cursor listCountCursor = db.rawQuery(sql,null);
		if(listCountCursor.moveToFirst())
			listCount = listCountCursor.getInt(0);
		return listCount;
	}
	
	public Word[] getRandomWords(int count)
	{
		Word[] ret = new Word[count];
		int randomRange = maxwords / count;
		for(int i = 0; i < count; i ++)
		{
			int index = i * randomRange + RNG.nextInt(randomRange);
			if(index < maxwords)
				ret[i] = mWordList[index];
			else ret[i] = mWordList[count - 1]; 
		}
		return ret;
	}
}
