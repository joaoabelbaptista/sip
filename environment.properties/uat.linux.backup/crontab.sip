# SIP Crontab
#
#       Created: 2016-09-28, 09:19
# Last Modified: 2016-09-28, 09:19
#
CRON_TZ=Europe/London
0 2 * * * $HOME/bin/sip -task clarity_migrations_BI >/dev/null 2>&1
# TODO: IOT Sync
0 0 * * * $HOME/bin/sip -task COL_Report >/dev/null 2>&1
30 0 * * * $HOME/bin/sip -task COL_Report_Validate >/dev/null 2>&1
0 1 * * * $HOME/bin/sip -task footprint_Full >/dev/null 2>&1
10 1 * * * $HOME/bin/sip -task GLL_Report >/dev/null 2>&1
20 1 * * * $HOME/bin/sip -task hub_connections_Full >/dev/null 2>&1
30 1 * * * $HOME/bin/sip -task IOT_Status_Report >/dev/null 2>&1
40 1 * * * $HOME/bin/sip -task service_availability_Full >/dev/null 2>&1
#0 * * * * $HOME/bin/remote.get >/dev/null 2>&1

