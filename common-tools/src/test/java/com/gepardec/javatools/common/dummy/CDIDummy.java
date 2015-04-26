package com.gepardec.javatools.common.dummy;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


public class CDIDummy {
	private DummyObject injectable;
	
	@Resource
	private DummyObject resource;
	
	private List<DummyObject> dummyObjects;
	
	@PostConstruct
	public void init(){
		getInjectable().setDummyField(getInjectable().getDummyField() + " after post construct");
		getResource().setDummyField(getResource().getDummyField() + " after post construct");
	}

	public DummyObject getInjectable() {
		return injectable;
	}

	public void setInjectable(DummyObject injectable) {
		this.injectable = injectable;
	}

	public DummyObject getResource() {
		return resource;
	}

	public void setResource(DummyObject resource) {
		this.resource = resource;
	}

	public List<DummyObject> getDummyObjects() {
		return dummyObjects;
	}

	public void setDummyObjects(List<DummyObject> dummyObjects) {
		this.dummyObjects = dummyObjects;
	}
}
