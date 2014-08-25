package com.mumfrey.liteloader.installer.gui;

public class CancelledException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public CancelledException()
	{
	}
	
	public CancelledException(String message)
	{
		super(message);
	}
	
	public CancelledException(Throwable cause)
	{
		super(cause);
	}
	
	public CancelledException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
