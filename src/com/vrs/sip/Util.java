package com.vrs.sip;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

public class Util {
	/** Get local hostname (use trimDomainName to select between without domain name (true) or with domain name (false) **/
	public static String getLocalHostName(Boolean trimDomainName) {
		String result = null;
		
		//	NOTE -- InetAddress.getLocalHost().getHostName() will not work in certain environments.
		try {
		    result = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		    // failed;  try alternate means.
		}
	
		if (result == null || result.isEmpty()) {
			// try environment properties.
			result = System.getenv("COMPUTERNAME");

			if (result == null || result.isEmpty()) {
				result = System.getenv("HOSTNAME");
			}
		}
		
		if (result != null && result.isEmpty() == false) {
			if (trimDomainName) {
				int firstIndexOfDot = result.indexOf(".");
						
				if (firstIndexOfDot != -1) {
					result = result.substring(0, firstIndexOfDot);
				}
			}
		}
		
		return result;
	}
	
	/** Get local hostname including domain name **/
	public static String getLocalHostName() {
		return getLocalHostName(false);
	}
	
	/** Get current process ID **/
	public static String getProcessId() {
		String processID = null;
		
		processID = ManagementFactory.getRuntimeMXBean().getName();
		
		if (processID != null) {
			int indexOfAt = processID.indexOf("@");
			
			if (indexOfAt != -1) {
				processID = processID.substring(0, indexOfAt);
			}
		}
		
		return processID;
	}
	
	/** Get current executing thread ID **/
	public static String getThreadId() {
		String threadId = null;
		Thread currentT = Thread.currentThread();
		
		if (currentT != null) {
			threadId = String.valueOf(currentT.getId());
		}
		
		return threadId;
	}
	
	/** Get unique ID **/
	public static String getUniqueId() {
		String localHostName = String.format("%x",  new BigInteger(1, getLocalHostName(true).getBytes()));
		String processId = getProcessId();
		String threadId = getThreadId();
		String nanoSecsHex = String.format("%x", System.nanoTime());
		String result = "";
		
		result = nanoSecsHex + "_" + localHostName + "_" + processId + "_" + threadId;
		
		return result;
	}
	
	/** Get a Simple Unique ID **/
	public static String getSimpleUniqueId() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date currentDate = new Date();
		String processId = getProcessId();
		String threadId = getThreadId();
		String simpleUniqueId;
		String TZ = System.getenv("LOG_TZ");
		TimeZone timezone;
		
		if (TZ != null) {
			timezone = TimeZone.getTimeZone(TZ);
			
			simpleDateFormat.setTimeZone(timezone);
		}
		
		simpleUniqueId = simpleDateFormat.format(currentDate) + "_" + processId + "_" + threadId;
		
		return simpleUniqueId;
	}
	
	/** Get a String dump of a Throwable Stack Trace **/
	public static String getStackTraceString(Throwable t) {
		if (t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			t.printStackTrace(pw);
			
			return sw.toString();
		}
		
		return null;
	}
	
	/** Get a String dump of the current Thread Stack Trace **/
	public static String getCurrentThreadStackTraceString() {
		Thread thread = Thread.currentThread();
		
		return getStackTraceString(thread.getStackTrace());
	}
	
	/** Get a String dump of a Thread Stack Trace **/
	public static String getStackTraceString(StackTraceElement[] stackTraceElementArray) {
		String result = "";
		
		for (StackTraceElement traceElement : stackTraceElementArray) {
			if (result.isEmpty() == false) {
				result += "\n";
			}
			
			result += traceElement;
		}
		
		return result;
	}
	
	/** Create a Directory **/
	public static void createDirectory(String pathname) {
		Boolean isDirectoryFound = false;
		File path = new File(pathname);
		
		isDirectoryFound = path.exists();
		
		if (! isDirectoryFound) {
			Boolean result = path.mkdirs();
			
			if (! result) {
				throw new RuntimeException("Error creating directory \'" + pathname + "'");
			}
		}
	}
	
	/** Check if a Directory has files **/
	public static Boolean hasFiles(String pathname) {
		Boolean filesFound = false;
		File path = new File(pathname);
		
		if (path.exists()) {
			String[] files = path.list();
			
			if (files != null && files.length > 0) {
				for (String filename : files) {
					String testFilename = pathname + "/" + filename;
					File testFile = new File(testFilename);
					
					if (testFile.isFile()) {
						filesFound = true;
						break;
					}
				}
			}
		}
		
		//System.out.println("hasFiles(" + pathname + ")=" + filesFound);
		
		return filesFound;
	}
	
	/** Move a Path (either file or directory) to a new name **/
	public static void rename(String pathname, String targetPathname) {
		File sourcePath = new File(pathname);
		File targetPath = new File(targetPathname);
		
		if (! sourcePath.renameTo(targetPath)) {
			throw new RuntimeException("Error moving \'" + pathname + "\' to \'" + targetPathname + "\'");
		}
	}
	
	public static String getMapAsString(Map<String,Object> context) {
		List<String> keyList = new Vector<String>();
		String result = null;
		
		if (context != null) {
			keyList.addAll(context.keySet());
			Collections.sort(keyList);
			
			for (String key : keyList) {
				if (result == null) {
					result = "";
				} else {
					result += "\n";
				}
				
				result += key + ": \'" + context.get(key) + "\'";
			}
		}
		
		return result;
	}
}
