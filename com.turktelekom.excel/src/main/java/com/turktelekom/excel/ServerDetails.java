package com.turktelekom.excel;

import org.apache.commons.lang3.StringUtils;

public class ServerDetails {
	
	private String machineType;
	private String serverType;
	private int numServer;
	private int numCore;
	private String OS;
	private boolean isHA;
	private int diskSize;
	
	public void update(ServerDetails details) {
		if(details != null) {
			this.machineType = details.machineType;
			this.serverType = details.serverType;
			this.numServer += details.numServer;
			this.numCore += details.numCore;
			this.OS = StringUtils.isNotBlank(details.OS) ? details.OS : this.OS;
			this.isHA = details.isHA;
			this.diskSize += details.diskSize;
		}
	}
	
	
	
	public String getMachineType() {
		return machineType;
	}
	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}
	
	public int getNumServer() {
		return numServer;
	}
	public void setNumServer(int numServer) {
		this.numServer = numServer;
	}
	public int getNumCore() {
		return numCore;
	}
	public void setNumCore(int numCore) {
		this.numCore = numCore;
	}
	public String getOS() {
		return OS;
	}
	public void setOS(String oS) {
		OS = oS;
	}
	public boolean isHA() {
		return isHA;
	}
	public void setHA(boolean isHA) {
		this.isHA = isHA;
	}
	
	public String getServerType() {
		return serverType;
	}



	public void setServerType(String serverType) {
		this.serverType = serverType;
	}



	public int getDiskSize() {
		return diskSize;
	}



	public void setDiskSize(int diskSize) {
		this.diskSize = diskSize;
	}



	@Override
	public String toString() {
		return String.format("{machineType: %s, serverType: %s, numServer: %d, numCore: %d, OS: %s, diskSize: %d}", machineType, serverType, numServer, numCore, OS, diskSize);
	}
	
}
