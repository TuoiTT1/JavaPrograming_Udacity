package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

    private final Clock clock;
    private final ProfilingState state = new ProfilingState();
    private final ZonedDateTime startTime;

    @Inject
    ProfilerImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        this.startTime = ZonedDateTime.now(clock);
    }

    @Override
    public <T> T wrap(Class<T> klass, T delegate) {
        Objects.requireNonNull(klass);

        // TODO: Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
        //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
        //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.
        if (!isAnnotatedProfiled(klass)) {
            throw new IllegalArgumentException(klass.getName() + " does not contain a @Profiled method.");
        }
        Object proxy = Proxy.newProxyInstance(
                ProfilerImpl.class.getClassLoader(),
                new Class<?>[]{klass},
                new ProfilingMethodInterceptor(clock, state, delegate, startTime));
        return (T) proxy;
    }

    @Override
    public void writeData(Path path) {
        // TODO: Write the ProfilingState data to the given file path. If a file already exists at that
        //       path, the new data should be appended to the existing file.
        try (FileWriter writer = new FileWriter(path.toFile(), true)) {
            writeData(writer);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Override
    public void writeData(Writer writer) throws IOException {
        writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
        writer.write(System.lineSeparator());
        state.write(writer);
        writer.write(System.lineSeparator());
    }

    private boolean isAnnotatedProfiled(Class<?> klass) {
        Method[] methods = klass.getMethods();
        if (methods.length == 0) {
            return false;
        }
        List<Method> result = Arrays.stream(methods)
                .filter(method -> method.getAnnotation(Profiled.class) != null)
                .limit(1)
                .collect(Collectors.toList());
        return !result.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfilerImpl profiler = (ProfilerImpl) o;
        return Objects.equals(clock, profiler.clock) && Objects.equals(state, profiler.state) && Objects.equals(startTime, profiler.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, state, startTime);
    }
}
