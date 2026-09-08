package net.lacnic;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;

public final class DomainModelTestUtil {
	private static final String SERIAL_VERSION_UID = "serialVersionUID";

	private DomainModelTestUtil() {
	}

	public static void assertBasicBeanContract(Class<?> type) {
		Assert.assertNotNull(type);
		if (type.isEnum()) {
			Assert.assertTrue(type.getName() + " no define valores de enum", type.getEnumConstants().length > 0);
			return;
		}

		Assert.assertTrue(type.getDeclaredConstructors().length > 0);

		if (Serializable.class.isAssignableFrom(type)) {
			assertSerialVersionUid(type);
		}

		Object instance = instantiateForTesting(type);
		for (Field field : type.getDeclaredFields()) {
			if (field.isSynthetic()) {
				continue;
			}
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (SERIAL_VERSION_UID.equals(field.getName()) && Modifier.isFinal(field.getModifiers())) {
				continue;
			}

			Method getter = findGetter(type, field);
			Assert.assertNotNull(type.getName() + " no tiene getter para " + field.getName(), getter);

			Method setter = findSetter(type, field);
			if (setter != null && instance != null) {
				assertRoundTrip(type, field, getter, setter, instance, createSampleValue(field.getType()));
			}
		}
	}

	private static void assertSerialVersionUid(Class<?> type) {
		try {
			Field serialVersionUid = type.getDeclaredField(SERIAL_VERSION_UID);
			Assert.assertTrue(Modifier.isStatic(serialVersionUid.getModifiers()));
			Assert.assertTrue(Modifier.isFinal(serialVersionUid.getModifiers()));
			Assert.assertEquals(long.class, serialVersionUid.getType());
		} catch (NoSuchFieldException nsfe) {
			Assert.fail(type.getName() + " no define serialVersionUID");
		}
	}

	private static Object createSampleValue(Class<?> fieldType) {
		Class<?> type = normalizePrimitiveType(fieldType);

		if (type == String.class) {
			return "sample";
		}
		if (type == Boolean.class) {
			return Boolean.FALSE;
		}
		if (type == Byte.class) {
			return (byte) 1;
		}
		if (type == Short.class) {
			return (short) 2;
		}
		if (type == Integer.class) {
			return 42;
		}
		if (type == Long.class) {
			return 42L;
		}
		if (type == Float.class) {
			return 1.5f;
		}
		if (type == Double.class) {
			return 3.7d;
		}
		if (type == Character.class) {
			return 'x';
		}
		if (Date.class.isAssignableFrom(type)) {
			return new Date(0L);
		}
		if (type.isEnum()) {
			Object[] values = type.getEnumConstants();
			return values.length > 0 ? values[0] : null;
		}
		if (Map.class.isAssignableFrom(type)) {
			return new HashMap<>();
		}
		if (Set.class.isAssignableFrom(type)) {
			return new HashSet<>();
		}
		if (List.class.isAssignableFrom(type)) {
			return new ArrayList<>();
		}
		return null;
	}

	private static void assertRoundTrip(Class<?> type, Field field, Method getter, Method setter, Object instance, Object value) {
		try {
			setter.setAccessible(true);
			getter.setAccessible(true);

			setter.invoke(instance, value);
			Object readValue = getter.invoke(instance);
			Assert.assertEquals(type.getName() + " no conserva el valor de " + field.getName(), value, readValue);
		} catch (ReflectiveOperationException roe) {
			Assert.fail(type.getName() + " no pudo ejecutar set/get para " + field.getName() + ": " + roe.getMessage());
		}
	}

	private static Method findGetter(Class<?> type, Field field) {
		String capitalized = capitalize(field.getName());
		String getter = "get" + capitalized;
		String boolGetter = "is" + capitalized;

		Method method = findMethod(type, boolGetter);
		if (method != null && normalizePrimitiveType(method.getReturnType()) == normalizePrimitiveType(field.getType()) && method.getParameterCount() == 0) {
			return method;
		}

		return findMethod(type, getter);
	}

	private static Method findSetter(Class<?> type, Field field) {
		String capitalized = capitalize(field.getName());
		String setter = "set" + capitalized;
		for (Method method : type.getMethods()) {
			if (!method.getName().equals(setter) || method.getParameterCount() != 1) {
				continue;
			}
			if (normalizePrimitiveType(method.getParameterTypes()[0]).equals(normalizePrimitiveType(field.getType()))) {
				return method;
			}
		}
		return null;
	}

	private static Method findMethod(Class<?> type, String name) {
		try {
			return type.getMethod(name);
		} catch (NoSuchMethodException nsme) {
			return null;
		}
	}

	private static Object instantiateForTesting(Class<?> type) {
		try {
			Constructor<?> ctor = type.getDeclaredConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		} catch (NoSuchMethodException nsme) {
			return instantiateFromStaticFactory(type);
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

	private static Object instantiateFromStaticFactory(Class<?> type) {
		for (Method method : type.getMethods()) {
			if (!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0
					|| !method.getReturnType().equals(type)) {
				continue;
			}
			try {
				return method.invoke(null);
			} catch (ReflectiveOperationException e) {
				return null;
			}
		}
		return null;
	}

	private static String capitalize(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	private static Class<?> normalizePrimitiveType(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == int.class) {
			return Integer.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == boolean.class) {
			return Boolean.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == char.class) {
			return Character.class;
		}
		if (type == void.class) {
			return Void.class;
		}
		return type;
	}
}

