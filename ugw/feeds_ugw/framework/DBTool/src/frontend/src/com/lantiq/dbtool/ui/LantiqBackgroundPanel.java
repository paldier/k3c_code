/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LantiqBackgroundPanel extends JPanel {
	Image image;
	boolean scale = true;

	public LantiqBackgroundPanel(String filename) throws IOException {
		InputStream url = getClass().getResourceAsStream(filename);
		image = ImageIO.read(url);
	}

	public void paintComponent(Graphics g) {
		if (scale) {
			g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		} else {
			g.drawImage(image, getWidth() * 2 / 5, 0, this);
		}
	}

	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		
		JPanel topPanel = new LantiqBackgroundPanel("resources/LQIMG-1.jpg");
		
		frame.getContentPane().add(topPanel, BorderLayout.CENTER);

		frame.setSize(new Dimension(600, 400));
		frame.setVisible(true);
	}

	public boolean isScale() {
		return scale;
	}

	public void setScale(boolean scale) {
		this.scale = scale;
	}
}
