package com.sunyongjie.blog;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Unit tests for the {@link BlogApplication} bootstrap class.
 *
 * <p>
 * Only the bootstrap <em>contract</em> is asserted: the annotations that drive
 * component scanning
 * and MyBatis mapper scanning, plus the entry-point signature. {@code main} is
 * deliberately not
 * invoked — doing so would start the full application context, which needs a
 * live MySQL
 * {@code blog_db} and would make the test environment-dependent and
 * non-deterministic.
 */
class BlogApplicationTest {

    /** The package declared in {@code @MapperScan} on the application class. */
    private static final String MAPPER_PACKAGE = "com.sunyongjie.blog.mapper";

    @Test
    @DisplayName("@SpringBootApplication enables auto-configuration and component scanning")
    void declaresSpringBootApplication() {
        SpringBootApplication annotation = BlogApplication.class.getAnnotation(SpringBootApplication.class);

        assertNotNull(annotation, "BlogApplication must be annotated with @SpringBootApplication");
        assertNotNull(AnnotationUtils.findAnnotation(BlogApplication.class, EnableAutoConfiguration.class),
                "@SpringBootApplication must keep enabling auto-configuration");
        assertNotNull(AnnotationUtils.findAnnotation(BlogApplication.class, ComponentScan.class),
                "@SpringBootApplication must keep enabling component scanning");
    }

    @Test
    @DisplayName("@MapperScan targets the mapper package")
    void declaresMapperScanForMapperPackage() {
        MapperScan mapperScan = BlogApplication.class.getAnnotation(MapperScan.class);

        assertNotNull(mapperScan, "BlogApplication must be annotated with @MapperScan");
        assertArrayEquals(new String[] { MAPPER_PACKAGE }, mapperScan.value());
    }

    @Test
    @DisplayName("main has the standard public static void (String[]) signature")
    void mainHasStandardEntryPointSignature() throws NoSuchMethodException {
        Method main = BlogApplication.class.getMethod("main", String[].class);

        assertTrue(Modifier.isPublic(main.getModifiers()), "main must be public");
        assertTrue(Modifier.isStatic(main.getModifiers()), "main must be static");
        assertArrayEquals(new Class<?>[] { String[].class }, main.getParameterTypes());
    }
}
