package com.gepardec.javatools.dozer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.dozer.DozerBeanMapper;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gepardec.javatools.dozer.MappingChecker;

public class MappingTestHandler extends TestWatcher{
	
	private DozerBeanMapper mapper;
	private MappingChecker transformationChecker;
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MappingTest{
		public Class<?> from();
		public Class<?> to();
	}
	
	public MappingTestHandler(DozerBeanMapper mapper) {
		this.mapper = mapper;
		transformationChecker = new MappingChecker();
	}
	
	@Override
	protected void succeeded(Description description) {
		final MappingTest transformationTest = description.getAnnotation(MappingTest.class);
		if(transformationTest == null){
			return;
		}
		transformationChecker.checkLossless(mapper, transformationTest.from(), transformationTest.to());	
    }

}
