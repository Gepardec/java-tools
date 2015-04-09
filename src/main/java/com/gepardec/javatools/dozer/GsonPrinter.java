package com.gepardec.javatools.dozer;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonPrinter {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	private static final Gson gsonStr = new GsonBuilder().serializeNulls().create();
	
	public static void printGson(Object o, Gson g){
		Class<?> oClass = o.getClass();
		if(oClass.isPrimitive()
				|| Number.class.isAssignableFrom(oClass)
				|| Boolean.class.isAssignableFrom(oClass)
				|| String.class.isAssignableFrom(oClass)
				|| Date.class.isAssignableFrom(oClass) 
				){
			System.out.println(o.toString());
			return;
		}
		
		System.out.print(o.getClass().getName()+" ");
		System.out.println(g.toJson(o));
	}
	
	public static void print(Object o){
		printGson(o, gson);
	}
	
	public static void printSingleString(Object o){
		printGson(o, gsonStr);
	}
	
	public static String prettyString(Object o){
		return gson.toJson(o);
	}
	
	public static void print(String prefix, Object o){
		System.out.print(prefix + " ");
		print(o);
	}
	
	public static void newLine(){
		System.out.println("");
	}
}
