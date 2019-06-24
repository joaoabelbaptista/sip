package com.vrs.sip.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

public class TransformationEngine {
	public static JexlBuilder jexlBuilder;
	public static JexlEngine jexl;
	
	/**
	 * And operator.
	 * 
	 * @param args
	 * @return
	 */
	public static Boolean and(Boolean... args) {
		Boolean result = true;
		
		for (Boolean arg : args) {
			if (! arg) {
				result = false;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Or operator.
	 * 
	 * @param args
	 * @return
	 */
	public static Boolean or(Boolean... args) {
		Boolean result = false;
		
		for (Boolean arg : args) {
			if (arg) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * If statement.
	 * 
	 * @param checkValue
	 * @param valueIfTrue
	 * @param valueIfFalse
	 * @return
	 */
	public static Object iif(Boolean checkValue, Object valueIfTrue, Object valueIfFalse) {
		Object result = checkValue ? valueIfTrue : valueIfFalse;
		
		return result;
	}
	
	/**
	 * Check if arg is NULL.
	 * 
	 * @param arg
	 * @return
	 */
	public static Boolean isNull(Object arg) {
		return (arg == null);
	}
	
	/**
	 * Check if arg is blank.
	 * 
	 * @param arg
	 * @return
	 */
	public static Boolean isBlank(String arg) {
		if (arg != null && arg.length() == 0) {
			return true;
		} else if (arg == null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the current Timestamp.
	 * 
	 * @return
	 */
	public static Date getCurrentTimestamp() {
		return new Date();
	}
	
	/**
	 * Decode method.
	 * 
	 * @param args
	 * @return
	 */
	public static Object decode(Object... args) {
		Object result = null;
		Object checkValue = null;
		Object defaultValue = null;
		Boolean isMatched = false;
		Boolean hasDefaultValue = false;
		int totalMatchValues = 0;
		
		if (args.length < 3) {
			throw new RuntimeException("Usage: decode(checkValue, matchValue1, outputValue1, matchValue2, outputValue2, ...[, defaultValue]");
		}
		
		if (((args.length - 1) % 2) != 0) {
			// Default value provided
			hasDefaultValue = true;
			defaultValue = args[args.length - 1];
		}
		
		checkValue = args[0];
		
		totalMatchValues = (int)((args.length - 1) / 2);
	
		for (int i = 0; i < totalMatchValues; i++) {
			Object matchValue = args[1 + i * 2];
			Object outputValue = args[1 + i * 2 + 1];
			
			if (checkValue == null) {
				if (matchValue == null) {
					result = outputValue;
					isMatched = true;
					break;
				}
			} else {
				if (matchValue != null) {
					if (checkValue.equals(matchValue)) {
						result = outputValue;
						isMatched = true;
						break;
					}
				}
			}
		}
		
		if (isMatched == false && hasDefaultValue) {
			result = defaultValue;
		}
		
		return result;
	}
		
	/**
	 * Return the Data formated using the inFormat parameter (follows the pattern of SimpleDateFormat).
	 * 
	 * @param inDate
	 * @param inFormat
	 * @return
	 */
	public static String dateFormat(Date inDate, String inFormat) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inFormat);
		String result = null;
		
		if (inDate != null) {
			result = simpleDateFormat.format(inDate);
		}
		
		return result;
	}
	
	/**
	 * Return the dateString value parsed to Date using the specified dateFormat (follows the pattern of SimpleDateFormat).
	 * @param dateString
	 * @param dateFormat
	 * @return
	 * @throws ParseException
	 */
	public static Date dateParse(String dateString, String dateFormat) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		Date result = null;
		
		if (dateString != null && dateString.isEmpty() == false) {
			result = simpleDateFormat.parse(dateString); 
		}
		
		return result;
	}
	
	/**
	 * If inObj is NULL return inOtherObj, else return inObj.
	 * 
	 * @param inObj
	 * @param inOtherObj
	 * @return
	 */
	public static Object nvl(Object inObj, Object inOtherObj) {
		return inObj != null ? inObj : inOtherObj;
	}
	
	/**
	 * Return the basename of a file.
	 * 
	 * @param inName
	 * @return
	 */
	public static String basename(String inName) {
		String result;
		
		if (inName != null && inName.contains("/")) {
			int lastIndexOfSeparator = inName.lastIndexOf("/");
			
			result = inName.substring(lastIndexOfSeparator + 1);
		} else {
			result = inName;
		}
		
		return result;
	}
	
	/**
	 * Return the dirname of a file.
	 * 
	 * @param inName
	 * @return
	 */
	public static String dirname(String inName) {
		String result;
		
		if (inName != null && inName.contains("/")) {
			int lastIndexOfSeparator = inName.lastIndexOf("/");
			
			if (lastIndexOfSeparator > 0) {
				result = inName.substring(0, lastIndexOfSeparator);
			} else {
				result = "/";
			}
		} else if (inName != null) {
			result = ".";
		} else {
			result = null;
		}
		
		return result;
	}
	
	/** ------------------------------------------------------------------------------ **/
	
	/** Support Methods of the Engine **/
	
	public static JexlEngine getJexl() {
		if (jexlBuilder == null) {
			Map<String,Object> namespaces = new HashMap<String,Object>();
			
			namespaces.put("util", TransformationEngine.class);
			
			// Strict = FALSE - Allow Null Variables
			jexlBuilder = new JexlBuilder().cache(512).strict(false).silent(false);
			
			jexlBuilder.namespaces(namespaces);
		}
		
		if (jexl == null) {
			jexl = jexlBuilder.create();
		}
		
		return jexl;
	}
	
	public static JexlContext getNewContext() {
		return new MapContext();
	}
	
	public static Boolean evaluateBoolean(JexlExpression expression, JexlContext context) {
		return (Boolean)expression.evaluate(context);
	}
	
	public static Date evaluateDate(JexlExpression expression, JexlContext context) {
		Object evaluateResult = expression.evaluate(context);
		Date result = null;
		
		if (evaluateResult != null) {
			if (evaluateResult instanceof String) {
				if (evaluateResult != null && ((String)evaluateResult).isEmpty() == false) {
					result = (Date)evaluateResult;
				}
			} else if (evaluateResult instanceof Date) {
				result = (Date)evaluateResult;
			} else {
				result = (Date)evaluateResult;
			}
		}
		
		return result;
	}
	
	public static Double evaluateDouble(JexlExpression expression, JexlContext context) {
		return (Double)expression.evaluate(context);
	}
	
	public static Integer evaluateInteger(JexlExpression expression, JexlContext context) {
		return (Integer)expression.evaluate(context);
	}
	
	public static String evaluateString(JexlExpression expression, JexlContext context) {
		return (String)expression.evaluate(context);
	}
}
