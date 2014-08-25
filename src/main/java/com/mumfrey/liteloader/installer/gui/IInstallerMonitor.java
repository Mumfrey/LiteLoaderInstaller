package com.mumfrey.liteloader.installer.gui;


public interface IInstallerMonitor
{
	public abstract boolean isCancelled();
	
	public abstract void setProgressMessage(String message);
	
	public abstract void setProgress(int progress);
	
	public abstract void setProgressAndMessage(int progress, String message);
	
	public abstract void beginTask(String message);
	
	public abstract void onTaskCompleted(boolean success, String message);
}