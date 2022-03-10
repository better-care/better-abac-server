package care.better.abac.version;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gregor Berger
 */
public class VersionProvider {
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("-SNAPSHOT", Pattern.LITERAL);
    private static String VERSION = "0.0.0";

    static {
        URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/build-info.properties");
        if (url != null) {
            Properties p = new Properties();
            try {
                p.load(url.openStream());
            } catch (IOException ex) {
            }

            VERSION = p.getProperty("build.version", VERSION);
        }

        VERSION = SNAPSHOT_PATTERN.matcher(VERSION).replaceAll(Matcher.quoteReplacement("S"));
    }

    public String getVersion() {
        return VERSION;
    }
}
