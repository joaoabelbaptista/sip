package com.vrs.sip.test;

import com.vrs.sip.Factory;
import com.vrs.sip.Metadata;
import com.vrs.sip.Setup;

public class SetupTest {
	String orgId;
	Setup setup;
	
	String applicationSetupFilename;
	
	public static void main(String[] args) throws Exception {
		SetupTest setupTest = new SetupTest();
		
		setupTest.doTestGetApplicationSetup();
	}
	
	public void doTestGetApplicationSetup() throws Exception {
		Metadata metadata = Factory.getMetadataInstance();
		
		orgId = metadata.getOrgId();
		
		setup = new Setup();

		applicationSetupFilename = setup.serverConfiguration.modelDirectory + "/" + orgId + ".zip";
		
		setup.getApplicationSetup(orgId);
	}
}
