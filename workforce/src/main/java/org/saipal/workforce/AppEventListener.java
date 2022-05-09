package org.saipal.workforce;

import org.saipal.workforce.lang.AppLangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class AppEventListener {

	@Autowired
	AppLangService applang;
	
	Logger log = LoggerFactory.getLogger(AppEventListener.class);

	@EventListener
	public void onApplicationStartedEvent(ApplicationStartedEvent event){
		applang.loadAllTranslations();
	}
}