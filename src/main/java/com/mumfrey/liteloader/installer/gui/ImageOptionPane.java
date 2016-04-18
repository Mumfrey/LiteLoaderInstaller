package com.mumfrey.liteloader.installer.gui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JOptionPane;

public class ImageOptionPane extends JOptionPane
{
    private static final long serialVersionUID = 1L;
    
    public ImageOptionPane(Object message, int messageType, int optionType)
    {
        super(message, messageType, optionType);
    }

    private Image background;

    public void setBackgroundImage(Image background)
    {
        this.background = background;
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);

        if (this.background != null)
        {
            graphics.drawImage(this.background, 0, 0, null);
        }
    }

    @Override
    protected void paintChildren(Graphics g)
    {
        super.paintChildren(g);
    }
}
