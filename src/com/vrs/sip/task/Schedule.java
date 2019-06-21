/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Schedule Class.
 * History: aosantos, 2016-07-06, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

import com.vrs.sip.FileLog;
import com.vrs.sip.Util;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule {
	private static FileLog log = FileLog.getNewInstance(Task.class, "schedule_" + Util.getSimpleUniqueId(), ".log");
	
	Boolean isDebug = false;
	
	public enum RecurrenceType {
		Year(Calendar.YEAR), Month(Calendar.MONTH), Weekday(Calendar.DAY_OF_WEEK), Day(Calendar.DAY_OF_MONTH), Hour(Calendar.HOUR_OF_DAY), Minute(Calendar.MINUTE), Second(Calendar.SECOND);
		
		private int calendarField;
		
		private RecurrenceType(int calendarField) {
			this.calendarField = calendarField;
		}
		
		public int getCalendarField() {
			return calendarField;
		}
	}
	
	public enum ScheduleType {
		OneTime, Recurrent
	}
	
	public FileLog getLog() {
		return log;
	}
	
	public enum DayOfWeek {
		Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
	}

	public void setDebug(Boolean isDebug) {
		this.isDebug = isDebug;
	}
	
	public static Map<String,DayOfWeek> weekdayMap = initializeDayOfWeekMap();
	public static Map<DayOfWeek,Integer> weekdayOffsetMap = initializeWeekdayOffsetMap();
	
	public String scheduleId;
	public String scheduleName;
	public Date startDate;
	public Date endDate;
	public String recurrence;
	public RecurrenceType recurrenceType;
	public ScheduleType scheduleType;

    public String getEmailTo() {
        return EmailTo == null ? "" : EmailTo;
    }

    public void setEmailTo(String emailTo) {
        EmailTo = emailTo;
    }

    public String getEmailCc() {
        return EmailCc == null ? "" : EmailCc;
    }

    public void setEmailCc(String emailCc) {
        EmailCc = emailCc;
    }

    public String getEmailBcc() {
        return EmailBcc == null ? "": EmailBcc;
    }

    public void setEmailBcc(String emailBcc) {
        EmailBcc = emailBcc;
    }

    public String EmailTo;
    public String EmailCc;
    public String EmailBcc;
    public String EmailSubject;
    public String EmailBody;

    public String Var1;
    public String Var2;
    public String Var3;
    public String Var4;
    public String Var5;


    public String getVar4() {
        return Var4 == null ? "" : Var4;
    }

    public void setVar4(String var4) {
        Var4 = var4;
    }

    public String getVar5() {
        return Var5 == null ? "" : Var5;
    }

    public void setVar5(String var5) {
        Var5 = var5;
    }


    public String getVar1() {

        return Var1 == null ? "": Var1;
    }

    public void setVar1(String var1) {
        Var1 = var1;
    }

    public String getVar2() {
        return Var2 == null ? "" : Var2;
    }

    public void setVar2(String var2) {
        Var2 = var2;
    }

    public String getVar3() {
        return Var3 == null ? "" : Var3;
    }

    public void setVar3(String var3) {
        Var3 = var3;
    }

	
	public static Map<DayOfWeek,Integer> initializeWeekdayOffsetMap() {
		HashMap<DayOfWeek,Integer> resultMap = new HashMap<DayOfWeek,Integer>();
		
		resultMap.put(DayOfWeek.Sunday, 1);
		resultMap.put(DayOfWeek.Monday, 2);
		resultMap.put(DayOfWeek.Tuesday, 3);
		resultMap.put(DayOfWeek.Wednesday, 4);
		resultMap.put(DayOfWeek.Thursday, 5);
		resultMap.put(DayOfWeek.Friday, 6);
		resultMap.put(DayOfWeek.Saturday, 7);
		
		return resultMap;
	}
	
	public static Map<String,DayOfWeek> initializeDayOfWeekMap() {
		HashMap<String,DayOfWeek> resultMap = new HashMap<String,DayOfWeek>();
		
		resultMap.put("SUNDAY", DayOfWeek.Sunday);
		resultMap.put("MONDAY", DayOfWeek.Monday);
		resultMap.put("TUESDAY", DayOfWeek.Tuesday);
		resultMap.put("WEDNESDAY", DayOfWeek.Wednesday);
		resultMap.put("THURSDAY", DayOfWeek.Thursday);
		resultMap.put("FRIDAY", DayOfWeek.Friday);
		resultMap.put("SATURDAY", DayOfWeek.Saturday);

		resultMap.put("SUN", DayOfWeek.Sunday);
		resultMap.put("MON", DayOfWeek.Monday);
		resultMap.put("TUE", DayOfWeek.Tuesday);
		resultMap.put("WED", DayOfWeek.Wednesday);
		resultMap.put("THU", DayOfWeek.Thursday);
		resultMap.put("FRI", DayOfWeek.Friday);
		resultMap.put("SAT", DayOfWeek.Saturday);
		
		return resultMap;
	}
	
	public static DayOfWeek parseDayOfWeek(String literal) {
		if (literal != null) {
			return weekdayMap.get(literal.toUpperCase());
		} else {
			return null;
		}
	}
	
	/**
	 * Based on Last Scheduled Date (or NULL if never executed), return the next Schedule Date based on the
	 * Schedule Type, Recurrence Type and Recurrence.
	 * 
	 * @param lastScheduleDate
	 * @return next execution date.
	 */
	public Date getNextScheduleDate(Date lastScheduleDate) {
		Calendar startCalendarDate = Calendar.getInstance();
		Calendar endCalendarDate = Calendar.getInstance();
		Calendar currentCalendarDate = Calendar.getInstance();
		Calendar lastScheduleCalendarDate = Calendar.getInstance();


		if (lastScheduleDate != null) {
			lastScheduleCalendarDate.setTime(lastScheduleDate);
		} else {
			lastScheduleCalendarDate.setTime(startDate);
		}
		
		if (isDebug) {
			System.out.println("Running in Debug Mode");
		}

		log.info("startDate =" + startDate);
		startCalendarDate.setTime(startDate);
		
		if (endDate == null) {
			endCalendarDate = null;
		} else {
			endCalendarDate.setTime(endDate);
		}
		
		if (currentCalendarDate.after(startCalendarDate) && (endDate == null || currentCalendarDate.before(endCalendarDate))) {
			if (isDebug) {
				System.out.println("#0");
			}
			
			if (lastScheduleDate == null) {
				if (isDebug) {
					System.out.println("#1");
				}
				
				return currentCalendarDate.getTime();
			} else {
				if (isDebug) {
					System.out.println("have LastScheduleDate");
				}
				
				if (scheduleType == ScheduleType.OneTime) {
					// If it's a one time schedule and last schedule date is already stamped then is no longer time to run this schedule
					if (isDebug) {
						System.out.println("#2");
					}

					return Calendar.getInstance().getTime();

					//return null;
					//TODO: OPA BUG SCHEDULE ONETIME and Status Completed.

				} else if (scheduleType == ScheduleType.Recurrent) {
					// Recurrent Schedule Type
					List<Integer> recurrenceOffsetList = expandRecurrenceOffset(recurrence, recurrenceType);
					
					if (isDebug) {
						System.out.println("#3");
					}
					
					if (isDebug) {
						System.out.println("recurrenceOffsetList = " + recurrenceOffsetList);
					}
					
					if (recurrenceOffsetList.isEmpty() == false) {
						for (Integer offset : recurrenceOffsetList) {
							Calendar nextEvent = Calendar.getInstance();
							int field;
							int value;
							
							nextEvent.setTime(lastScheduleDate);
							
							// Adjust with the Preferred Start Date/Time
							switch (recurrenceType) {
								case Second:
									break;
									
								case Minute:
									nextEvent.set(Calendar.SECOND, startCalendarDate.get(Calendar.SECOND));
									break;
									
								case Hour:
									nextEvent.set(Calendar.MINUTE, startCalendarDate.get(Calendar.MINUTE));
									nextEvent.set(Calendar.SECOND, startCalendarDate.get(Calendar.SECOND));
									break;
									
								case Weekday:
									nextEvent.set(Calendar.HOUR, startCalendarDate.get(Calendar.HOUR_OF_DAY));
									nextEvent.set(Calendar.MINUTE, startCalendarDate.get(Calendar.MINUTE));
									nextEvent.set(Calendar.SECOND, startCalendarDate.get(Calendar.SECOND));
									break;
									
								case Day:
									nextEvent.set(Calendar.HOUR_OF_DAY, startCalendarDate.get(Calendar.HOUR_OF_DAY));
									nextEvent.set(Calendar.MINUTE, startCalendarDate.get(Calendar.MINUTE));
									nextEvent.set(Calendar.SECOND, startCalendarDate.get(Calendar.SECOND));
									break;
									
								case Month:
									nextEvent.set(Calendar.DAY_OF_MONTH, startCalendarDate.get(Calendar.DAY_OF_MONTH));
									nextEvent.set(Calendar.HOUR_OF_DAY, startCalendarDate.get(Calendar.HOUR_OF_DAY));
									nextEvent.set(Calendar.MINUTE, startCalendarDate.get(Calendar.MINUTE));
									nextEvent.set(Calendar.SECOND, startCalendarDate.get(Calendar.SECOND));
									break;
									
								case Year:
									nextEvent.set(Calendar.MONTH, startCalendarDate.get(Calendar.MONTH));
									nextEvent.set(Calendar.DAY_OF_MONTH, startCalendarDate.get(Calendar.DAY_OF_MONTH));
									nextEvent.set(Calendar.HOUR_OF_DAY, startCalendarDate.get(Calendar.HOUR_OF_DAY));
									nextEvent.set(Calendar.MINUTE, startCalendarDate.get(Calendar.MINUTE));
									nextEvent.set(Calendar.SECOND, startCalendarDate.get(Calendar.SECOND));
									break;
							}
							
							if (isDebug) {
								System.out.println("nextEvent set to " + nextEvent.getTime());
							}
							
							field = recurrenceType.getCalendarField();
							value = nextEvent.get(field);
							
							if (isDebug) {
								System.out.println("field=" + field);
							}

							if (isDebug) {
								System.out.println("offset=" + offset + ", value=" + value);
							}
							
							if (offset > value) {
								if (isDebug) {
									System.out.println("#3.1");
								}
								
								nextEvent.set(field, offset);
								
								if (isDebug) {
									System.out.println("nextEvent set to " + nextEvent.getTime());
									
									System.out.println("nextEvent = " + nextEvent.getTime() + ", currentDate = " + currentCalendarDate.getTime());
									System.out.println("nextEvent equals lastScheduleDate = " + nextEvent.equals(lastScheduleCalendarDate));
									System.out.println("nextEvent after lastScheduleDate = " + nextEvent.after(lastScheduleCalendarDate));
									System.out.println("nextEvent after currentDate = " + nextEvent.after(currentCalendarDate));
								}
								
								if (nextEvent.equals(lastScheduleCalendarDate) || nextEvent.after(lastScheduleCalendarDate)) {									
									if (nextEvent.after(currentCalendarDate) == true) {
										return nextEvent.getTime();
									} else {
										return currentCalendarDate.getTime();
									}
								}
							}
						}
						
						Calendar nextEvent = Calendar.getInstance();
						
						nextEvent.setTime(lastScheduleDate);
						
						if (isDebug) {
							System.out.println("nextEvent=" + nextEvent.getTime());
						}
						
						while (
								(endCalendarDate == null || endCalendarDate != null && nextEvent.before(endCalendarDate))
								&&
								nextEvent.before(currentCalendarDate)
						){
							if (isDebug) {
								System.out.println("nextEvent=" + nextEvent.getTime());
							}
						
							for (Integer offset : recurrenceOffsetList) {
								int field;
								int value;
								
								//nextEvent.setTime(lastScheduleDate);
								
								if (isDebug) {
									System.out.println("nextEvent set to " + nextEvent.getTime());
								}
								
								field = recurrenceType.getCalendarField();
								value = nextEvent.get(field);
	
								if (isDebug) {
									System.out.println("offset=" + offset + ", value=" + value);
								}
								
								if (offset > value) {
									if (isDebug) {
										System.out.println("#3.1");
									}
									
									nextEvent.set(field, offset);
									
									if (isDebug) {
										System.out.println("nextEvent set to " + nextEvent.getTime());
									}
								} else {
									if (isDebug) {
										System.out.println("#3.2");
									}
									
									// Need to increment 
									switch (recurrenceType) {
										case Second:
											if (isDebug) {
												System.out.println("Increment 1 minute");
											}
											
											// Minute increment
											nextEvent.add(Calendar.MINUTE, 1);
											nextEvent.set(field, offset);
											break;
											
										case Minute:
											if (isDebug) {
												System.out.println("Increment 1 hour");
											}
											
											// Hour increment
											nextEvent.add(Calendar.HOUR_OF_DAY, 1);
											nextEvent.set(field, offset);
											break;
											
										case Hour:
											if (isDebug) {
												System.out.println("Increment 1 day");
											}
											
											nextEvent.add(Calendar.DAY_OF_MONTH, 1);
											
											if (isDebug) {
												System.out.println("nextEvent set to " + nextEvent.getTime());
											}
											
											nextEvent.set(field, offset);
											
											if (isDebug) {
												System.out.println("nextEvent set to " + nextEvent.getTime());
											}
											
											break;
											
										case Day:
											if (isDebug) {
												System.out.println("Increment 1 month");
											}
											
											nextEvent.add(Calendar.MONTH, 1);
											nextEvent.set(field, offset);
											break;
											
										case Weekday:
											if (isDebug) {
												System.out.println("Increment 1 week");
											}
											
											nextEvent.add(Calendar.WEEK_OF_MONTH, 1);
											nextEvent.set(field, offset);
											break;
											
										case Month:
											if (isDebug) {
												System.out.println("Increment 1 year");
											}
											
											nextEvent.add(Calendar.YEAR, 1);
											nextEvent.set(field, offset);
											break;
											
										case Year:
											if (isDebug) {
												System.out.println("Increment " + offset + " years");
											}
											
											nextEvent.add(Calendar.YEAR, offset);
											break;
									}
								}
								
								if (isDebug) {
									System.out.println("nextEvent = " + nextEvent.getTime() + ", currentDate = " + currentCalendarDate.getTime());
									System.out.println("nextEvent equals currentDate = " + nextEvent.equals(currentCalendarDate));
									System.out.println("nextEvent equals lastScheduleDate = " + nextEvent.equals(lastScheduleCalendarDate));
									System.out.println("nextEvent after lastScheduleDate = " + nextEvent.after(lastScheduleCalendarDate));
								}	
								
								if (nextEvent.equals(lastScheduleCalendarDate) || nextEvent.after(lastScheduleCalendarDate)) {
									if (nextEvent.after(currentCalendarDate) == true) {
										return nextEvent.getTime();
									} else {
										return currentCalendarDate.getTime();
									}
								}
							}
						}
					}
				}
			}
		}
		
		if (isDebug) {
			System.out.println("#4");
		}
		
		return null;
	}
		
	public static List<Integer> expandRecurrenceOffset(String recurrence, RecurrenceType recurrenceType) {
		List<Integer> resultList = new Vector<Integer>();
		
		//System.out.println("expandRecurrenceOffset(recurrence=" + recurrence + ", recurrenceType=" + recurrenceType.name());
		
		if (recurrence == null) {
			return resultList;
		}
		
		if (recurrence.trim().equals("*")) {
			Integer start = 0;
			Integer end = -1;

			switch (recurrenceType) {
				case Second:
				case Minute:
					end = 60; 
					break;
					
				case Hour:
					end = 24;
					break;
					
				case Day:
					start = 1;
					end = 31;
					break;
					
				case Weekday:
					start = 1;
					end = 7;
					break;
					
				case Month:
					start = 1;
					end = 12;
					break;
					
				case Year:
					start = 1;
					end = 10;
					break;
			}
			
			if (end != -1) {
				Integer index;
				
				index = start;
				
				while (index <= end) {
					resultList.add(fixCalendarValue(recurrenceType, index));
					
					index++;
				}
			}
		} else {
			String[] recurrenceToken = recurrence.trim().split(",");
			
			if (recurrenceToken != null) {
				Pattern pattern = Pattern.compile("([^-]+)-([^-]+)");
				
				for (String recurrenceValue : recurrenceToken) {
					Integer start = 1;
					Integer end = -1;
					Matcher matcher = pattern.matcher(recurrenceValue);
					
					if (matcher.matches()) {
						// A - B
						String from = matcher.group(1);
						String to = matcher.group(2);
						
						if (recurrenceType == RecurrenceType.Weekday) {
							DayOfWeek fromWeekday = weekdayMap.get(from);
							DayOfWeek toWeekday = weekdayMap.get(to);
							
							if (fromWeekday != null && toWeekday != null) {
								Integer fromWeekdayOffset = weekdayOffsetMap.get(fromWeekday);
								Integer toWeekdayOffset = weekdayOffsetMap.get(toWeekday);
								
								start = fromWeekdayOffset;
								end = toWeekdayOffset;
								
								if (end < start) {
									throw new RuntimeException("Error parsing recurrence of \'" + recurrenceToken + "\'");
								}
							} else {
								throw new RuntimeException("Error parsing recurrence of \'" + recurrenceToken + "\'");
							}
						} else {
							start = Integer.valueOf(from);
							end = Integer.valueOf(to);
							
							if (end < start) {
								throw new RuntimeException("Error parsing recurrence of \'" + recurrenceToken + "\'");
							}
							
							checkRecurrenceValue(recurrenceType, start);
							checkRecurrenceValue(recurrenceType, end);
						}
						
						if (end != -1) {
							Integer index;
							
							index = start;
							
							while (index <= end) {
								resultList.add(fixCalendarValue(recurrenceType, index));
								
								index++;
							}
						}
					} else {
						// Normal Case
						if (recurrenceType == RecurrenceType.Weekday) {
							DayOfWeek dow = weekdayMap.get(recurrenceValue);
							Integer odow = weekdayOffsetMap.get(dow);
							
							resultList.add(odow);
						} else {
							Integer value = Integer.valueOf(recurrenceValue);
							
							checkRecurrenceValue(recurrenceType, value);
								
							resultList.add(fixCalendarValue(recurrenceType, Integer.valueOf(recurrenceValue)));
						}
					}
				}
			}
		}
		
		Collections.sort(resultList);
		
		return resultList;
	}
	
	private static Integer fixCalendarValue(RecurrenceType recurrenceType, Integer value) {
		if (recurrenceType == RecurrenceType.Month) {
			return value - 1;
		} else {
			return value;
		}
	}
	
	private static void checkRecurrenceValue(RecurrenceType recurrenceType, Integer value) {
		switch (recurrenceType) {
			case Second:
			case Minute:
				
				if (value < 0 || value > 59) {
					throw new RuntimeException("Error parsing recurrence of \'" + value + "\' for Recurrence Type " + recurrenceType.name());
				}
				
				break;
				
			case Hour:
				if (value < 0 || value > 23) {
					throw new RuntimeException("Error parsing recurrence of \'" + value + "\' for Recurrence Type " + recurrenceType.name());
				}
				
				break;
				
			case Day:
				if (value < 1 || value > 31) {
					throw new RuntimeException("Error parsing recurrence of \'" + value + "\' for Recurrence Type " + recurrenceType.name());
				}
				
				break;
				
			case Weekday:
				if (value < 1 || value > 7) {
					throw new RuntimeException("Error parsing recurrence of \'" + value + "\' for Recurrence Type " + recurrenceType.name());
				}
				
				break;
				
			case Month:
				if (value < 1 || value > 12) {
					throw new RuntimeException("Error parsing recurrence of \'" + value + "\' for Recurrence Type " + recurrenceType.name());
				}
				
				break;
				
			case Year:
				if (value < 1) {
					throw new RuntimeException("Error parsing recurrence of \'" + value + "\' for Recurrence Type " + recurrenceType.name());
				}
				
				break;
		}
	}
}
