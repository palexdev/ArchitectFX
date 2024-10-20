package jmh;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import app.Launcher;
import io.github.palexdev.architectfx.yaml.YamlLoader;
import misc.TestUtils;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class JMHLoadBenchmarks {

    @Test
    void runBenchmarks() throws Exception {
        Options opt = new OptionsBuilder()
            .include(this.getClass().getName() + ".*")
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.MILLISECONDS)
            .warmupIterations(5)
            .measurementIterations(5)
            .threads(1)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .build();
        new Runner(opt).run();
    }

    @Setup(Level.Invocation)
    public void prepare(){
        TestUtils.forceInitFX();
    }

    @Benchmark
    public void load() throws IOException {
        URL res = Launcher.class.getClassLoader().getResource("assets/TextFields.jdsl");
        new YamlLoader()
            .setParallel(false)
            .load(res);
    }

    @Benchmark
    public void loadAsync() throws IOException {
        URL res = Launcher.class.getClassLoader().getResource("assets/TextFields.jdsl");
        new YamlLoader()
            .setParallel(true)
            .load(res);
    }
}
