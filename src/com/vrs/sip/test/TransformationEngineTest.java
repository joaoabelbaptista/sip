package com.vrs.sip.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.vrs.sip.connection.drivers.SalesforceStatement;
import com.vrs.sip.task.TransformationEngine;

public class TransformationEngineTest {

	public static void main(String[] args) {
		try {
			testDecode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			testDateParse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testDateParse() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SZ");
		String var1 = "2007-12-01 00:00:00.0";
		String var2 = "2007-06-01 00:00:00.0";
		
		Object result1 = TransformationEngine.dateParse(var1, "yyyy-MM-dd HH:mm:ss.S");
		Object result2 = TransformationEngine.dateParse(var2, "yyyy-MM-dd HH:mm:ss.S");
		
		java.util.Date dt1 = (java.util.Date)result1;
		java.util.Date dt2 = (java.util.Date)result2;
		
		java.util.GregorianCalendar gcresult1 = SalesforceStatement.getGregorianCalendarDate(dt1);
		java.util.GregorianCalendar gcresult2 = SalesforceStatement.getGregorianCalendarDate(dt2);
		
		System.out.println("testDateParse: result1=" + result1 + ", var1=" + var1 + ", gc1=" + sdf.format(gcresult1.getTime()) + " [" + gcresult1 + "], dt1=" + sdf.format(dt1));
		System.out.println("testDateParse: result2=" + result2 + ", var2=" + var2 + ", gc2=" + sdf.format(gcresult2.getTime()) + " [" + gcresult2 + "], dt2=" + sdf.format(dt2));
	}
	
	public static void testDecode() {
		String var1 = "VIRA";
		String var2 = "AAAA";
		String var3 = "BBBB";
		String var4 = null;
		String var5 = "Hello";
		String var6 = "BBBB";
		
		Object result1 = TransformationEngine.decode(var1, "AAAA", "T-AAAA", "BBBB", "T-BBBB", var1);
		Object result2 = TransformationEngine.decode(var2, "AAAA", "T-AAAA", "BBBB", "T-BBBB", var2);
		Object result3 = TransformationEngine.decode(var3, "AAAA", "T-AAAA", "BBBB", "T-BBBB", var3);
		Object result4 = TransformationEngine.decode(var4, "AAAA", "T-AAAA", "BBBB", "T-BBBB", var4);
		Object result5 = TransformationEngine.decode(var5, "AAAA", "T-AAAA", "BBBB", "T-BBBB");
		Object result6 = TransformationEngine.decode(var6, "AAAA", "T-AAAA", "BBBB", "T-BBBB");
		
		System.out.println("testDecode: result1=" + result1);
		if (result1 != var1) {
			System.out.println("\tError");
		} else {
			System.out.println("\tSuccess");
		}
		
		System.out.println("testDecode: result2=" + result2);
		if (result2.equals("T-AAAA") == false) {
			System.out.println("\tError");
		} else {
			System.out.println("\tSuccess");
		}
		
		System.out.println("testDecode: result3=" + result3);
		if (result3.equals("T-BBBB") == false) {
			System.out.println("\tError");
		} else {
			System.out.println("\tSuccess");
		}
		
		System.out.println("testDecode: result4=" + result4);
		if (result4 != null) {
			System.out.println("\tError");
		} else {
			System.out.println("\tSuccess");
		}
		
		System.out.println("testDecode: result5=" + result5);
		if (result5 != null) {
			System.out.println("\tError");
		} else {
			System.out.println("\tSuccess");
		}
		
		System.out.println("testDecode: result6=" + result6);
		if (result6 != "T-BBBB") {
			System.out.println("\tError");
		} else {
			System.out.println("\tSuccess");
		}
	}

}
