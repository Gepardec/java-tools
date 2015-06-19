package com.gepardec.javatools.testing.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.gepardec.javatools.testing.dozer.MappingTestHandler.MappingTest;
import com.gepardec.javatools.testing.dozer.objects.first.FirstRequestHeader;
import com.gepardec.javatools.testing.dozer.objects.second.SecondRequestHeader;
import com.gepardec.javatools.testing.dozer.objects.second.SecondRequestHeader2;

public class RequestHeaderTransformationTest {
	
	@Rule
	public MappingTestHandler handler = new MappingTestHandler(new DozerBeanMapper());	
	
	@Test @MappingTest(where=FirstRequestHeader.class, isPartOf=SecondRequestHeader.class)
	public void testFirst(){ }
	
	@Ignore @Test @MappingTest(where=FirstRequestHeader.class, isPartOf=SecondRequestHeader2.class)
	public void testSecond(){ }
}
