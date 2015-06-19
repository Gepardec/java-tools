package com.gepardec.javatools.testing.dozer;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jemos.podam.api.AbstractRandomDataProviderStrategy;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 * Checks Lossless of dozer transformation
 * @author eerofeev
 *
 */
public class MappingChecker{
	
	private static AbstractRandomDataProviderStrategy STRATEGY = new AbstractRandomDataProviderStrategy() {
	};
	private static PodamFactory FACTORY;
		
	{
		STRATEGY.setNumberOfCollectionElements(1);
		FACTORY = new PodamFactoryImpl(STRATEGY);
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(MappingChecker.class);
	
	public enum Log{
		NONE, OUT, ERR;
	}
	
	public interface CustomGenerator<T>{
		T generate();
	}
	
	private Log logLevel;
	private Map<Class<?>, CustomGenerator<?>> customGenerators;
	private Map<String, Object> customFieldValues;
	private List<String> ignoreFields;
	
	public MappingChecker(Log logLevel) {
		this.logLevel = logLevel;
		customGenerators = new HashMap<Class<?>, CustomGenerator<?>>();
		customFieldValues = new HashMap<String, Object>();
		ignoreFields = new ArrayList<String>();
	}
	
	public MappingChecker() {
		this.logLevel = Log.ERR;
		customGenerators = new HashMap<Class<?>, CustomGenerator<?>>();
		customFieldValues = new HashMap<String, Object>();
		ignoreFields = new ArrayList<String>();
	}
	
	public <T> void addCustomGenerator(CustomGenerator<T> randomGnerator, Class<? extends T> generatorClass){
		customGenerators.put(generatorClass, randomGnerator);
	}
	
	public void clearCustomGenerators(){
		customGenerators.clear();
	}
	
	public Map<String, Object> getCustomFieldValues() {
		return customFieldValues;
	}

	public void setCustomFieldValues(Map<String, Object> customFieldValues) {
		this.customFieldValues = customFieldValues;
	}
	
	public void setIgnoreFields(List<String> ignoreFields) {
		this.ignoreFields = ignoreFields;		
	}
	
	/**
	 * Checks, whether Dozer Transformation ClassL->ClassR is lossless.
	 * Transformation is lossless iff l==l' at the transformation l->r->l'
	 */
	public <L, R> void checkLossless(Mapper mapper, Class<? extends L> leftClass, Class<? extends R> rightClass){
		if(leftClass.isEnum() && rightClass.isEnum()){
			checkLosslessForEnum(mapper, leftClass, rightClass);
			return;
		}
		L original = manufactureInstance(leftClass);
		applyCustomGenerators(original);
		applyCustomFieldValues(original);
		applyIgnoreFields(original);
		R right = mapper.map(original, rightClass);
		L transformed = mapper.map(right, leftClass);
		applyIgnoreFields(transformed);
		
		if(! objectEquals(original, transformed, right)){
			throw new AssertionError("Non loseless mapping from " + leftClass + " to " + rightClass);
		}
	}
	
	/**
	 * Checks, whether Dozer Transformation EnumL->EnumR is lossless.
	 * Transformation is lossless iff l==l' at the transformation l->r->l'
	 */
	private <L, R> void checkLosslessForEnum(Mapper mapper, Class<? extends L> leftClass, Class<? extends R> rightClass){
		for(int i = 0; i < leftClass.getEnumConstants().length; i++){
			if(ignoreFields.contains(leftClass.getEnumConstants()[i].toString())){
				continue;
			}
			L original = leftClass.getEnumConstants()[i];
			R right = mapper.map(original, rightClass);
			L transformed = mapper.map(right, leftClass);
			
			if(! objectEquals(original, transformed, right)){
				throw new AssertionError("Non loseless mapping from " + leftClass + " to " + rightClass);
			}
		}
	}

	private <L> L manufactureInstance(Class<? extends L> instanceClass) {
		
		if(instanceClass.isEnum()){
			int enumConstantsLength = instanceClass.getEnumConstants().length;

			if (enumConstantsLength > 0) {
				int enumIndex = STRATEGY.getIntegerInRange(0, enumConstantsLength, null)
						% enumConstantsLength;
				return instanceClass.getEnumConstants()[enumIndex];
			}
		}
		
		return FACTORY.manufacturePojo(instanceClass);
	}
	
	private void applyCustomGenerators(Object object) {
		
		if(customGenerators.isEmpty()){
			return;
		}
		
		if(object == null){
			return;
		}
		
		if (ClassUtils.isPrimitiveOrWrapper(object.getClass())
				|| Number.class.isAssignableFrom(object.getClass())
				|| Boolean.class.isAssignableFrom(object.getClass())
				|| String.class.isAssignableFrom(object.getClass())){
			return;
		}
		
		Field[] fields = FieldUtils.getAllFields(object.getClass());
		try {
			for (Field field : fields) {				
				
				if (ClassUtils.isPrimitiveOrWrapper(field.getType())
						|| Number.class.isAssignableFrom(field.getType())
						|| Boolean.class.isAssignableFrom(field.getType())
						|| String.class.isAssignableFrom(field.getType())){
					continue;
				}
				
				field.setAccessible(true);
				if(Collection.class.isAssignableFrom(field.getType())){
					for(Object element : (Collection<?>)field.get(object)){
						applyCustomGenerators(element);
					}
					continue;
				}
				
				if(!customGenerators.containsKey(field.getType())){
					if(field.getType().isEnum()){
						continue;
					}
					applyCustomGenerators(field.get(object));
					continue;
				}
				
				field.set(object, customGenerators.get(field.getType()).generate());
			}
		} catch (IllegalAccessException e) {
			LOG.error("Fehler beim Mapping ", e);
			return;
		}
	}
	
	private void applyCustomFieldValues(Object original){
		for(String field : customFieldValues.keySet()){
			Object value = customFieldValues.get(field);
			try {
				org.apache.commons.beanutils.PropertyUtils.setProperty(original, field, value);
			} catch ( SecurityException
					| IllegalArgumentException | IllegalAccessException |  InvocationTargetException | NoSuchMethodException e) {
				LOG.error("Error while setting custom field " + field + "=" + value, e);
				e.printStackTrace();
				throw new AssertionError("Error while setting custom field " + field + "=" + value);
			}
		}
	}
	
	private void applyIgnoreFields(Object original){
		for(String field : ignoreFields){
			try {
				if(field.contains("=")){
					String fieldName = field.split("=")[0];
					String expectedValue = field.split("=")[1];
					Object fieldValue = org.apache.commons.beanutils.PropertyUtils.getProperty(original, fieldName);
					if(fieldValue!= null && fieldValue.toString().equals(expectedValue)){
						org.apache.commons.beanutils.PropertyUtils.setProperty(original, fieldName, null);
					}
				} else {
					org.apache.commons.beanutils.PropertyUtils.setProperty(original, field, null);
				}
			} catch ( SecurityException
					| IllegalArgumentException | IllegalAccessException |  InvocationTargetException | NoSuchMethodException e) {
				LOG.error("Error while setting custom field" + field + "=" + null, e);
				e.printStackTrace();
				throw new AssertionError("Error while setting custom field " + field + "=" + null);
			}
		}
	}
	
	private <T> boolean objectEquals(T object1, T object2, Object destination){
		String str1 = GsonPrinter.prettyString(object1);
		String str2 = GsonPrinter.prettyString(object2);
		if(!str1.equals(str2)){
			switch (logLevel) {
			case OUT:
				log(System.out, object1, object2, destination);
				break;
			
			case ERR:
				log(System.err, object1, object2, destination);
				break;

			default:
				break;
			}
			
			return false;
		}
		
		return true;
	}
	
	private static void log(PrintStream ps, Object object1, Object object2, Object destination){
		ps.println("============================ Mapper Checker Diff Log ===============================");
		ps.println();
		ps.println("From class(A): " + object1.getClass().getName());
		ps.println("To class(B): " + destination.getClass().getName());
		ps.println();
		ps.println("------------------------------------ Diff ------------------------------------------");
		
		String str1 = GsonPrinter.prettyString(object1);
		String str2 = GsonPrinter.prettyString(object2);
		
		List<DiffMatchPath.Diff> diffs = new DiffMatchPath().diff_main(str1, str2);
		for(DiffMatchPath.Diff diff : diffs){
			switch (diff.operation) {
			case DELETE:
				ps.print("[--- " + diff.text + "]");
				break;
				
			case INSERT:
				ps.print("[+++ " + diff.text + "]");
				break;

			default:
				ps.print(diff.text);
				break;
			}
			
		}
		
		ps.println();
		ps.println("------------------------------------ Object a---------------------------------------");
		ps.println(str1);
		ps.println("------------------------------------------------------------------------------------");
		ps.println("                                        |                                           ");
		ps.println("                                        V                                           ");
		ps.println("------------------------------------ Object b---------------------------------------");
		ps.println(GsonPrinter.prettyString(destination));
		ps.println("------------------------------------------------------------------------------------");
		ps.println("                                        |                                           ");
		ps.println("                                        V                                           ");
		ps.println("------------------------------------ Object a'---------------------------------------");
		ps.println(str2);
		ps.println("====================================================================================");
		ps.println();
	}	
}

