package com.xiaoma.demo.mvc.action;


import com.xiaoma.demo.service.IDemoService;
import com.xiaoma.spring.annotation.Autowried;
import com.xiaoma.spring.annotation.Controller;
import com.xiaoma.spring.annotation.RequestMapping;

@Controller
public class MyAction {

		@Autowried
		IDemoService demoService;
	
		@RequestMapping("/index.html")
		public void query(){

		}
	
}
