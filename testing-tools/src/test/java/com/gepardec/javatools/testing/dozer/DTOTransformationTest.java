package com.gepardec.javatools.testing.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.gepardec.javatools.testing.dozer.MappingTestHandler.MappingTest;
import com.gepardec.javatools.testing.dozer.objects.domain.Person;
import com.gepardec.javatools.testing.dozer.objects.dto.PersonDTOV2;
import com.gepardec.javatools.testing.dozer.objects.dto.PersonDTOV3;
import com.gepardec.javatools.testing.dozer.objects.first.ExampleDTO;
import com.gepardec.javatools.testing.dozer.objects.second.ExampleDomainObject;

public class DTOTransformationTest {
	@Rule
	public MappingTestHandler handler = new MappingTestHandler(new DozerBeanMapper());	
	
	@Test @MappingTest(where=ExampleDTO.class, isPartOf=ExampleDomainObject.class)
	public void test(){
		handler.ignoreField("header");
	}
	
	@Ignore @Test @MappingTest(where=PersonDTOV3.class, isPartOf=Person.class)
	public void testPersonV3Mapping(){
	}
	
	@Test @MappingTest(where=PersonDTOV2.class, isPartOf=Person.class)
	public void testPersonV2Mapping(){
	}
}
