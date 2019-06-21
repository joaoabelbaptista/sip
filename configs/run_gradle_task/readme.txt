gradle tasks --all
gradle -Penv=ci.sip_prod task jar
gradle -Penv=ci.sip_uat task jar

