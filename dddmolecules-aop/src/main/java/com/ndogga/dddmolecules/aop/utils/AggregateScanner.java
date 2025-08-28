package com.ndogga.dddmolecules.aop.utils;

import com.ndogga.dddmolecules.AggregateRoot;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for scanning and extracting AggregateRoot instances from various objects.
 * Used by AOP aspects to find aggregates that may contain domain events to publish.
 */
@UtilityClass
@Slf4j
public class AggregateScanner {
    
    private static final Set<Class<?>> PRIMITIVE_AND_WRAPPER_TYPES = Set.of(
        boolean.class, Boolean.class,
        byte.class, Byte.class,
        char.class, Character.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class,
        String.class
    );
    
    /**
     * Scans multiple objects and returns all found AggregateRoot instances.
     */
    public static Set<AggregateRoot<?>> scanForAggregates(Object... objects) {
        Set<AggregateRoot<?>> aggregates = new HashSet<>();
        Set<Object> visited = new HashSet<>();
        
        for (Object obj : objects) {
            scanObject(obj, aggregates, visited, 0);
        }
        
        return aggregates;
    }
    
    /**
     * Recursively scans an object for AggregateRoot instances.
     * 
     * @param obj the object to scan
     * @param aggregates the set to collect found aggregates
     * @param visited set of already visited objects to prevent infinite recursion
     * @param depth current recursion depth (limited to prevent stack overflow)
     */
    private static void scanObject(Object obj, Set<AggregateRoot<?>> aggregates, Set<Object> visited, int depth) {
        // Prevent infinite recursion
        if (obj == null || visited.contains(obj) || depth > 10) {
            return;
        }
        
        visited.add(obj);
        
        // If it's an aggregate, add it
        if (obj instanceof AggregateRoot<?> aggregate) {
            aggregates.add(aggregate);
            return; // Don't scan inside aggregates to respect boundaries
        }
        
        Class<?> objClass = obj.getClass();
        
        // Skip primitive types and common immutable types
        if (PRIMITIVE_AND_WRAPPER_TYPES.contains(objClass) || objClass.isEnum()) {
            return;
        }
        
        // Handle collections
        if (obj instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                scanObject(item, aggregates, visited, depth + 1);
            }
            return;
        }
        
        // Handle maps
        if (obj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                scanObject(entry.getKey(), aggregates, visited, depth + 1);
                scanObject(entry.getValue(), aggregates, visited, depth + 1);
            }
            return;
        }
        
        // Handle arrays
        if (objClass.isArray()) {
            if (!objClass.getComponentType().isPrimitive()) {
                Object[] array = (Object[]) obj;
                for (Object item : array) {
                    scanObject(item, aggregates, visited, depth + 1);
                }
            }
            return;
        }
        
        // For complex objects, scan fields using reflection
        scanObjectFields(obj, aggregates, visited, depth);
    }
    
    /**
     * Scans object fields using reflection.
     */
    private static void scanObjectFields(Object obj, Set<AggregateRoot<?>> aggregates, Set<Object> visited, int depth) {
        try {
            Class<?> clazz = obj.getClass();
            
            // Skip certain package patterns to avoid scanning framework internals
            String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
            if (packageName.startsWith("java.") || 
                packageName.startsWith("javax.") ||
                packageName.startsWith("jakarta.") ||
                packageName.startsWith("org.springframework.") ||
                packageName.startsWith("org.hibernate.")) {
                return;
            }
            
            // Scan declared fields
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // Skip static and synthetic fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);
                    scanObject(fieldValue, aggregates, visited, depth + 1);
                } catch (Exception e) {
                    // Log warning but continue scanning
                    log.debug("Could not access field {} on {}: {}", field.getName(), clazz.getSimpleName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Error scanning object fields for {}: {}", obj.getClass().getSimpleName(), e.getMessage());
        }
    }
    
    /**
     * Specialized method to scan method parameters for aggregates.
     * This is more efficient than general object scanning.
     */
    public static Set<AggregateRoot<?>> scanMethodParameters(Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return Set.of();
        }
        
        Set<AggregateRoot<?>> aggregates = new HashSet<>();
        for (Object param : parameters) {
            if (param instanceof AggregateRoot<?> aggregate) {
                aggregates.add(aggregate);
            }
        }
        
        return aggregates;
    }
    
    /**
     * Checks if an object is likely to contain aggregates based on its type.
     * Used for optimization to avoid expensive reflection scanning.
     */
    public static boolean mightContainAggregates(Object obj) {
        if (obj == null) return false;
        
        Class<?> clazz = obj.getClass();
        
        // Direct aggregate
        if (obj instanceof AggregateRoot<?>) return true;
        
        // Collections might contain aggregates
        if (obj instanceof Iterable<?> || obj instanceof Map<?, ?> || clazz.isArray()) {
            return true;
        }
        
        // Skip known types that won't contain aggregates
        if (PRIMITIVE_AND_WRAPPER_TYPES.contains(clazz) || clazz.isEnum()) {
            return false;
        }
        
        // Skip framework types
        String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        if (packageName.startsWith("java.") || 
            packageName.startsWith("javax.") ||
            packageName.startsWith("jakarta.") ||
            packageName.startsWith("org.springframework.")) {
            return false;
        }
        
        // Assume other objects might contain aggregates
        return true;
    }
}