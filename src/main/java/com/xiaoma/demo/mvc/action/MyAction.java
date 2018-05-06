package com.xiaoma.demo.mvc.action;


import com.xiaoma.demo.service.IModifyService;
import com.xiaoma.demo.service.IQueryService;
import com.xiaoma.spring.framework.annotation.Autowired;
import com.xiaoma.spring.framework.annotation.Controller;
import com.xiaoma.spring.framework.annotation.RequestMapping;
import com.xiaoma.spring.framework.annotation.RequestParam;
import com.xiaoma.spring.framework.webmvc.XMModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/web")
public class MyAction {

	@Autowired
	IQueryService queryService;

	@Autowired
	IModifyService modifyService;

	@RequestMapping("/query.json")
	public XMModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@RequestParam("name") String name){
		String result = queryService.query(name);
		System.out.println(result);
		return out(response,result);
	}

	@RequestMapping("/add*.json")
	public XMModelAndView add(HttpServletRequest request,HttpServletResponse response,
							  @RequestParam("name") String name,@RequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		return out(response,result);
	}

	@RequestMapping("/remove.json")
	public XMModelAndView remove(HttpServletRequest request,HttpServletResponse response,
								 @RequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}

	@RequestMapping("/edit.json")
	public XMModelAndView edit(HttpServletRequest request,HttpServletResponse response,
							   @RequestParam("id") Integer id,
							   @RequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}



	private XMModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
