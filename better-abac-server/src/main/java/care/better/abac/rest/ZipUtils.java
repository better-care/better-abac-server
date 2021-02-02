package care.better.abac.rest;

import lombok.NonNull;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Andrej Dolenc
 */
public final class ZipUtils {
    private ZipUtils() {
    }

    public static void addTextFileToZip(@NonNull ZipOutputStream stream, @NonNull String fileName, @NonNull String text) throws IOException {
        stream.putNextEntry(new ZipEntry(fileName));
        stream.write(text.getBytes(StandardCharsets.UTF_8));
    }

    public static <T> T readTextFileFromZip(@NonNull ZipInputStream stream, @NonNull TextEntryMapper<T> mapper) throws IOException {
        ZipEntry zipEntry = stream.getNextEntry();
        if (zipEntry != null) {
            String name = zipEntry.getName();
            String value = new String(StreamUtils.copyToByteArray(stream), StandardCharsets.UTF_8);
            return mapper.map(name, value);
        } else {
            return null;
        }
    }

    @FunctionalInterface
    public interface TextEntryMapper<T> {
        T map(String fileName, String text);
    }
}
