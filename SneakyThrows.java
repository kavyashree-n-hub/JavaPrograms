/**
 * Mock annotation for suppressing checked exceptions (Lombok SneakyThrows)
 */
public @interface SneakyThrows {
    Class<? extends Throwable>[] value() default {};
}