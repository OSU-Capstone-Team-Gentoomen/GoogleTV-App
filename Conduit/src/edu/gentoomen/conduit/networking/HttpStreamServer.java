package edu.gentoomen.conduit.networking;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import android.util.Log;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/*
 * Simple HTTP servlet that will serve samba content
 * to clients that connect, this class is based off of
 * the NanoHTTPD source code
 */
public class HttpStreamServer{

	private static final String TAG = "StreamOverHttp";	
	private static final String HTTP_BAD_REQUEST = "400 Bad Request";
	private static final String HTTP_416 = "416 Range not satisfiable";
	private static final String HTTP_INTERNAL_ERROR = "500 Internal Server Error";
	private static final String HTTP_OK = "200 OK";
	private static final String HTTP_PARTIAL = "206 Partial Content";
	private static final String HTTP_CLRF = "\r\n";

	private static final int    HTTP_PORT = 8888;
	
	private final String       fileMimeType;
	private final ServerSocket serverSocket;
	private Thread             listenThread;
	private SmbFile     	   file;
	
	private static Hashtable<String, String> theMimeTypes = new Hashtable<String, String>();
	static {
		StringTokenizer st = new StringTokenizer(					
						"txt		text/plain "+
						"gif		image/gif "+
						"jpg		image/jpeg "+
						"jpeg		image/jpeg "+
						"png		image/png "+
						"mp3		audio/mpeg "+
						"m3u		audio/mpeg-url " +
						"mp4		video/mp4 " +
						"mkv		video/x-matroska " +
						"avi 		video/x-msvideo " +
						"ogv		video/ogg " +
						"flv		video/x-flv " +
						"mov		video/quicktime " +
						"swf		application/x-shockwave-flash " +
						"ogg		application/x-ogg "+
						"class		application/octet-stream " );
		while (st.hasMoreTokens())
			theMimeTypes.put(st.nextToken(), st.nextToken());
	}


	public HttpStreamServer(String path, String mimeType) throws IOException{

		Log.d(TAG, "starting StreamOverHttp init");
		file = new SmbFile("smb://" + path);		
		fileMimeType = mimeType;
		serverSocket = new ServerSocket(HTTP_PORT);

		listenThread = new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					while (true) {
						Log.d(TAG, "Waiting for connection");
						Socket accept = serverSocket.accept();
						Log.d(TAG, "Connection accepted");
						new HttpSession(accept);
					}
				}catch(IOException e){
					e.printStackTrace();
				}
			}

		});

		listenThread.setName("Stream over HTTP");
		listenThread.setDaemon(true);
		listenThread.start();

	}

	private class HttpSession implements Runnable {

		private SmbFileInputStream smbFileInputStream;
		private final Socket clientSocket;
		private byte[] buf;

		HttpSession(Socket socket){

			clientSocket = socket;
			buf = new byte[8192];
			Thread responseThread = new Thread(this, "Http response");
			responseThread.setDaemon(true);
			responseThread.start();

		}

		@Override
		public void run(){

			try {
				smbFileInputStream = new SmbFileInputStream(file);
			} catch (SmbException e1) {				
				e1.printStackTrace();
			} catch (MalformedURLException e1) {				
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			handleResponse();

			if (smbFileInputStream != null) {
				try{
					smbFileInputStream.close();
				}catch(IOException e){
					e.printStackTrace();
				}            
			}
		}     

		private Properties readHeader(InputStream clientStream) throws InterruptedException, IOException {

			int retLen = clientStream.read(buf, 0, buf.length);

			if (retLen <= 0)
				return null;

			ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, retLen);
			BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
			Properties clientHeaders = new Properties();

			if(!decodeHeader(hin, clientHeaders))
				return null;

			return clientHeaders;
		}

		private ReturnHeader constructReturnHeader(Properties clientHeader) throws InterruptedException, IOException {

			String range = clientHeader.getProperty("range");
			Properties responseHeader = new Properties();

			if(file.length()!=-1)
				responseHeader.put("Content-Length", String.valueOf(file.length()));

			responseHeader.put("Accept-Ranges", "bytes");

			long sendCount;
			String status;

			if (range == null) {
				status = HTTP_OK;
				sendCount = (int)file.length();
			} else {
				if (!range.startsWith("bytes=")) {
					sendError(HTTP_416, null);
					return null;
				}

				Log.d(TAG, "Range: " + range);
				range = range.substring(6);
				long startFrom = 0;
				long endAt = -1;
				int hyphen = range.indexOf('-');
				if (hyphen > 0) {
					try {
						String startRange = range.substring(0, hyphen);
						startFrom = Long.parseLong(startRange);
						String endRange = range.substring(hyphen + 1);
						endAt = Long.parseLong(endRange);
					} catch(NumberFormatException e) {
					}
				}

				if (startFrom >= file.length()) {
					sendError(HTTP_416, null);					
					return null;
				}
				if (endAt < 0)
					endAt = file.length() - 1;

				sendCount = (int)(endAt - startFrom + 1);

				if(sendCount < 0)
					sendCount = 0;

				status = HTTP_PARTIAL;
				smbFileInputStream.skip(startFrom);

				responseHeader.put("Content-Length", "" + sendCount);
				String rangeSpec = "bytes " + startFrom + "-" + endAt + "/" + file.length();
				responseHeader.put("Content-Range", rangeSpec);
			}

			ReturnHeader headerResponse = new ReturnHeader(responseHeader, sendCount, status);
			return headerResponse;

		}

		/*
		 * This function will parse the header
		 * and construct a response and send it to
		 * the client with the binary data
		 */
		private void handleResponse(){

			try {			
				InputStream inS = clientSocket.getInputStream();

				if (inS == null)
					return;

				Properties clientHeader = readHeader(inS);

				if (clientHeader == null)
					return;

				ReturnHeader response = constructReturnHeader(clientHeader);

				if (response == null) {
					inS.close();
					return;
				}

				sendResponse(response.status, fileMimeType, response.returnHeader, smbFileInputStream, response.sendCount, null);
				inS.close();
				Log.d(TAG, "stream finished");

			} catch(IOException ioe) {				
					ioe.printStackTrace();
				try {
					sendError(HTTP_INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				} catch(Throwable t) {
				}
			} catch(InterruptedException ie) {
				/*Could not send the error, just exit*/				
				ie.printStackTrace();
			}    	      	      	
		}

		private boolean decodeHeader(BufferedReader in, Properties clientHeader) throws InterruptedException{

			try {
				/*Read in the line*/
				String inLine = in.readLine();

				if (inLine == null)
					return false;

				StringTokenizer st = new StringTokenizer(inLine);

				/*Sanity check, make sure we've got a header*/
				if (!st.hasMoreTokens())
					sendError(HTTP_BAD_REQUEST, "Syntax error");

				String method = st.nextToken();

				/*We only support GET methods*/
				if (!method.equals("GET"))
					return false;

				if (!st.hasMoreTokens())
					sendError(HTTP_BAD_REQUEST, "Missing URI");

				/*Now read the rest of the header*/
				while (true) {
					String line = in.readLine();

					if (line==null)
						break;

					int colon = line.indexOf(':');

					if (colon < 0)
						continue;

					/*Put our header into the properties object*/
					String atr = line.substring(0, colon).trim().toLowerCase();
					String val = line.substring(colon + 1).trim();
					clientHeader.put(atr, val);
				}
			} catch(IOException ioe) {
				sendError(HTTP_INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
			return true;

		}

		/*Send a response along with binary data to the client, then close the socket*/
		private void sendResponse(String status, String mimeType, Properties header, SmbFileInputStream smbInput, long sendCount, String errMsg){

			try {
				OutputStream out = clientSocket.getOutputStream();
				PrintWriter writer = new PrintWriter(out);

				/*Write out our header to the client stream*/
				if (status != null) {
					String retLine = "HTTP/1.0 " + status + HTTP_CLRF;
					writer.print(retLine);
				}

				if (mimeType != null) {
					String mimeT = "Content-Type: " + mimeType + HTTP_CLRF;
					writer.print(mimeT);
				}

				if (header != null) {
					Enumeration<?> e = header.keys();
					while (e.hasMoreElements()) {
						String key = (String)e.nextElement();
						String value = header.getProperty(key);
						String l = key + ": " + value + HTTP_CLRF;
						writer.print(l);
					}
				}
				writer.print(HTTP_CLRF);
				writer.flush();

				/*Now write the binary data*/
				if (smbInput != null)
					copyStream(smbInput, out, buf, sendCount);
				else if (errMsg != null) {
					writer.print(errMsg);
					writer.flush();
				}
				out.flush();
				out.close();

			} catch(IOException e) {        
			} finally {
				try {
					clientSocket.close();
				} catch(Throwable t) {
				}
			}
		}

		/*Send an HTTP error response*/
		private void sendError(String status, String msg) throws InterruptedException{

			sendResponse(status, "text/plain", null, null, 0, msg);
			throw new InterruptedException();

		}

		/*Do a byte copy to our output stream to the client*/
		private void copyStream(SmbFileInputStream in, OutputStream out, byte[] tmpBuf, long maxSize) throws IOException{

			while (maxSize > 0) {
				int count = (int)Math.min(maxSize, tmpBuf.length);
				count = in.read(tmpBuf, 0, count);
				if( count < 0 )
					break;
				out.write(tmpBuf, 0, count);
				maxSize -= count;				
			}

		}

		/*Simple container class*/
		private class ReturnHeader {

			Properties returnHeader = null;
			long sendCount = -1;
			String status = null;

			public ReturnHeader(Properties header, long count, String stat) {

				returnHeader = header;
				sendCount = count;
				status = stat;

			}
		}
	}

	public void close(){
		
			try {				
				/*
				 * Stop the listening thread NOW!
				 * or else the listen thread will accept a connection
				 * to a dead socket if the user selects a file
				 * before the listen thread is stopped
				 */				 
				listenThread.interrupt();			
				serverSocket.close();	
			} catch (IOException e) {				
				e.printStackTrace();
			}
	}

//	public static String getMimeType(String fileName) {
//
//		String mime;
//		int extensionStart = fileName.lastIndexOf('.');
//		Log.d(TAG, "extension found " + fileName.substring(extensionStart));
//		mime = theMimeTypes.get(fileName.substring(extensionStart + 1).toLowerCase());
//		return mime;
//
//	}
	
	public static int getBindPort() {
		return HTTP_PORT;
	}
	
	public void setNewFile(String newFile) {
		
		//Not a good solution, need to do some error handling higher up
		SmbFile oldFile = file;
		
		try {
			file = new SmbFile("smb://" + newFile);
		} catch (MalformedURLException e) {
			file = oldFile;
			e.printStackTrace();
		}
	}
}