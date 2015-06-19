package com.gepardec.javatools.testing.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Rule;
import org.junit.Test;

import com.gepardec.javatools.testing.dozer.MappingTestHandler;
import com.gepardec.javatools.testing.dozer.MappingTestHandler.MappingTest;
import com.gepardec.javatools.testing.dozer.objects.first.ExampleDTO;
import com.gepardec.javatools.testing.dozer.objects.second.ExampleDomainObject;

public class DTOTransformationTest {
	@Rule
	public MappingTestHandler handler = new MappingTestHandler(new DozerBeanMapper());	
	
	@Test @MappingTest(where=ExampleDTO.class, isPartOf=ExampleDomainObject.class)
	public void test(){
		handler.ignoreField("header");
	}
}
