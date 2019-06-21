package com.vrs.sip;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.impl.SimpleLog;

public class FileLog extends SimpleLog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Configuration.Log logConfiguration;
	Configuration.Server serverConfiguration;
	
	String logFilename;
	
	String logFilenamePrefix;
	String logFilenameSuffix;
	
	String linePrefix;

	static String TZ = System.getenv("LOG_TZ");
	
	public FileLog(String name) {
		super(name);
		
		try {
			if (name != null) {
				logConfiguration = Configuration.getInstance().getLog();
				
				if (logConfiguration != null) {
					String logLevel = logConfiguration.level;
					
					if (logLevel != null) {
						setLevel(logLevel);
					}
				}
				
				serverConfiguration = Configuration.getInstance().getServer();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public FileLog() {
		super(null);
	}
	
	public void setLinePrefix(String prefix) {
		linePrefix = prefix;
	}
	
	public void setLogFilenamePrefix(String prefix) {
		this.logFilenamePrefix = prefix;
	}
	
	public void setLogFilenameSuffix(String suffix) {
		this.logFilenameSuffix = suffix;
	}
	
	public String getLogFilenamePrefix() {
		return logFilenamePrefix;
	}
	
	public String getLogFilenameSuffix() {
		return logFilenameSuffix;
	}
	
	public void setLevel(String level) {
		Integer logLevel = SimpleLog.LOG_LEVEL_INFO;
		
		switch (level.toUpperCase()) {
			case "ALL":
				logLevel = SimpleLog.LOG_LEVEL_ALL;
				break;
				
			case "DEBUG":
				logLevel = SimpleLog.LOG_LEVEL_DEBUG;
				break;
				
			case "ERROR":
				logLevel = SimpleLog.LOG_LEVEL_ERROR;
				break;
				
			case "FATAL":
				logLevel = SimpleLog.LOG_LEVEL_FATAL;
				break;
				
			case "INFO":
				logLevel = SimpleLog.LOG_LEVEL_INFO;
				break;
				
			case "OFF":
				logLevel = SimpleLog.LOG_LEVEL_OFF;
				break;
				
			case "TRACE":
				logLevel = SimpleLog.LOG_LEVEL_TRACE;
				break;
				
			case "WARN":
				logLevel = SimpleLog.LOG_LEVEL_WARN;
				break;
				
			default:
				throw new RuntimeException("Invalid Log level specified");
		}
		
		setLevel(logLevel);
	}
	
	public void setLogFilename(String filename) {
		logFilename = filename;
	}
	
	public String getLogFilenameFullPath() {
		String result = "";

		if (logConfiguration != null) {
			if (logConfiguration.directory != null) {
				result = logConfiguration.directory;
			}
			
			if (result != null && result.length() > 0) {
				result += "/" + logFilename;
			}
		} else {
			result = null;
		}
		
		return result;
	}
	
	public void append(String content) {
		try {
			String logFilename = getLogFilenameFullPath();
			BufferedWriter bw;
			
			if (logFilename != null) {
				bw = new BufferedWriter(new FileWriter(logFilename, true));
			
				bw.write(content);
				bw.close();
			} else {
				System.out.print(content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void log(int arg0, Object arg1, Throwable arg2) {
		String logLevel;
		String logLine = null;
		Date currentDate = new Date();
		String logDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		BufferedWriter bw = null;
		String logFilename = getLogFilenameFullPath();
		long threadId = Thread.currentThread().getId();
		
		//System.out.println("TZ=" + TZ);
		
		if (TZ != null && TZ.isEmpty() == false) {
			TimeZone tz = TimeZone.getTimeZone(TZ);
			
			sdf.setTimeZone(tz);
		}
		
		//System.out.println("format TimeZone: " + sdf.getTimeZone().toString());
		
		logDate = sdf.format(currentDate);
		
		if (logFilename != null) {
			try {
				bw = new BufferedWriter(new FileWriter(logFilename, true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (logFilename != null) {
			String arg1updated;
			
			if (linePrefix != null) {
				arg1updated = logDate + " [" + linePrefix + "] [T=" + threadId + "] " + arg1;
			} else {
				arg1updated = logDate + " [T=" + threadId + "] " + arg1;
			}
			
			super.log(arg0, arg1updated, arg2);
		}
		
		if (bw != null) {
			String logText = "";
			
			switch(arg0) {
				case LOG_LEVEL_DEBUG:
					logLevel = "DEBUG";
					break;
					
				case LOG_LEVEL_ERROR:
					logLevel = "ERROR";
					break;
					
				case LOG_LEVEL_FATAL:
					logLevel = "FATAL";
					break;
					
				case LOG_LEVEL_INFO:
					logLevel = "INFO";
					break;
					
				case LOG_LEVEL_WARN:
					logLevel = "WARN";
					break;
					
				default:
					logLevel = "";
					break;
			}
			
			if (linePrefix != null) {
				logLine = "[" + linePrefix + "] [T=" + threadId + "] " + logDate + " " + logLevel + " " + arg1;
			} else {
				logLine = "[T=" + threadId + "] " + logDate + " " + logLevel + " " + arg1;
			}
			
			if (arg2 != null) {				
				logLine += " " + Util.getStackTraceString(arg2);
			}
			
			if (serverConfiguration.audit) {
				if (linePrefix != null) {
					logText += "[" + linePrefix + "] ";
				}
				
				logText += arg1;
				
				if (arg2 != null) {
					logText += " " + Util.getStackTraceString(arg2);
				}
				
				// Pass to server audit
				if (logConfiguration.isDatabaseLog == true) {
					Logging.serverAudit(logLevel, logText);
				}
			}
			
			try {
				bw.write(logLine + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (linePrefix != null) {
				System.out.println("[" + linePrefix + "] [T=" + threadId + "] " + logLine);
			} else {
				System.out.println("[T=" + threadId + "] " + logLine);
			}
		}
	}
	
	public static String getNewFilenameInstance(String prefix, String suffix) {
		//Date currentDate = new Date();
		String filename;
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
		filename = "";
		
		if (prefix != null) {
			filename += prefix;
		}
		
		//filename += sdf.format(currentDate);
		
		if (suffix != null) {
			filename += suffix;
		}
		
		return filename;
	}
	
	public static FileLog getNewInstance(@SuppressWarnings("rawtypes") Class zclass, String logFilenamePrefix, String logFilenameSuffix) {
		FileLog fl = new FileLog(zclass.getName());
		
		fl.setLogFilenamePrefix(logFilenamePrefix);
		fl.setLogFilenameSuffix(logFilenameSuffix);
		
		fl.setLogFilename(getNewFilenameInstance(logFilenamePrefix, logFilenameSuffix));
		
		return fl;
	}
	
	public void closeLogFile() {
		setLogFilename(null);
	}
	
	public void reopenLogFile() {
		if (logFilename == null) {
			setLogFilename(getNewFilenameInstance(getLogFilenamePrefix(), getLogFilenameSuffix()));
		}
	}
}
