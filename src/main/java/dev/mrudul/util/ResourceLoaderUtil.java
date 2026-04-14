package dev.mrudul.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceLoaderUtil {

    public static byte[] readBytes(String path) {
        try (InputStream is = ResourceLoaderUtil.class
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }

            return is.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readText(String path) {
        return new String(readBytes(path), StandardCharsets.UTF_8);
    }
}