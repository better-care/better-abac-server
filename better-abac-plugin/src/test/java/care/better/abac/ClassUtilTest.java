package care.better.abac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Andrej Dolenc
 */
public class ClassUtilTest {

    @Test
    public void testCreate() {
        ClassUtilTest test = ClassUtil.create("care.better.abac.ClassUtilTest", getClass().getClassLoader());
        assertThat(test).isNotNull();
    }

    @Test
    public void testCreateForWrongType() {
        Assertions.assertThrows(ClassCastException.class, () -> {
            ClassUtil.create("care.better.abac.ClassUtilTest", getClass().getClassLoader());
        });
    }

    @Test
    public void testCreateWithoutDefaultConstructor() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ClassUtil.create("care.better.abac.ClassUtilTest$TestClass", getClass().getClassLoader());
        });
    }

    private class TestClass {
        private TestClass(String string) {
        }
    }
}
