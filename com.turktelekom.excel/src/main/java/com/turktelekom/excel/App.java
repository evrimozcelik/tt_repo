package com.turktelekom.excel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 * 
 */
public class App {
	
	public static final String SRC_PATH = "/Users/Evrim/Downloads/Application Question -BSS v5_Consolidated_6thApr.xlsx";
	public static final String INFRA_REF_PATH = "/Users/Evrim/Downloads/TTG_infra_input.xlsx";
	
	public static final int APP_ID_SRC_COL_INDEX = 0;
	public static final int APP_NAME_SRC_COL_INDEX = 1;
	public static final int OPERATOR_SRC_COL_INDEX = 4;
	public static final int OPS_OWNER_SRC_COL_INDEX = 7;
	public static final int HOSTNAME_SRC_COL_INDEX = 27; 
	
	public static final int HOSTNAME_REF_COL_INDEX = 1;
	public static final int MACHINE_TYPE_REF_COL_INDEX = 2;
	public static final int OS_VERSION_REF_COL_INDEX = 4;
	public static final int OS_NAME_REF_COL_INDEX = 5;
	public static final int NUM_CORE_REF_COL_INDEX = 7;
	public static final int DISK_SIZE_REF_COL_INDEX = 8;
	public static final int SERVER_FUNC_REF_COL_INDEX = 22;
	public static final int DB_TYPE_REF_COL_INDEX = 23;
	public static final int MW_REF_COL_INDEX = 24;
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
	
    public static void main( String[] args ) {
//    		String hostnames = "mnpappp0 ,mnpappp1,mnpappp2 ,mnpappp3,mnpdbp02 ,mnpdbp03";
//    		String[] array = hostnames.split("\\s*,[,\\s]*");
//    		System.out.println(String.join(",", array));
    		startProcessing();
    }
    
    private static void startProcessing() {
    
    		XSSFWorkbook wb = null;
    		XSSFWorkbook infraWb = null;
    		
    		try {
    			FileInputStream fis = new FileInputStream(SRC_PATH);
    			wb = new XSSFWorkbook(fis);
    			
    			FileInputStream infraFis = new FileInputStream(INFRA_REF_PATH);
    			infraWb = new XSSFWorkbook(infraFis);
    			
    			XSSFSheet sheet = wb.getSheetAt(0);
    			int rows = sheet.getPhysicalNumberOfRows();
    			
    			for (int r = 3; r < rows; r++) {
    				XSSFRow row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				
				String appId = row.getCell(APP_ID_SRC_COL_INDEX)!=null ? row.getCell(APP_ID_SRC_COL_INDEX).getStringCellValue() : "";
				String appName = row.getCell(APP_NAME_SRC_COL_INDEX)!=null ? row.getCell(APP_NAME_SRC_COL_INDEX).getStringCellValue() : "";
				String operator = row.getCell(OPERATOR_SRC_COL_INDEX)!=null ? row.getCell(OPERATOR_SRC_COL_INDEX).getStringCellValue() : "";
				
				logger.info("Processing App {} - {}", appId, appName);
				
				//String servers = row.getCell(HOSTNAME_SRC_COL_INDEX)!=null ? row.getCell(HOSTNAME_SRC_COL_INDEX).getStringCellValue() : "";
				String servers = getStringCellValue(row, HOSTNAME_SRC_COL_INDEX);
				
				if(StringUtils.isNotBlank(servers)) {
					servers = servers.trim().replaceAll("\n", ",");
					String[] serverArray = servers.split("\\s*,[,\\s]*");
					Map<String,ServerDetails> serverDetailsMap;
					
					serverDetailsMap = lookupInfraDetails(appId, infraWb, serverArray);
							
					if(serverDetailsMap != null) {
						updateInfraDetails(row, serverDetailsMap);
					}
					
				}
			}
    			
    			// Save sheet
			FileOutputStream out = new FileOutputStream(SRC_PATH);
			wb.write(out);
			out.close();
									
    			
    		} catch(Exception e) {
    			logger.error(e.getMessage(),e);
    		} finally {
    			try {
    				wb.close();
    				infraWb.close();
    			} catch (Exception e) { }
    		}
    		
    }
    
    public static Map<String,ServerDetails> lookupInfraDetails(String appId, XSSFWorkbook infraWb, String[] serverArray) {
    	
    		Map<String,ServerDetails> serverDetailsMap = new HashMap<String, ServerDetails>();
    		serverDetailsMap.put("App", new ServerDetails());
    		serverDetailsMap.put("DB", new ServerDetails());
    		serverDetailsMap.put("Web", new ServerDetails());
    		serverDetailsMap.put("Other", new ServerDetails());
    		
    		XSSFSheet sheet = infraWb.getSheetAt(0);
		int rows = sheet.getPhysicalNumberOfRows();
		int numNotFound = 0;
		
		logger.info("Looking up Server List: {}", StringUtils.join(serverArray, ","));
		
		for (String servername : serverArray) {
			
			boolean found = false;
		
			for (int r = 0; r < rows; r++) {
				XSSFRow row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				
				String infraHostname = getCell(row, HOSTNAME_REF_COL_INDEX)!=null ? getCell(row, HOSTNAME_REF_COL_INDEX).getStringCellValue() : "";
				infraHostname = infraHostname.trim();
				
				if(StringUtils.containsIgnoreCase(servername, infraHostname) || StringUtils.containsIgnoreCase(infraHostname, servername)) {					
					String machineType = getStringCellValue(row, MACHINE_TYPE_REF_COL_INDEX);
					int numCore = (int)getNumericCellValue(row, NUM_CORE_REF_COL_INDEX);
					String osName = getStringCellValue(row, OS_NAME_REF_COL_INDEX);
					String osVersion = getStringCellValue(row, OS_VERSION_REF_COL_INDEX);
					String serverFunction = getStringCellValue(row, SERVER_FUNC_REF_COL_INDEX);
					String dbType = getStringCellValue(row, DB_TYPE_REF_COL_INDEX);
					String mwType = getStringCellValue(row, MW_REF_COL_INDEX);
					int diskSize = (int)getNumericCellValue(row, DISK_SIZE_REF_COL_INDEX);
					
					ServerDetails serverDetails = new ServerDetails();
					serverDetails.setMachineType(machineType);
					serverDetails.setNumCore(numCore);
					serverDetails.setNumServer(1);
					serverDetails.setOS(StringUtils.equalsIgnoreCase(osName, osVersion) ? osName : ""+osName+" "+osVersion);
					serverDetails.setDiskSize(diskSize);
					
					if(StringUtils.containsIgnoreCase(mwType, "Apache")) {
						serverFunction = "Web";
					}
					
					if(StringUtils.contains(serverFunction, "Application")) {
						serverDetails.setServerType(mwType);
						serverDetailsMap.get("App").update(serverDetails);
					} else if(StringUtils.contains(serverFunction, "Database")) {
						serverDetails.setServerType(dbType);
						serverDetailsMap.get("DB").update(serverDetails);
					} else if(StringUtils.contains(serverFunction, "Web")) {
						serverDetails.setServerType(mwType);
						serverDetailsMap.get("Web").update(serverDetails);
					} else {
						serverDetails.setServerType(mwType);
						serverDetailsMap.get("Other").update(serverDetails);
					}
					
					logger.debug("Found {} - {}", servername, serverDetails);

					found = true;
					break;
				}
					
			}
			
			if(!found) {
				logger.warn("Hostname Not Found! Hostname: {}, AppId: {}", servername, appId);
				numNotFound++;
			}
		
		}
		
		logger.info("AppId: {},  #Servername: {}, Total Not Found: {}, Consolidated - App: {}, DB: {}, Web: {}, Other: {}", appId, serverArray.length, numNotFound, serverDetailsMap.get("App"), serverDetailsMap.get("DB"),serverDetailsMap.get("Web"),serverDetailsMap.get("Other"));
		
		if(serverArray.length == numNotFound) {
			return null;
		} else {
			return serverDetailsMap;
		}

	}
    
    public static void updateInfraDetails(XSSFRow row, Map<String,ServerDetails> serverDetailsMap) {
    		
    		// App
    		int index = 30;
    		row.createCell(index).setCellValue(serverDetailsMap.get("App").getMachineType());
    		row.createCell(index+1).setCellValue(serverDetailsMap.get("App").getNumServer());
    		row.createCell(index+2).setCellValue(serverDetailsMap.get("App").getNumCore());
    		row.createCell(index+3).setCellValue(serverDetailsMap.get("App").getOS());
    		
    		// DB
    		index = 35;
    		row.createCell(index).setCellValue(serverDetailsMap.get("DB").getMachineType());
    		row.createCell(index+1).setCellValue(serverDetailsMap.get("DB").getNumServer());
    		row.createCell(index+2).setCellValue(serverDetailsMap.get("DB").getNumCore());
    		row.createCell(index+3).setCellValue(serverDetailsMap.get("DB").getOS());
    		row.createCell(index+5).setCellValue(serverDetailsMap.get("DB").getDiskSize());
    		
    		// Web
    		index = 42;
    		row.createCell(index).setCellValue(serverDetailsMap.get("Web").getServerType());
    		row.createCell(index+1).setCellValue(serverDetailsMap.get("Web").getMachineType());
    		row.createCell(index+2).setCellValue(serverDetailsMap.get("Web").getNumServer());
    		row.createCell(index+3).setCellValue(serverDetailsMap.get("Web").getNumCore());
    		row.createCell(index+4).setCellValue(serverDetailsMap.get("Web").getOS());
    		
    		// Other
    		index = 47;
    		row.createCell(index).setCellValue(serverDetailsMap.get("Other").getServerType());
    		row.createCell(index+1).setCellValue(serverDetailsMap.get("Other").getMachineType());
    		row.createCell(index+2).setCellValue(serverDetailsMap.get("Other").getNumServer());
    		row.createCell(index+3).setCellValue(serverDetailsMap.get("Other").getNumCore());
    		row.createCell(index+4).setCellValue(serverDetailsMap.get("Other").getOS());
    		
    }
    
    public static XSSFCell getCell(XSSFRow row, int cellnum) {
    		if(row == null) {
    			return null;
    		} else {
    			XSSFCell cell = row.getCell(cellnum);
    			if(cell == null) {
    				return null;
    			} else {
    				if(cell.getCellTypeEnum() == CellType.ERROR) {
    					return null;
    				} else {
    					return cell;
    				}
    			}
    		}
    }
    
    public static String getStringCellValue(XSSFRow row, int cellnum) {
    		XSSFCell cell = getCell(row, cellnum);
    		if(cell != null && cell.getCellTypeEnum() == CellType.STRING) {
    			return cell.getStringCellValue();
    		} else if(cell != null && cell.getCellTypeEnum() == CellType.NUMERIC) {
    			return "" + cell.getNumericCellValue();
    		} else if(cell != null && cell.getCellTypeEnum() == CellType.FORMULA) {
    			return cell.getRawValue();
    		}else {
    			return "";
    		}
    }
    
    public static double getNumericCellValue(XSSFRow row, int cellnum) {
		XSSFCell cell = getCell(row, cellnum);
		if(cell != null && cell.getCellTypeEnum() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		} else {
			return 0;
		}
    }
    
    	
}
