package com.xiaoma.demo.service.impl;


import com.xiaoma.demo.service.IDemoService;
import com.xiaoma.spring.annotation.Service;

@Service
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name;
	}

}
