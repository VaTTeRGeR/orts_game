package de.vatterger.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import de.vatterger.engine.util.UnsafeUtil;
import sun.misc.Unsafe;


public class ReflectionTest {

	@Serialize
	public float x = 0.1f;
	public String s = "Hello";
	
	public ReflectionTest() {
		
	}

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		ReflectionTest rt = new ReflectionTest();
		
		Field fx = ReflectionTest.class.getField("x");
		Field fs = ReflectionTest.class.getField("s");
		
		System.out.println(fx.getFloat(rt));
		String s = (String)fs.get(rt);
		
		System.out.println(s);
		
		System.out.println("Serialize " + fx.getName() + ": " + fx.isAnnotationPresent(Serialize.class));
		System.out.println("Serialize " + fs.getName() + ": " + fs.isAnnotationPresent(Serialize.class));
		
		Unsafe u = UnsafeUtil.getUnsafe();
		
		long fxFieldOffset = u.objectFieldOffset(fx);
		
		System.out.println("X via Unsafe: " + u.getFloat(rt, fxFieldOffset));
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Serialize {
		
	}
}
