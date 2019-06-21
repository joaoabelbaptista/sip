/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Type of Field.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

public enum FieldType {
	T_BOOLEAN(java.sql.Types.BOOLEAN), T_INTEGER(java.sql.Types.INTEGER), T_DECIMAL(java.sql.Types.DECIMAL), T_STRING(
			java.sql.Types.VARCHAR), T_DATE(java.sql.Types.DATE), T_DATE_WITHOUT_TIME(java.sql.Types.DATE),
	T_FILEATTACHMENT_LIST(java.io.OutputStream.class.hashCode()), T_FILE(java.io.InputStream.class.hashCode());

	private final Integer sqlType;

	FieldType(Integer sqlType) {
		this.sqlType = sqlType;
	}

	public Integer getSQLType() {
		return sqlType;
	}
}
