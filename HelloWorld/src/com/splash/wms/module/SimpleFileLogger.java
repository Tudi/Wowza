package com.splash.wms.module;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleFileLogger {
	public SimpleFileLogger( )
	{
	}
	public SimpleFileLogger( String str )
	{
		Log( str );
	}
	public void Log( String msg )
	{
		try {
			FileWriter fwriter;
			BufferedWriter writer;
			fwriter = new FileWriter( "d:/temp/MyLog.txt", true );
			writer = new BufferedWriter(fwriter);
			writer.write(msg + "\n");
			writer.close();
			fwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
