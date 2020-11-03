package care.better.abac;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrej Dolenc
 */
public class ClassUtilTest {

    @Test
    public void testCreate() {
        ClassUtilTest test = ClassUtil.create("care.better.abac.ClassUtilTest", getClass().getClassLoader());
        Assert.assertNotNull(test);
    }

    @Test(expected = ClassCastException.class)
    public void testCreateForWrongType() {
        TestClass test = ClassUtil.create("care.better.abac.ClassUtilTest", getClass().getClassLoader());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithoutDefaultConstructor() {
        TestClass test = ClassUtil.create("care.better.abac.ClassUtilTest$TestClass", getClass().getClassLoader());
    }

    private class TestClass {
        private TestClass(String string) {
        }
    }
}
