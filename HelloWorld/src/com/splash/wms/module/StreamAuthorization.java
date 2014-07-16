package com.splash.wms.module;

import com.wowza.wms.httpstreamer.cupertinostreaming.httpstreamer.*;
import com.wowza.wms.module.*;
import com.wowza.wms.application.*;

import java.sql.*;
import java.util.Formatter;
import java.util.Locale;

import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;

public class StreamAuthorization extends ModuleBase {
	public void onHTTPCupertinoStreamingSessionCreate(HTTPStreamerSessionCupertino httpCupertinoStreamingSession)
	{
		boolean isGood = true;
		
		String ipAddressClient = httpCupertinoStreamingSession.getIpAddress();
		String ipAddressServer = httpCupertinoStreamingSession.getServerIp();
		String queryStr = httpCupertinoStreamingSession.getQueryStr();
		String referrer = httpCupertinoStreamingSession.getReferrer();
		String cookieStr = httpCupertinoStreamingSession.getCookieStr();
		String userAgent = httpCupertinoStreamingSession.getUserAgent();
		
		IApplicationInstance appInstance = httpCupertinoStreamingSession.getAppInstance();
		String streamName = httpCupertinoStreamingSession.getStreamName();
		
		// Here you can use the request and session information above to determine 
		// if you want to reject the connection
		// isGood = true/false;
				
		String res = "ModuleAccessControlCupertinoStreaming.onHTTPCupertinoStreamingSessionCreate["+appInstance.getContextStr()+":"+streamName+"]: accept:"+isGood;
		getLogger().info( res );
		
		SimpleFileLogger log2 = new SimpleFileLogger( res );
		SimpleFileLogger log3 = new SimpleFileLogger( "Isggod : " + isGood );
		
		if (!isGood)
			httpCupertinoStreamingSession.rejectSession();
	}
	public void onAppStart(IApplicationInstance appInstance)
	{
		// preload the driver class
		try 
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance(); 
		} 
		catch (Exception e) 
		{ 
			getLogger().error("Error loading: com.mysql.jdbc.Driver: "+e.toString());
			SimpleFileLogger log3 = new SimpleFileLogger( "Error loading: com.mysql.jdbc.Driver: "+e.toString() );
		} 
		SimpleFileLogger log2 = new SimpleFileLogger( "auth : loaded msql" );
	}

	public void onConnect(IClient client, RequestFunction function, AMFDataList params) 
	{
		
		String userName = getParamString(params, PARAM1);
		String password = getParamString(params, PARAM2);
		
		String ClientParams = client.getQueryStr();
		if( userName == null && ClientParams != null )
		{
//			SimpleFileLogger log3 = new SimpleFileLogger( "auth : split1 " + ClientParams );
			String[] parts = ClientParams.split("#");
			for( String a : parts )
			{
				String[] parts2 = a.split("=");
//				SimpleFileLogger log4 = new SimpleFileLogger( "auth : split2 " + a + " " + parts2[0] + " " + parts2[1] );
				if( parts2[0].equals( "user" ) )
				{
//					SimpleFileLogger log5 = new SimpleFileLogger( "auth : split - setusername " + parts2[1] );
					userName = parts2[1];
				}
				else if( parts2[0].equals( "passw" ) )
				{
//					SimpleFileLogger log5 = new SimpleFileLogger( "auth : split - setpassw " + parts2[1] );
					password = parts2[1];
				}
			}
		}

		SimpleFileLogger log2 = new SimpleFileLogger( "auth : onConnect " + userName + " " +  password + " " + client.getClientId() );
//		SimpleFileLogger log3 = new SimpleFileLogger( "auth : onConnect " + client.getQueryStr() );
		
		Connection conn = null;
		try 
		{
			conn = DriverManager.getConnection("jdbc:mysql://localhost/temp?user=wowd&password=wowd");

			Statement stmt = null;
			ResultSet rs = null;

			try 
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT count(*) as userCount FROM users where user = '"+userName+"' and passw = '"+password+"'");
				if (rs.next() == true)
				{
					SimpleFileLogger log3 = new SimpleFileLogger( "auth : mysql found a row " );
				    if (rs.getInt("userCount") > 0)
					{
						SimpleFileLogger log4 = new SimpleFileLogger( "auth : mysql should accept connection " );
						client.acceptConnection();
					}
				    else
				    {
						SimpleFileLogger log5 = new SimpleFileLogger( "auth : mysql should reject connection : user not found or incorrect data " );
						client.rejectConnection();
				    }
				}
			} 
			catch (SQLException sqlEx) 
			{
				getLogger().error("sqlexecuteException: " + sqlEx.toString());
				SimpleFileLogger log3 = new SimpleFileLogger( "auth : mysql cound not connect " + sqlEx.toString() );
			} 
			finally 
			{
				// it is a good idea to release
				// resources in a finally{} block
				// in reverse-order of their creation
				// if they are no-longer needed

				if (rs != null) 
				{
					try 
					{
						rs.close();
					} 
					catch (SQLException sqlEx) 
					{

						rs = null;
					}
				}

				if (stmt != null) 
				{
					try 
					{
						stmt.close();
					} 
					catch (SQLException sqlEx) 
					{
						stmt = null;
					}
				}
			}

			conn.close();
		} 
		catch (SQLException ex) 
		{
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		getLogger().info("onConnect: " + client.getClientId());
	}

	static public void onConnectAccept(IClient client) 
	{
		getLogger().info("onConnectAccept: " + client.getClientId());
		SimpleFileLogger log2 = new SimpleFileLogger( "auth : onConnectAccept " );
	}

	static public void onConnectReject(IClient client) 
	{
		getLogger().info("onConnectReject: " + client.getClientId());
		SimpleFileLogger log2 = new SimpleFileLogger( "auth : onConnectReject " );
	}

	static public void onDisconnect(IClient client) 
	{
		getLogger().info("onDisconnect: " + client.getClientId());
		SimpleFileLogger log2 = new SimpleFileLogger( "auth : onDisconnect " );
	}	
}
