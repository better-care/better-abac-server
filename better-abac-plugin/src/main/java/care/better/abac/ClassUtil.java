package care.better.abac;

import lombok.NonNull;

/**
 * @author Andrej Dolenc
 */
public final class ClassUtil {
    private ClassUtil() {
    }

    public static <C> C create (@NonNull String className, @NonNull ClassLoader classLoader)
    {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            return (C)clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(String.format("Cannot create instance for class %s", className), e);
        }
    }
}
