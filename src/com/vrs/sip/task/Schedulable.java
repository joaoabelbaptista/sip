package com.vrs.sip.task;

import com.vrs.sip.Server;

import java.util.Date;

public abstract class Schedulable implements Runnable {

	public abstract void storeBeginExecution() throws Exception;

	public abstract void storeLastScheduleDate() throws Exception;
	
	public abstract Schedule getSchedule() throws Exception;
	
	public abstract Date getLastScheduleDate() throws Exception;
	
	public abstract void setLastScheduleDate(Date lastScheduleDate) throws Exception;
	
	public abstract void run();
	
	public abstract String getSchedulableName() throws Exception;
	
	public abstract String getSchedulableId() throws Exception;
	
	public abstract void setServerInstance(Server serverInstance);
	
	public abstract void abort() throws Exception;
}
