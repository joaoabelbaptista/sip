/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Transformation.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

public class Transformation implements Comparable<Transformation> {
	public String transformationId;
	public String transformationName;
	public String targetFieldName;
	public String transformation;
	public Integer order;
	
	@Override
	public int compareTo(Transformation o) {
		if (o == null) {
			return 1;
		}
		
		if (order == null) {
			return -1;
		}
		
		return order.compareTo(o.order);
	}
}
