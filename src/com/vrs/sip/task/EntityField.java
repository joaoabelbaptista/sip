/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Entity Field.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

public class EntityField implements Comparable<EntityField> {
	public enum EntityFieldType {
		Boolean, Date, Decimal, Integer, String
	};
	
	public String entityFieldId;
	public String entityFieldName;
	public EntityFieldType entityFieldType;
	public Integer order;
	
	@Override
	public int compareTo(EntityField o) {
		return order.compareTo(o.order);
	}
}
