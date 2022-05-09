package org.saipal.workforce.contorllers;

import javax.servlet.http.HttpServletRequest;

import org.saipal.fmisutil.auth.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/apphome")
public class AppController {

	@Autowired
	private Authenticated auth;

	private static final Logger LOG = LoggerFactory.getLogger(AppController.class);

	@GetMapping("")
	public String index(HttpServletRequest request, Model model) {
		if(request.getParameter("lang")!=null) {
			String lang = request.getParameter("lang")+"";
			auth.setLang(lang);
		}
		
		if(request.getParameter("ribbonId")!=null) {
			String ribbonId = request.getParameter("ribbonId")+"";
			auth.setUserstate("ribbonid",ribbonId);
		}
		
		String editor = request.getParameter("editor");
		if(editor!=null) {
			if(editor.equals("1")) {
				auth.setUserstate("editor","1");
			}else {
				auth.setUserstate("editor","0");
			}
		}
		return "main";
	}
}
