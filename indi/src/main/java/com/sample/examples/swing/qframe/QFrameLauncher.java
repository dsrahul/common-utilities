package com.sample.examples.swing.qframe;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class QFrameLauncher {
	public static void main(String[] args) {
		String[] contextPaths = new String[]{"qframeapp-context.xml", "context.xml", "datasource.xml"};
        new ClassPathXmlApplicationContext(contextPaths);
	}

}
