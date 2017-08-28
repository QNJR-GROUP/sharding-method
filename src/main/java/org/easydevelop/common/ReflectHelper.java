package org.easydevelop.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

/** 
* @author xudeyou 
*/
public class ReflectHelper {
	
	private static Logger LOG = LoggerFactory.getLogger(ReflectHelper.class);
	
	private static ConcurrentHashMap<Class<?>, Optional<Constructor<?>>> mapConstructor = new ConcurrentHashMap<>();
	private static boolean hasPublicParameterLessConstructor(Class<?> clazz){
		return getPublicParameterLessConstructor(clazz).isPresent();
	}
	
	public static boolean canInitInstanceWithNoParameter(Class<?> clazz){
		if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())){
			return false;
		}
		return hasPublicParameterLessConstructor(clazz);
	}
	
	private static Optional<Constructor<?>> getPublicParameterLessConstructor(Class<?> clazz){
		Optional<Constructor<?>> result = mapConstructor.get(clazz);
		if(result == null){
			result = mapConstructor.computeIfAbsent(clazz, c->{
				try {
					Constructor<?> constructor = clazz.getConstructor();
					return Optional.of(constructor);
				} catch (NoSuchMethodException | SecurityException e) {
					LOG.info(clazz.getName() + " has no PublicParameterLessConstructor");
					return Optional.empty();
				}
			});
		}
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <R> R createInstance(Class<R> clazz){
		Optional<Constructor<?>> optional = getPublicParameterLessConstructor(clazz);
		if(optional.isPresent()){
			Constructor<?> constructor = optional.get();
			try {
				return (R) constructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Illegal Call!",e);
			}
		} else {
			throw new RuntimeException("Illegal Call!");
		}
	}

}
