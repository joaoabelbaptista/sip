package com.vrs.sip.util;

import com.vrs.sip.FileLog;
import com.vrs.sip.Util;
import com.vrs.sip.task.Schedule;

public class Helper {

    private static FileLog log = FileLog.getNewInstance(Helper.class, "helper_" + Util.getSimpleUniqueId(), ".log");

    public static String parsingEntityName(String entityNameSrc, Schedule schedule){

        log.info("schedule baba = "+ schedule);

        String entityName = new String(entityNameSrc);

        if(schedule != null) {

            String var1 = schedule.getVar1();
            String var2 = schedule.getVar2();
            String var3 = schedule.getVar3();
            String var4 = schedule.getVar4();
            String var5 = schedule.getVar5();

            log.debug(" var1 = " + var1);

            log.info(" var2 = " + var2);

            log.debug(" var3 = " + var3);

            log.debug(" var4 = " + var4);

            log.debug(" var5 = " + var5);


            if (!var1.equalsIgnoreCase(""))
                entityName = entityName.replace("@var1@", var1);

            if (!var2.equalsIgnoreCase(""))
                entityName = entityName.replace("@var2@", var2);

            if (!var3.equalsIgnoreCase(""))
                entityName = entityName.replace("@var3@", var3);

            if (!var4.equalsIgnoreCase(""))
                entityName = entityName.replace("@var4@", var4);

            if (!var5.equalsIgnoreCase(""))
                entityName = entityName.replace("@var5@", var5);


            log.info("---------------------------------------------------------------");
            log.info(" var2 = " + var2);

        }


        log.info("---------------------------------------------------------------");

        log.info(entityName);

        log.info("---------------------------------------------------------------");

        return entityName;
    }
}
