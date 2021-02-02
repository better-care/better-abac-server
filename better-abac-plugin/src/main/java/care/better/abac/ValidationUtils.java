package care.better.abac;

import java.util.regex.Pattern;

/**
 * @author Andrej Dolenc
 */
public final class ValidationUtils {

    private static final Pattern URL_PATTERN = Pattern.compile("\\b(https|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private ValidationUtils() {
    }

    public static boolean isValidURL(String baseUrl) {
        return URL_PATTERN.matcher(baseUrl).matches();
    }
}
