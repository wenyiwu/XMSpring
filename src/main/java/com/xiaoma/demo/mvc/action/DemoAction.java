package com.xiaoma.demo.mvc.action;



import com.xiaoma.demo.service.IDemoService;
import com.xiaoma.spring.framework.annotation.Autowired;
import com.xiaoma.spring.framework.annotation.Controller;
import com.xiaoma.spring.framework.annotation.RequestMapping;
import com.xiaoma.spring.framework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/demo")
public class DemoAction {

	public IDemoService getDemoService() {
		return demoService;
	}

	@Autowired
	private IDemoService demoService;

	private List l;
	
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
