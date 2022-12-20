package care.better.abac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Andrej Dolenc
 */
public class PropertiesHelperTest {

    final Properties properties = new Properties();

    @BeforeEach
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

        assertThat(withPrefix.getProperty("a.b.c1")).isEqualTo("1");
        assertThat(withPrefix.getProperty("a.b.c2")).isEqualTo("2");
        assertThat(withPrefix.getProperty("a.d")).isNull();

        assertThat(withoutPrefix.getProperty("c1")).isEqualTo("1");
        assertThat(withoutPrefix.getProperty("c2")).isEqualTo("2");
        assertThat(withoutPrefix.getProperty("a.d")).isNull();
    }

    @Test
    public void testGetPropertyMap() {
        Map<String, Properties> propertyMap = PropertiesHelper.getPropertyMap(properties, "a");

        Properties propertyMap1 = propertyMap.get("b");
        Properties propertyMap2 = propertyMap.get("d");

        assertThat(propertyMap1).isNotNull();
        assertThat(propertyMap2).isNotNull();

        assertThat(propertyMap1.getProperty("c1")).isEqualTo("1");
        assertThat(propertyMap1.getProperty("c2")).isEqualTo("2");
        assertThat(propertyMap2.getProperty("c1")).isEqualTo("3");
    }
}

