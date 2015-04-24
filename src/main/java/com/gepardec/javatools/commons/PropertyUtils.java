package com.gepardec.javatools.commons;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.reflect.FieldUtils;

public class PropertyUtils {
	private static final Pattern ARRAY_ELEMENT = Pattern.compile("^(@?\\w+)\\[(-?\\d+)\\]$");
	@SuppressWarnings("rawtypes")
	public static Object getProperty(Object object, String path) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if(path == null || object == null){
			throw new IllegalArgumentException("Input parameter can not be null");
		}
		String[] fields = path.split("\\.");
		Object result = object;
		
		for(String fieldName : fields){
			int index = -1;
			String field = fieldName;
			Matcher arrayElementMatcher = ARRAY_ELEMENT.matcher(field);
			if(arrayElementMatcher.matches()){
				field = arrayElementMatcher.group(1);
				index = Integer.parseInt(arrayElementMatcher.group(2));
			}
			Field f = result.getClass().getDeclaredField(field);
			f.setAccessible(true);
			result = f.get(result);
			if(index >=0 && result instanceof Collection){
				result = ((Collection)result).toArray()[index];
			}
		}
		return result;
			
	}
	
	@SuppressWarnings("rawtypes")
	public static void setProperty(Object object, String path, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if(path == null || object == null){
			throw new IllegalArgumentException("Input parameter can not be null");
		}
		
		String[] fields = path.split("\\.");
		Object loopObject = object;
		Object fieldParent = object;
		Field currentField = null;
		for(String fieldName : fields){
			int index = -1;
			String field = fieldName;
			Matcher arrayElementMatcher = ARRAY_ELEMENT.matcher(field);
			if(arrayElementMatcher.matches()){
				field = arrayElementMatcher.group(1);
				index = Integer.parseInt(arrayElementMatcher.group(2));
			}
			currentField = loopObject.getClass().getDeclaredField(field);
			currentField.setAccessible(true);
			fieldParent = loopObject;
			loopObject = currentField.get(loopObject);
			if(index >=0 && loopObject instanceof Collection){
				fieldParent = loopObject;
				loopObject = ((Collection)loopObject).toArray()[index];
			}
		}
		
		currentField.set(fieldParent, value);
		
	}
	
	/**
	 * Updating of properties of an object using properties of another object.
	 * Actually it implements method PropertyUtils.copyProperties of apache commons
	 * with the difference that it copies all properties (not only those that have getters)
	 * @author egor
	 *
	 */
		
		public static void copyProps(Object source, Object destination){
			if(destination.getClass().isAssignableFrom(source.getClass())){
				Field[] fields = FieldUtils.getAllFields(source.getClass());
				
				for (Field f : fields) {
					try {
						boolean accessibleForF = f.isAccessible();
						f.setAccessible(true);
						
						Field to = FieldUtils.getField(destination.getClass(), f.getName(), true);
						boolean accessibleForTo = to.isAccessible();
						to.setAccessible(true);
						
						to.set(destination, f.get(source));
						f.setAccessible(accessibleForF);
						to.setAccessible(accessibleForTo);
						
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}
}
