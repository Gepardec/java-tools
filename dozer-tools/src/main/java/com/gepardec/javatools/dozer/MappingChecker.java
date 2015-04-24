package com.gepardec.javatools.dozer;
import java.io.PrintStream;
import java.lang.reflect.Field;
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
	
	public MappingChecker(Log logLevel) {
		this.logLevel = logLevel;
		customGenerators = new HashMap<Class<?>, CustomGenerator<?>>();
		customFieldValues = new HashMap<String, Object>();
	}
	
	public MappingChecker() {
		this.logLevel = Log.ERR;
		customGenerators = new HashMap<Class<?>, CustomGenerator<?>>();
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
	
	/**
	 * Checks, whether Dozer Transformation ClassL->ClassR is lossless.
	 * Transformation is lossless iff l==l' at the transformation l->r->l'
	 */
	public <L, R> void checkLossless(Mapper mapper, Class<? extends L> leftClass, Class<? extends R> rightClass){
		L original = FACTORY.manufacturePojo(leftClass);
		applyCustomGenerators(original);
		applyCustomFieldValues(original);
		R right = mapper.map(original, rightClass);
		L transformed = mapper.map(right, leftClass);
		
		if(! objectEquals(original, transformed, right)){
			throw new AssertionError("Nicht verlustfreie Mapping von " + leftClass + " in " + rightClass);
		}
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
				PropertyUtils.setProperty(original, field, value);
			} catch (NoSuchFieldException | SecurityException
					| IllegalArgumentException | IllegalAccessException e) {
				LOG.error("Fehler beim Setzen von custom Feld " + field + "=" + value, e);
				e.printStackTrace();
				throw new AssertionError("Fehler beim Setzen von custom Feld " + field + "=" + value);
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
