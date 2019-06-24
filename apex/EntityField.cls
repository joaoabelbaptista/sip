String entityUniqueID = 'VFGE_IOT_SYNC_IMPORT_FLAT_DISCOUNT_PERIODS_EXTRACT_SOURCE';
SIP_Entity__c entity;
Id entityId;
Integer order;
List<String> fieldList = new List<String>();
List<SIP_Entity_Field__c> entityFieldInsert = new List<SIP_Entity_Field__c>();

fieldList.add('PERIOD_INDEX');
fieldList.add('DEAL_INDEX');
fieldList.add('DEAL_NAME');
fieldList.add('NEGOTIATION_TEAM');
fieldList.add('DEAL_TYPE');
fieldList.add('VRS_DEAL_STATUS');
fieldList.add('VRS_DISC_CONT_STATUS');
fieldList.add('ACTIVE');
fieldList.add('PERIOD_FROM');
fieldList.add('PERIOD_TO');
fieldList.add('TAX_DISC_RATES');
fieldList.add('OB_TAX_RATE');
fieldList.add('EMBEDDED_DERIVATIVES');
fieldList.add('VOICE_MO_DISCOUNTS_INCL');
fieldList.add('VOICE_MT_DISCOUNTS_INCL');
fieldList.add('SMS_DISCOUNTS_INCL');
fieldList.add('DATA_DISCOUNTS_INCL');
fieldList.add('COND_VOICE_MO_DISCOUNTS');
fieldList.add('COND_VOICE_MT_DISCOUNTS');
fieldList.add('IB_VOICE_TRAFFIC_COMM');
fieldList.add('DEFINITION_OF_MIN');
fieldList.add('OB_VOICE_TRAFFIC_COMM');
fieldList.add('COND_SMS_DISCOUNTS');
fieldList.add('IB_SMS_TRAFFIC_COMM');
fieldList.add('OB_SMS_TRAFFIC_COMM');
fieldList.add('COND_DATA_DISCOUNTS');
fieldList.add('IB_DATA_TRAFFIC_COMM');
fieldList.add('DEFINITION_OF_MB');
fieldList.add('OB_DATA_TRAFFIC_COMM');
fieldList.add('MO_INIT_IB_TG_LY_NO_DEAL');
fieldList.add('MO_INIT_IB_TG_LY_WITH_DEAL');
fieldList.add('VOICE_MO_INIT_IB_DISC_TYPE');
fieldList.add('MO_INIT_IB_ACT_MIN_PERC_DISC');
fieldList.add('MO_INIT_OB_TG_COUNTRY_LY');
fieldList.add('MO_INIT_OB_MS_3RD_WITH_DEAL');
fieldList.add('MO_INIT_OB_MS_3RD_WO_DEAL');
fieldList.add('VOICE_MO_INIT_OB_DISC_TYPE');
fieldList.add('MO_INIT_OB_ACT_MIN_EXCL_TAXES');
fieldList.add('MO_INIT_OB_ACT_MIN_INCL_TAXES');
fieldList.add('MT_INIT_IB_TR_VS_MO_IB');
fieldList.add('VOICE_MT_INIT_IB_DISC_TYPE');
fieldList.add('MT_INIT_IB_ACT_MIN_PERC_DISC');
fieldList.add('MT_INIT_OB_TR_VS_MO_OB');
fieldList.add('VOICE_MT_INIT_OB_DISC_TYPE');
fieldList.add('MT_INIT_OB_ACT_MIN_EXCL_TAXES');
fieldList.add('MT_INIT_OB_ACT_MIN_INCL_TAXES');
fieldList.add('SMS_INIT_TR_VS_MO_IB');
fieldList.add('SMS_INIT_IB_DISC_TYPE');
fieldList.add('SMS_INIT_IB_SMS_PERC_DISC');
fieldList.add('SMS_INIT_OB_TR_VS_MO_OB');
fieldList.add('SMS_INIT_OB_DISC_TYPE');
fieldList.add('SMS_INIT_OB_SMS_EXCL_TAXES');
fieldList.add('SMS_INIT_OB_SMS_INCL_TAXES');
fieldList.add('DATA_INIT_TR_VS_MO');
fieldList.add('DATA_INIT_IB_DISC_TYPE');
fieldList.add('DATA_INIT_IB_MB_PERC_DISC');
fieldList.add('DATA_INIT_OB_TR_VS_MO_OB');
fieldList.add('DATA_INIT_OB_DISC_TYPE');
fieldList.add('DATA_INIT_OB_MB_EXCL_TAXES');
fieldList.add('DATA_INIT_OB_MB_INCL_TAXES');
fieldList.add('VOICE_VAL_VS_DO_NOTHING');
fieldList.add('SMS_VAL_VS_DO_NOTHING');
fieldList.add('DATA_VAL_VS_DO_NOTHING');
fieldList.add('VOICE_VAL_VS_PREV_PERIOD');
fieldList.add('SMS_VAL_VS_PREV_PERIOD');
fieldList.add('DATA_VAL_VS_PREV_PERIOD');
fieldList.add('IB_CURRENCY');
fieldList.add('OB_CURRENCY');
fieldList.add('MO_UPDT_IB_TG_LY_NO_DEAL');
fieldList.add('MO_UPDT_IB_TG_LY_WITH_DEAL');
fieldList.add('VOICE_MO_UPDT_IB_DISC_TYPE');
fieldList.add('MO_UPDT_IB_ACT_MIN_PERC_DISC');
fieldList.add('MO_UPDT_OB_TG_LY');
fieldList.add('MO_UPDT_OB_MS_3RD_WITH_DEAL');
fieldList.add('MO_UPDT_OB_MS_3RD_WO_DEAL');
fieldList.add('VOICE_MO_UPDT_OB_DISC_TYPE');
fieldList.add('MO_UPDT_OB_ACT_MIN_EXCL_TAXES');
fieldList.add('MO_UPDT_OB_ACT_MIN_INCL_TAXES');
fieldList.add('MT_UPDT_IB_TR_VS_MO_IB');
fieldList.add('VOICE_MT_UPDT_IB_DISC_TYPE');
fieldList.add('MT_UPDT_IB_ACT_MIN_PERC_DISC');
fieldList.add('MT_UPDT_OB_TR_VS_MO_OB');
fieldList.add('VOICE_MT_UPDT_OB_DISC_TYPE');
fieldList.add('MT_UPDT_OB_ACT_MIN_EXCL_TAXES');
fieldList.add('MT_UPDT_OB_ACT_MIN_INCL_TAXES');
fieldList.add('SMS_UPDT_TR_VS_MO_IB');
fieldList.add('SMS_UPDT_IB_DISC_TYPE');
fieldList.add('SMS_UPDT_IB_SMS_PERC_DISC');
fieldList.add('SMS_UPDT_OB_TR_VS_MO_OB');
fieldList.add('SMS_UPDT_OB_DISC_TYPE');
fieldList.add('SMS_UPDT_OB_SMS_EXCL_TAXES');
fieldList.add('SMS_UPDT_OB_SMS_INCL_TAXES');
fieldList.add('DATA_UPDT_IB_TR_VS_MO_IB');
fieldList.add('DATA_UPDT_IB_DISC_TYPE');
fieldList.add('DATA_UPDT_IB_MB_PERC_DISC');
fieldList.add('DATA_UPDT_OB_TR_VS_MO_OB');
fieldList.add('DATA_UPDT_OB_DISC_TYPE');
fieldList.add('DATA_UPDT_OB_MB_EXCL_TAXES');
fieldList.add('DATA_UPDT_OB_MB_INCL_TAXES');
fieldList.add('MO_IB_ACT_MIN_DISC_VAR');
fieldList.add('MT_IB_ACT_MIN_DISC_VAR');
fieldList.add('SMS_IB_DISC_VAR');
fieldList.add('DATA_IB_DISC_VAR');
fieldList.add('MO_OB_ACT_MIN_DISC_VAR');
fieldList.add('MT_OB_ACT_MIN_DISC_VAR');
fieldList.add('SMS_OB_DISC_VAR');
fieldList.add('DATA_OB_DISC_VAR');
fieldList.add('IB_DISC_ON_TAP');
fieldList.add('DISC_CALCULATION');
fieldList.add('DISC_DOCUMENTATION');
fieldList.add('FRAUD_ADJUSTMENT_INCL');
fieldList.add('OB_DISC_ON_TAP');
fieldList.add('DISC_PERIOD_LENGTH');
fieldList.add('PLAN_SETTLEMENT_START_DT');
fieldList.add('PLAN_SETTLEMENT_END_DT');
fieldList.add('SETTLEMENT_STATUS');
fieldList.add('ACTUAL_SETTLEMENT_START_DT');
fieldList.add('ACTUAL_SETTLEMENT_END_DT');
fieldList.add('EXPT_DISC_PAYABLE');
fieldList.add('EXPT_DISC_PAYABLE_CURR');
fieldList.add('EXPT_DISC_RECEIVABLE');
fieldList.add('EXPT_DISC_RECEIVABLE_CURR');
fieldList.add('ACTUAL_DISC_PAYABLE');
fieldList.add('ACTUAL_DISC_PAYABLE_CURR');
fieldList.add('ACTUAL_DISC_RECEIVABLE');
fieldList.add('ACTUAL_DISC_RECEIVABLE_CURR');
fieldList.add('DAYS_REM_ON_DISC_PERIOD');
fieldList.add('LAST_UPDATED_DATE');

entity = [SELECT Id, Unique_ID__c FROM SIP_Entity__c WHERE Unique_ID__c = :entityUniqueID LIMIT 1];
entityId = entity.Id;

order = 0;
for (String fieldName : fieldList) {
	String fieldUniqueID = entityUniqueID + '_' + fieldName.toUpperCase();
	String fieldType = 'String';

	order++;

	entityFieldInsert.add(
		new SIP_Entity_Field__c(
			Entity__c = entityId,
			Field_Name__c = fieldName,
			Type__c = fieldType,
			Order__c = order,
			Unique_ID__c = fieldUniqueID
		)
	);
}

insert entityFieldInsert;

