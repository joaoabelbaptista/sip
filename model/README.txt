package.xml contains the XML to extract/upload the object types we need by SIP.

We are looking for CustomObjects, CustomTabs, CustomApplications, PageLayouts and PermissionSets.

PermissionSet is a bit difficult to export from one env to another if the org's are not exactly the same.
As such I have normalized the SIP_Admin permission set (XML) to only include the relevant objects.

If new fields are added to SIP model then this permission set XML needs to be updated and included in the baseline deployment ZIP for the SIP application.

