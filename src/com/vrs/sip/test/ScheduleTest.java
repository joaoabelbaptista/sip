package com.vrs.sip.test;

import java.util.Date;

import com.vrs.sip.task.Schedule;

public class ScheduleTest {
	public static boolean isDebug = false;
	
	public static void main(String[] args) {		
		testRecurrenceDailyAtSpecificTimeWithoutLastRun();
		testRecurrenceDailyAtSpecificTimeWithLastRun1();
		testRecurrenceDailyAtSpecificTimeWithLastRun2();
	}
	
	public static boolean isDateEqual(Date date1, Date date2, int thresholdMiliseconds) {
		boolean result = false;
		
		long l1 = date1.getTime();
		long l2 = date2.getTime();
		
		if (Math.abs(l2-l1) <= thresholdMiliseconds) {
			result = true;
		}
		
		return result;
	}
	
	
	public static void testRecurrenceDailyAtSpecificTimeWithoutLastRun() {
		Schedule schedule = new Schedule();
		Date startDate;
		Date now = new Date();
		Date lastRun = null;
		Date expectedRun = new Date();
		Date nextDate;
		int secsOffsetNow = 2 * 60 * 60;
		int thresholdMs = 50;
		
		schedule.setDebug(isDebug);
		startDate = new Date();
		
		// Start Date is yesterday at same time + secondsOffset
		startDate.setTime(startDate.getTime() - 86400*1000 + secsOffsetNow * 1000);
		
		expectedRun.setTime(now.getTime());
		
		schedule.scheduleId = "1";
		schedule.scheduleName = "testRecurrenceDailyAtSpecificTimeWithoutLastRun";
		schedule.startDate = startDate;
		
		schedule.scheduleType = Schedule.ScheduleType.Recurrent;
		schedule.recurrenceType = Schedule.RecurrenceType.Day;
		schedule.recurrence = "*"; // Every day
		
		nextDate = schedule.getNextScheduleDate(lastRun);
		
		System.out.println("Schedule:\n\tName=" + schedule.scheduleName + "\n\tRecurrence=" + schedule.recurrence + "\n\tRecurrenceType=" + schedule.recurrenceType + "\n\tScheduleType=" + schedule.scheduleType + "\n\tScheduleStartDate=" + schedule.startDate + "\n\tLastRun=" + lastRun + "\n\tExpected=" + expectedRun + "\n\tNext=" + nextDate + "\n\tThresholdMilisecs=" + thresholdMs);
		
		if (isDateEqual(nextDate, expectedRun, thresholdMs)) {
			System.out.println("  OK\n");
		} else {
			System.out.println("  FAIL\n");
		}
		
		assert(nextDate == expectedRun);
	}

	public static void testRecurrenceDailyAtSpecificTimeWithLastRun1() {
		Schedule schedule = new Schedule();
		Date startDate;
		Date now = new Date();
		Date lastRun = new Date();
		Date expectedRun = new Date();
		Date nextDate;
		int secsOffsetNow = 2 * 60 * 60;
		int thresholdMs = 50;
		
		schedule.setDebug(isDebug);
		startDate = new Date();
		
		// Start Date is yesterday at same time + secondsOffset
		startDate.setTime(startDate.getTime() - 86400*1000 + secsOffsetNow * 1000);
		
		lastRun.setTime(startDate.getTime());
		expectedRun.setTime(now.getTime() + secsOffsetNow * 1000);
		
		schedule.scheduleId = "1";
		schedule.scheduleName = "testRecurrenceDailyAtSpecificTimeWithLastRun1";
		schedule.startDate = startDate;
		
		schedule.scheduleType = Schedule.ScheduleType.Recurrent;
		schedule.recurrenceType = Schedule.RecurrenceType.Day;
		schedule.recurrence = "*"; // Every day
		
		nextDate = schedule.getNextScheduleDate(lastRun);
		
		System.out.println("Schedule:\n\tName=" + schedule.scheduleName + "\n\tRecurrence=" + schedule.recurrence + "\n\tRecurrenceType=" + schedule.recurrenceType + "\n\tScheduleType=" + schedule.scheduleType + "\n\tScheduleStartDate=" + schedule.startDate + "\n\tLastRun=" + lastRun + "\n\tExpected=" + expectedRun + "\n\tNext=" + nextDate + "\n\tThresholdMilisecs=" + thresholdMs);
		
		if (isDateEqual(nextDate, expectedRun, thresholdMs)) {
			System.out.println("  OK\n");
		} else {
			System.out.println("  FAIL\n");
		}
		
		assert(nextDate == expectedRun);
	}

	public static void testRecurrenceDailyAtSpecificTimeWithLastRun2() {
		Schedule schedule = new Schedule();
		Date startDate;
		Date now = new Date();
		Date lastRun = new Date();
		Date expectedRun = new Date();
		Date nextDate;
		int secsOffsetNow = 2 * 60 * 60;
		int thresholdMs = 50;
		
		schedule.setDebug(isDebug);
		startDate = new Date();
		
		// Start Date is yesterday at same time + secondsOffset
		startDate.setTime(startDate.getTime() - 86400*1000 + secsOffsetNow * 1000);
		
		lastRun.setTime(now.getTime());
		
		expectedRun.setHours(0);
		expectedRun.setMinutes(0);
		expectedRun.setSeconds(0);
		
		expectedRun.setTime(expectedRun.getTime() + 86400*1000);
		
		expectedRun.setHours(startDate.getHours());
		expectedRun.setMinutes(startDate.getMinutes());
		expectedRun.setSeconds(startDate.getSeconds());
		
		expectedRun.setTime(expectedRun.getTime());
		
		schedule.scheduleId = "1";
		schedule.scheduleName = "testRecurrenceDailyAtSpecificTimeWithLastRun2";
		schedule.startDate = startDate;
		
		schedule.scheduleType = Schedule.ScheduleType.Recurrent;
		schedule.recurrenceType = Schedule.RecurrenceType.Day;
		schedule.recurrence = "*"; // Every day
		
		nextDate = schedule.getNextScheduleDate(lastRun);
		
		System.out.println("Schedule:\n\tName=" + schedule.scheduleName + "\n\tRecurrence=" + schedule.recurrence + "\n\tRecurrenceType=" + schedule.recurrenceType + "\n\tScheduleType=" + schedule.scheduleType + "\n\tScheduleStartDate=" + schedule.startDate + "\n\tLastRun=" + lastRun + "\n\tExpected=" + expectedRun + "\n\tNext=" + nextDate + "\n\tThresholdMilisecs=" + thresholdMs);
		
		if (isDateEqual(nextDate, expectedRun, thresholdMs)) {
			System.out.println("  OK\n");
		} else {
			System.out.println("  FAIL\n");
		}
		
		assert(nextDate == expectedRun);
	}
}
