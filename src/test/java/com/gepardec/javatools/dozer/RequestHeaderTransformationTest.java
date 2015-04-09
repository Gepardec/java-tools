package com.gepardec.javatools.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Rule;
import org.junit.Test;

import com.gepardec.javatools.dozer.MappingTestHandler.MappingTest;
import com.gepardec.javatools.dozer.objects.first.FirstRequestHeader;
import com.gepardec.javatools.dozer.objects.second.SecondRequestHeader;
import com.gepardec.javatools.dozer.objects.second.SecondRequestHeader2;

public class RequestHeaderTransformationTest {
	
	@Rule
	public MappingTestHandler handler = new MappingTestHandler(new DozerBeanMapper());	
	
	@Test @MappingTest(from=FirstRequestHeader.class, to=SecondRequestHeader.class)
	public void testFirst(){ }
	
	@Test @MappingTest(from=FirstRequestHeader.class, to=SecondRequestHeader2.class)
	public void testSecond(){ }
}
