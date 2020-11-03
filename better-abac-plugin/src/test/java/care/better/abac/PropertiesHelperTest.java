package care.better.abac;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

/**
 * @author Andrej Dolenc
 */
public class PropertiesHelperTest {

    final Properties properties = new Properties();

    @Before
    public void setUp() {
        properties.clear();
        properties.setProperty("a.b.c1", "1");
        properties.setProperty("a.b.c2", "2");
        properties.setProperty("a.d.c1", "3");
    }

    @Test
    public void testGetPropertiesByPrefix() {
        Properties withPrefix = PropertiesHelper.getPropertiesByPrefix(properties, "a.b", true);
        Properties withoutPrefix = PropertiesHelper.getPropertiesByPrefix(properties, "a.b", false);

        Assert.assertEquals("1", withPrefix.getProperty("a.b.c1"));
        Assert.assertEquals("2", withPrefix.getProperty("a.b.c2"));
        Assert.assertNull(withPrefix.getProperty("a.d"));

        Assert.assertEquals("1", withoutPrefix.getProperty("c1"));
        Assert.assertEquals("2", withoutPrefix.getProperty("c2"));
        Assert.assertNull(withoutPrefix.getProperty("a.d"));
    }

    @Test
    public void testGetPropertyMap() {
        Map<String, Properties> propertyMap = PropertiesHelper.getPropertyMap(properties, "a");

        Properties propertyMap1 = propertyMap.get("b");
        Properties propertyMap2 = propertyMap.get("d");

        Assert.assertNotNull(propertyMap1);
        Assert.assertNotNull(propertyMap2);

        Assert.assertEquals("1", propertyMap1.getProperty("c1"));
        Assert.assertEquals("2", propertyMap1.getProperty("c2"));
        Assert.assertEquals("3", propertyMap2.getProperty("c1"));
    }
}

