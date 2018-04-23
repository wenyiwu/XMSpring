package com.xiaoma.demo.mvc.action;



import com.xiaoma.demo.service.IDemoService;
import com.xiaoma.spring.annotation.Autowried;
import com.xiaoma.spring.annotation.Controller;
import com.xiaoma.spring.annotation.RequestMapping;
import com.xiaoma.spring.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DemoAction {
	
	@Autowried
	private IDemoService demoService;
	
	@RequestMapping("/query.json")
	public void query(HttpServletRequest req, HttpServletResponse resp,
                      @RequestParam("name") String name){
		String result = demoService.get(name);
		System.out.println(result);
//		try {
//			resp.getWriter().write(result);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	@RequestMapping("/edit.json")
	public void edit(HttpServletRequest req, HttpServletResponse resp, Integer id){

	}
	
}
