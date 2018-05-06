package com.xiaoma.demo.mvc.action;


import com.xiaoma.demo.service.IQueryService;
import com.xiaoma.spring.framework.annotation.Autowired;
import com.xiaoma.spring.framework.annotation.Controller;
import com.xiaoma.spring.framework.annotation.RequestMapping;
import com.xiaoma.spring.framework.annotation.RequestParam;
import com.xiaoma.spring.framework.webmvc.XMModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@Controller
@RequestMapping("/")
public class PageAction {

	@Autowired
	IQueryService queryService;
	
	@RequestMapping("/first.html")
	public XMModelAndView query(@RequestParam("teacher") String teacher){
		String result = queryService.query(teacher);
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("teacher", teacher);
		model.put("data", result);
		model.put("token", "123456");
		return new XMModelAndView("first.html",model);
	}
	
}
