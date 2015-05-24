package com.gepardec.javatools.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Rule;
import org.junit.Test;

import com.gepardec.javatools.dozer.MappingTestHandler.MappingTest;
import com.gepardec.javatools.dozer.objects.domain.Person;
import com.gepardec.javatools.dozer.objects.dto.PersonDTOV3;
import com.gepardec.javatools.dozer.objects.first.ExampleDTO;
import com.gepardec.javatools.dozer.objects.second.ExampleDomainObject;

public class DTOTransformationTest {
	@Rule
	public MappingTestHandler handler = new MappingTestHandler(new DozerBeanMapper());	
	
	@Test @MappingTest(from=ExampleDTO.class, to=ExampleDomainObject.class)
	public void test(){
		handler.ignoreField("header");
	}
	
	@Test @MappingTest(from=PersonDTOV3.class, to=Person.class)
	public void testPersonMapping(){
	}
}
