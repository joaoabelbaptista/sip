/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Misc Test Class.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.vrs.sip.Util;
import com.vrs.sip.connection.drivers.SalesforceStatement;
import com.vrs.sip.task.Schedule;

public class MiscTest {
	public static void main(String[]args) {
		String hostName = Util.getLocalHostName();
		String hostName2 = Util.getLocalHostName(true);
		String processId = Util.getProcessId();
		String threadId = Util.getThreadId();
		String uniqueId = Util.getUniqueId();
		String simpleUniqueId = Util.getSimpleUniqueId();
		String recurrence = "5-12,2";
		Schedule.RecurrenceType recurrenceType = Schedule.RecurrenceType.Month;
		Schedule schedule = new Schedule();
		Calendar currentCalendarDate = Calendar.getInstance();
		Calendar startCalendarDate = Calendar.getInstance();
		Calendar lastScheduleCalendarDate = Calendar.getInstance();
		Date currentDate;
		Date startDate;
		Date lastScheduleDate;
		Date nextScheduleDate;
		SimpleDateFormat sdf = new SimpleDateFormat("E, yyyy-MM-dd HH:mm:SS");
		
		List<Integer> erol = Schedule.expandRecurrenceOffset(recurrence, recurrenceType);
		
		String soqlQuery = "SELECT Id FROM Account WHERE Unique_Key__c='STELLAR'";
		
		String fixSoqlQuery = SalesforceStatement.fixQuery(soqlQuery);
		
		
		System.out.println("soqlQuery=" + soqlQuery);
		System.out.println("fixSoqlQuery=" + fixSoqlQuery);
		
		System.out.println("HostName=" + hostName);
		System.out.println("HostNameWithoutDomain=" + hostName2);
		System.out.println("processID=" + processId);
		System.out.println("ThreadId=" + threadId);
		System.out.println("UniqueId=" + uniqueId);
		System.out.println("SimpleUniqueId=" + simpleUniqueId);
		
		
		System.out.println("Expansion of Recurrence Offset");
		for (Integer v : erol) {
			System.out.println("\t" + v);
		}
		
		// Wrap to 1st of Jan of current year
		startCalendarDate.set(Calendar.MONTH, 0);
		startCalendarDate.set(Calendar.DAY_OF_MONTH, 1);
		
		// Set as if it was scheduled run 1 minute before now
		lastScheduleCalendarDate.add(Calendar.MINUTE, -1);
		
		currentDate = currentCalendarDate.getTime();
		startDate = startCalendarDate.getTime();
		lastScheduleDate = lastScheduleCalendarDate.getTime();
		
		schedule.startDate = startDate;
		schedule.scheduleType = Schedule.ScheduleType.Recurrent;
	
		/* Next Minute Schedule */
/*
		schedule.recurrenceType = Schedule.RecurrenceType.Minute;
		schedule.recurrence = "1-59";
*/
		
		/* Next Hour Schedule */
/*
		schedule.recurrenceType = Schedule.RecurrenceType.Hour;
		schedule.recurrence = "1-23";
*/	
		/* Next Day Schedule */
		
		schedule.recurrenceType = Schedule.RecurrenceType.Day;
		schedule.recurrence = "1-31";
		
		/* Next Month Schedule */
/*
		schedule.recurrenceType = Schedule.RecurrenceType.Month;
		schedule.recurrence = "1-12";		
*/		
		
		nextScheduleDate = schedule.getNextScheduleDate(lastScheduleCalendarDate.getTime());
		
		System.out.println("LastScheduleDate=" + lastScheduleDate + ", CurrentDate=" + currentDate + ", NextScheduleDate=" + nextScheduleDate);
		
		System.out.println("Last Schedule Date = " + sdf.format(lastScheduleDate) + ", Current Date = " + sdf.format(currentDate) + ", Next Schedule Date = " + sdf.format(nextScheduleDate));
	}
}
