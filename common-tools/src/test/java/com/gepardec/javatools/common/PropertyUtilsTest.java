package com.gepardec.javatools.common;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.gepardec.javatools.common.PropertyUtils.TypeConverter;
import com.gepardec.javatools.common.dummy.CDIDummy;
import com.gepardec.javatools.common.dummy.DummyObject;

public class PropertyUtilsTest {
	private DummyObject dummyResource;
	private CDIDummy patient;
	
	@Before
	public void setUp(){		
		dummyResource = new DummyObject();
		dummyResource.setDummyField("dummy resource");
		
		patient = new CDIDummy();
		
		patient.setResource(dummyResource);
		
		patient.setDummyObjects(Arrays.asList(dummyResource, new DummyObject(), new DummyObject()));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void getThrowsExceptionForWrongInput() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		PropertyUtils.getProperty(null, "resource");
	}
	
	@Test(expected=NoSuchFieldException.class)
	public void getThrowsExceptionForWrongPath() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		PropertyUtils.getProperty(patient, "somefield");
	}
	
	@Test
	public void getReturnsCorrectValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		assertEquals("dummy resource", PropertyUtils.getProperty(patient, "resource.dummyField"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setThrowsExceptionForWrongInput() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		PropertyUtils.setProperty(null, "resource", null);
	}
	
	@Test(expected=NoSuchFieldException.class)
	public void setThrowsExceptionForWrongPath() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		PropertyUtils.setProperty(patient, "somefield", "");
	}
	
	@Test
	public void setSetsCorrectValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		PropertyUtils.setProperty(patient, "resource.dummyField", "new dummy field");
		assertEquals("new dummy field", patient.getResource().getDummyField());
	}
	
	@Test
	public void correctArraysGet() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		patient.getDummyObjects().get(1).setDummyField("dummyFieldOf1");
		assertEquals("dummyFieldOf1", PropertyUtils.getProperty(this, "patient.dummyObjects[1].dummyField"));
	}
	
	@Test
	public void correctArraysSet() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		patient.getDummyObjects().get(1).setDummyField("dummyFieldOf1");
		assertEquals("dummyFieldOf1", PropertyUtils.getProperty(this, "patient.dummyObjects[1].dummyField"));
		PropertyUtils.setProperty(this, "patient.dummyObjects[1].dummyField", "newDummyFieldOf1");
		assertEquals("newDummyFieldOf1", patient.getDummyObjects().get(1).getDummyField());
	}
	
	@Test(expected=NullPointerException.class)
	public void failedNestedProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		patient = new CDIDummy();
		assertTrue(patient.getResource() == null);		
		PropertyUtils.setProperty(patient, "resource.dummyField", "dummyField", false);
	}
	
	@Test
	public void correctedNestedProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		patient = new CDIDummy();
		assertTrue(patient.getResource() == null);		
		PropertyUtils.setProperty(patient, "resource.dummyField", "dummyField", true);
		assertEquals("dummyField", patient.getResource().getDummyField());
	}
	
	@Test
	public void correctConverter() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException{
		patient = new CDIDummy();
		assertTrue(patient.getResource() == null);
		
		TypeConverter tc = new TypeConverter() {
			
			String o = "dummyField";
			
			@Override
			public Object convertTo(Class<?> toClass) {
				if(toClass == DummyObject.class){
					DummyObject dummyObject = new DummyObject();
					dummyObject.setDummyField(o);
					return dummyObject;
				}
				return null;
			}
		};
		
		PropertyUtils.setProperty(patient, "resource", tc, false);
		assertEquals("dummyField", patient.getResource().getDummyField());
	}
}
