package com.mumfrey.liteloader.installer.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;

import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private Image background;
	
	public ImagePanel()
	{
	}
	
	public void setBackgroundImage(Image background)
	{
		this.background = background;
	}
	
	public Image getBackgroundImage()
	{
		return this.background;
	}
	
	@Override
	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);
		
		Shape clip = graphics.getClip();
		graphics.setClip(0, 0, this.getWidth(), this.getHeight() + 4);
		
		if (this.background != null)
		{
			graphics.drawImage(this.background, 0, 0, null);
		}
		
		graphics.setClip(clip);
	}
}
