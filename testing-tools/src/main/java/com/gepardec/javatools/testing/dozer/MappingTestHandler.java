package com.gepardec.javatools.testing.dozer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class MappingTestHandler extends TestWatcher{
	
	private DozerBeanMapper mapper;
	private MappingChecker transformationChecker;
	private List<String> ignoreFields;
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MappingTest{
		public Class<?> where();
		public Class<?> isPartOf() default MappingTest.class;;
		public Class<?> equivalentTo() default MappingTest.class;
	}
	
	public MappingTestHandler(DozerBeanMapper mapper) {
		this.mapper = mapper;
		transformationChecker = new MappingChecker();
		ignoreFields = new ArrayList<String>();
	}
	
	@Override
	protected void succeeded(Description description) {
		final MappingTest transformationTest = description.getAnnotation(MappingTest.class);
		if(transformationTest == null){
			return;
		}
		if(transformationTest.isPartOf().equals(MappingTest.class) && transformationTest.equivalentTo().equals(MappingTest.class)){
			return;
		}
		
		Class<?> leftClass = transformationTest.where();
		boolean bidirectional = !transformationTest.equivalentTo().equals(MappingTest.class);
		Class<?> rightClass = bidirectional ? transformationTest.equivalentTo() : transformationTest.isPartOf();
		
		transformationChecker.setIgnoreFields(ignoreFields);
		transformationChecker.checkLossless(mapper, leftClass, rightClass);
		if(bidirectional){
			transformationChecker.checkLossless(mapper, rightClass, leftClass);
		}
    }

	public void ignoreField(String field){
		ignoreFields.add(field);
	}

}
