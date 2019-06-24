/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Application Setup.
 * History: aosantos, 2016-07-22, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.sforce.soap.metadata.*;
import com.vrs.sip.connection.drivers.SalesforceConnection;
import com.vrs.sip.task.TransformationEngine;

/**
 * Important Note: I have checked and using SFDC endpoint API with 22 the code from this class does'nt work.
 *                 I have tested with version 32 of API and it worked correctly.
 * 
 * 
 * @author aosantos
 *
 */

public class Setup {
	public Configuration.Server serverConfiguration;
	
	private static final String MANIFEST_FILE = "package.xml";
	private static final double API_VERSION = 32.0;
	
	// What is one second in milliseconds?
	private static final long ONE_SECOND_IN_MS = 1000;
	
	// Maximum attempts to deploy the ZIP file
	private static final int MAX_NUMBER_POLL_REQUESTS = 5; // seconds = 1,2,4,8,16,32,64,128,256,512
	
	// Identification of SIP components based on types
	private static Map<String, String> appComponents = initializeAppComponents();
	
	private MetadataConnection metadataConnection;
	
	public Setup() throws FileNotFoundException, IOException {
		Boolean isInstallation = true;
		
		Configuration.isInstall = isInstallation;
		
		Metadata metadata = Factory.getMetadataInstance(isInstallation);
		
		SalesforceConnection salesforceConnection = (SalesforceConnection)metadata.metadataConnection;
		
		metadataConnection = salesforceConnection.getSalesforceMetadataConnection();
		
		serverConfiguration = Configuration.getInstance().getServer();
	}
	
	/**
	 * Initialize the components that together compose the SIP application.
	 * 
	 * @return
	 * @throws Exception
	 */
	private static Map<String,String> initializeAppComponents() {
		Map<String,String> componentMap = new HashMap<String,String>();
		
		componentMap.put("", "package.xml");
		componentMap.put("applications", "SIP");
		componentMap.put("layouts", "SIP_");
		componentMap.put("objects", "SIP_");
		componentMap.put("tabs", "SIP_");
		componentMap.put("permissionsets", "SIP_");
		
		return componentMap;
	}
	
	/**
	 * Get the Application Setup from a Specific Org.
	 * @param orgId
	 * @throws Exception 
	 */
	public void getApplicationSetup(String orgId) throws Exception {
		RetrieveRequest retrieveRequest = new RetrieveRequest();
		AsyncResult asyncResult;
		RetrieveResult result;
		File resultsFile;
		FileOutputStream os;
		String targetArchiveFilename = serverConfiguration.modelDirectory + "/model_" + orgId + ".zip";
		
		retrieveRequest.setApiVersion(API_VERSION);
		setUnpackaged(retrieveRequest);
		
		asyncResult = metadataConnection.retrieve(retrieveRequest);
		result = waitForRetrieveCompletion(asyncResult);
		
		if (result.getStatus() == RetrieveStatus.Failed) {
			throw new Exception(result.getErrorStatusCode() + " msg: " + result.getErrorMessage());
		} else if (result.getStatus() == RetrieveStatus.Succeeded) {
			// Print out any warning messages
			StringBuilder stringBuilder = new StringBuilder();
			
			if (result.getMessages() != null) {
				for (RetrieveMessage rm : result.getMessages()) {
					stringBuilder.append(rm.getFileName() + " - " + rm.getProblem() + "\n");
				}
			}
			
			if (stringBuilder.length() > 0) {
				System.out.println("Retrieve warnings:\n" + stringBuilder);
			}
			
			System.out.println("Writing results to zip file '" + targetArchiveFilename + "'");
			
			resultsFile = new File(targetArchiveFilename);
			os = new FileOutputStream(resultsFile);
			
			try {
				os.write(result.getZipFile());
			} finally {
				os.close();
			}
			
			removeNonAppComponents(targetArchiveFilename);
		}
	}
	
	/**
	 * Deploy the ZIP file from the Org.
	 * 
	 * @param archiveFilename
	 * @throws Exception
	 */
	public void runApplicationSetup(String orgId) throws Exception {
		String archiveFilename = serverConfiguration.modelDirectory + "/model_" + orgId + ".zip";
		File archiveFile = new File(archiveFilename);
		byte zipBytes[];
		DeployOptions deployOptions = new DeployOptions();
		AsyncResult asyncResult;
		DeployResult result;
		
		if (! archiveFile.exists()) {
			throw new RuntimeException("File " + archiveFilename + " not found");
		}
		
		if (! archiveFile.isFile()) {
			throw new RuntimeException(archiveFilename + " is not a file");
		}
		
		zipBytes = readZipFile(archiveFilename);
		
		deployOptions.setPerformRetrieve(false);
		deployOptions.setRollbackOnError(true);
		
		asyncResult = metadataConnection.deploy(zipBytes, deployOptions);
		
		result = waitForDeployCompletion(asyncResult.getId());
		
		if (! result.isSuccess()) {
			printErrors(result, "Final list of failures:\n");
			
			throw new Exception("The files were not successfully deployed");
		} else {
			System.out.println("The file " + archiveFilename + " was successfully deployed\n");
		}
	}
	
	private DeployResult waitForDeployCompletion(String asyncResultId) throws Exception {
		int poll = 0;
		long waitTimeMillisecs = ONE_SECOND_IN_MS;
		DeployResult deployResult;
		boolean fetchDetails;
		
		do {
			Thread.sleep(waitTimeMillisecs);
			
			waitTimeMillisecs *= 2;
			
			if (poll++ > MAX_NUMBER_POLL_REQUESTS) {
				throw new Exception("Request timed out. If this is a large set of metadata components, ensure that MAX_NUMBER_POLL_REQUESTS is sufficient.");
			}
			
			fetchDetails = (poll %3 == 0);
			
			deployResult = metadataConnection.checkDeployStatus(asyncResultId, fetchDetails);
			
			System.out.println("Status is: " + deployResult.getStatus());
			
			if (! deployResult.isDone() && fetchDetails) {
				printErrors(deployResult, "Failures for deployment in progress:\n");
			}
		} while (! deployResult.isDone());
		
		if (! deployResult.isSuccess() && deployResult.getErrorStatusCode() != null) {
			throw new Exception(deployResult.getErrorStatusCode() + " msg: " + deployResult.getErrorMessage());
		}
		
		if (! fetchDetails) {
			deployResult = metadataConnection.checkDeployStatus(asyncResultId, true);
		}
		
		return deployResult;
	}
	
	private void printErrors(DeployResult result, String messageHeader) {
		DeployDetails details = result.getDetails();
		StringBuilder stringBuilder = new StringBuilder();
		
		if (details != null) {
			DeployMessage[] componentFailures = details.getComponentFailures();
			for (DeployMessage failure : componentFailures) {
				String loc = "(" + failure.getLineNumber() + ", " + failure.getColumnNumber();
				
				if (loc.length() == 0 && ! failure.getFileName().equals(failure.getFullName())) {
					loc = "(" + failure.getFullName() + ")";
				}
				
				stringBuilder.append(failure.getFileName() + loc + ":" + failure.getProblem()).append("\n");
			}
			RunTestsResult rtr = details.getRunTestResult();
			
			if (rtr.getFailures() != null) {
				for (RunTestFailure failure : rtr.getFailures()) {
					String n = (failure.getNamespace() == null ? "" : (failure.getNamespace() + ".")) + failure.getName();
					stringBuilder.append("Test failure, method: " + n + "." + failure.getMethodName() + " -- " + failure.getMessage() + " stack " + failure.getStackTrace() + "\n\n");
				}
			}
			
			if (rtr.getCodeCoverageWarnings() != null) {
				for (CodeCoverageWarning ccw : rtr.getCodeCoverageWarnings()) {
					stringBuilder.append("Code coverage issue");
					
					if (ccw.getName() != null) {
						String n = (ccw.getNamespace() == null ? "" : (ccw.getNamespace() + ".")) + ccw.getName();
						stringBuilder.append(", class:" + n);
					}
					stringBuilder.append(" -- " + ccw.getMessage() + "\n");
				}
			}
		}
		
		if (stringBuilder.length() > 0) {
			stringBuilder.insert(0,  messageHeader);
			
			System.out.println(stringBuilder.toString());
		}
	}
	
	private byte[] readZipFile(String zipFilename) throws Exception {
		byte[] result = null;
		File zipFile = new File(zipFilename);
		FileInputStream fileInputStream = new FileInputStream(zipFile);
		
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			
			while (-1 != (bytesRead = fileInputStream.read(buffer))) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}
			
			result = byteArrayOutputStream.toByteArray();
		} finally {
			fileInputStream.close();
		}
		
		return result;
	}
	
	private void removeNonAppComponents(String archiveFilename) throws Exception {
		String tmpFilename = serverConfiguration.tmpDirectory + "/" + TransformationEngine.basename(archiveFilename);
		String originalFilename = TransformationEngine.dirname(archiveFilename) + "/" + TransformationEngine.basename(archiveFilename) + ".orig";
		String allFilename = TransformationEngine.dirname(archiveFilename) + "/unfiltered_" + TransformationEngine.basename(archiveFilename);
		
		ZipFile zipFile;
		FileOutputStream fileOutputStream;
		ZipOutputStream resultZipFile;
		
		Enumeration<? extends ZipEntry> zipEntries;
		File archiveFile = new File(archiveFilename);
		File tmpFile = new File(tmpFilename);
		File origFile = new File(originalFilename);
		File allFile = new File(allFilename);
		
		// Copy from ARCHIVE to TMP
		if (tmpFile.exists()) {
			tmpFile.delete();
		}
		
		Files.copy(archiveFile.toPath(), tmpFile.toPath());
	
		// Copy from ARCHIVE to ORIG
		if (origFile.exists()) {
			origFile.delete();
		}
		
		Files.move(archiveFile.toPath(), origFile.toPath());
		
		// Copy from TMP to ALL
		if (allFile.exists()) {
			allFile.delete();
		}
		
		Files.copy(tmpFile.toPath(), allFile.toPath());
		
		zipFile = new ZipFile(tmpFilename);
		zipEntries = zipFile.entries();
		
		fileOutputStream = new FileOutputStream(archiveFilename);
		resultZipFile = new ZipOutputStream(fileOutputStream);
		
		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = zipEntries.nextElement();
			
			if (isAppComponent(zipEntry.getName())) {
				byte[] bytes = new byte[1024];
				int length;
				InputStream inputStream = zipFile.getInputStream(zipEntry);
				
				//System.out.println("zipEntry: " + zipEntry.getName() + ", isAppComponent=" + isAppComponent(zipEntry.getName()));
				
				resultZipFile.putNextEntry(zipEntry);
				
				while ((length = inputStream.read(bytes)) > 0) {
					resultZipFile.write(bytes, 0, length);
				}
			}
		}
		
		resultZipFile.close();
		fileOutputStream.close();
		zipFile.close();
	}
	
	private Boolean isAppComponent(String filename) {
		Boolean result = false;
		
//		System.out.println("isAppComponent=" + filename);
		
		for (String directory : appComponents.keySet()) {
			String dirnameExpression = "unpackaged";
			String basenameExpression;
			
			String dirname;
			String basename;
			
			if (directory != null && directory.isEmpty() == false) {
				dirnameExpression += "/" + directory;
			}
			
			basenameExpression = appComponents.get(directory);
			
			dirname = TransformationEngine.dirname(filename);
			basename = TransformationEngine.basename(filename);

			/*
			System.out.println("filename=" + filename + ", dirname=" + dirname + ", basename=" + basename);
			System.out.println("dirnameExpression=" + dirnameExpression + ", basenameExpression=" + basenameExpression);
			*/
			
			if (dirnameExpression.equals(dirname) && basename.startsWith(basenameExpression)) {
				
				result = true;
				break;
			}
		}
		
//		System.out.println("result=" + result);
		
		return result;
	}
	
	private void setUnpackaged(RetrieveRequest request) throws Exception {
		com.sforce.soap.metadata.Package salesforcePackage;
		File unpackedManifest = new File(serverConfiguration.modelDirectory + "/" + MANIFEST_FILE);
		
		System.out.println("Manifest File: " + unpackedManifest.getAbsolutePath());
		
		if (! unpackedManifest.exists() || ! unpackedManifest.isFile()) {
			throw new Exception("No valid retrieve manifest found on " + unpackedManifest.getAbsolutePath());
		}
		
		salesforcePackage = parsePackageManifest(unpackedManifest);
		
		request.setUnpackaged(salesforcePackage);
	}
	
	private com.sforce.soap.metadata.Package parsePackageManifest(File file) throws ParserConfigurationException, IOException, SAXException {
		com.sforce.soap.metadata.Package packageManifest = null;
		List<PackageTypeMembers> listPackageTypes = new ArrayList<PackageTypeMembers>();
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputStream inputStream = new FileInputStream(file);
		Element d = db.parse(inputStream).getDocumentElement();
		PackageTypeMembers[] packageTypesArray;
		
		for (Node c = d.getFirstChild(); c!= null; c = c.getNextSibling()) {
			if (c instanceof Element) {
				Element ce = (Element)c;
				NodeList nodeList = ce.getElementsByTagName("name");
				NodeList m;
				String name;
				List<String> members = new ArrayList<String>();
				PackageTypeMembers packageTypes;
				
				if (nodeList.getLength() == 0) {
					continue;
				}
				
				name = nodeList.item(0).getTextContent();
				
				m = ce.getElementsByTagName("members");
				
				for (int i = 0; i < m.getLength(); i++) {
					Node mm = m.item(i);
					
					members.add(mm.getTextContent());
				}
				
				packageTypes = new PackageTypeMembers();
				
				packageTypes.setName(name);;
				packageTypes.setMembers(members.toArray(new String[members.size()]));
				
				listPackageTypes.add(packageTypes);
			}
		}
		
		packageManifest = new com.sforce.soap.metadata.Package();
		
		packageTypesArray = new PackageTypeMembers[listPackageTypes.size()];
		
		packageManifest.setTypes(listPackageTypes.toArray(packageTypesArray));
		packageManifest.setVersion(API_VERSION + "");
		
		return packageManifest;
	}
	
	private RetrieveResult waitForRetrieveCompletion(AsyncResult asyncResult) throws Exception {
		// Wait for the retrieve to complete
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND_IN_MS;
		String asyncResultId = asyncResult.getId();
		RetrieveResult result = null;
		
		System.out.println("asyncResultId=" + asyncResultId + ", isDone=" + asyncResult.isDone());
		
		do {
			try {
				do {
					System.out.println("Waiting for " + (waitTimeMilliSecs/1000) + " seconds...");
					
					Thread.sleep(waitTimeMilliSecs);
					
					// Double the wait time for the next iteration
					waitTimeMilliSecs *= 2;
					
					if (poll++ > MAX_NUMBER_POLL_REQUESTS) {
						throw new Exception("Request timed out. If this is a large set of metadata components, check that the time allowed by MAX_NUMBER_POLL_REQUESTS is sufficient.");
					}
					
					result = metadataConnection.checkRetrieveStatus(asyncResultId, true);
					
					System.out.println("Retrieve Status: " + result.getStatus());
				} while (! result.isDone());
			} catch (com.sforce.ws.SoapFaultException e) {
				System.out.println("Soap Exception: " + e.getMessage());
			}
		} while (result == null);
		
		return result;
	}
}
