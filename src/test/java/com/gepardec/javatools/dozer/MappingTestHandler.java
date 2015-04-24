package com.gepardec.javatools.dozer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class MappingTestHandler extends TestWatcher{
	
	private DozerBeanMapper mapper;
	private MappingChecker transformationChecker;
	private Map<String, Object> customFieldValues;
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MappingTest{
		public Class<?> from();
		public Class<?> to();
	}
	
	public MappingTestHandler(DozerBeanMapper mapper) {
		this.mapper = mapper;
		transformationChecker = new MappingChecker();
		setCustomFieldValues(new HashMap<String, Object>());
	}
	
	@Override
	protected void succeeded(Description description) {
		final MappingTest transformationTest = description.getAnnotation(MappingTest.class);
		if(transformationTest == null){
			return;
		}
		transformationChecker.setCustomFieldValues(customFieldValues);
		transformationChecker.checkLossless(mapper, transformationTest.from(), transformationTest.to());	
    }

	public Map<String, Object> getCustomFieldValues() {
		return customFieldValues;
	}

	public void setCustomFieldValues(Map<String, Object> customFieldValues) {
		this.customFieldValues = customFieldValues;
	}
	
	public void ignoreField(String field){
		customFieldValues.put(field, null);
	}

}
