package org.fabri1983.eternity2.forkjoin_solver.benchmark;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.tilesreader.ClassLoaderReaderForTilesFile;
import org.fabri1983.eternity2.forkjoin_solver.MainFaster;
import org.fabri1983.eternity2.forkjoin_solver.SolverFaster;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

public class MainFasterBenchmark {

	public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
	@Fork(value = 1)
	public void init(MainFasterBenchmarkContextProvider context) {
		context.solver.atacar(context.timeoutTaskInSecs);
		context.solver.resetInternalStatus();
	}
	
	private static final String getProperty(Properties properties, String key) {
		String sysProp = System.getProperty(key);
		if (sysProp != null && !"".equals(sysProp))
			return sysProp;
		return properties.getProperty(key);
	}
	
	@State(Scope.Benchmark)
	public static class MainFasterBenchmarkContextProvider {

		public long timeoutTaskInSecs = 10;
		public SolverFaster solver;
		
		@Setup(Level.Invocation)
		public void setup() throws IOException {
			
			Properties properties = readProperties();
			
			SolverFaster solver = SolverFaster.build(
					Long.parseLong(getProperty(properties,       "max.ciclos.save_status")),
					Integer.parseInt(getProperty(properties,     "min.pos.save.partial")),
					Integer.parseInt(getProperty(properties,     "exploration.limit")),
					Integer.parseInt(getProperty(properties,     "max.partial.files")),
					Integer.parseInt(getProperty(properties,     "target.rollback.pos")),
					false, // ui.show
					false, // ui.per.proc
					0,     // ui.cell.size
					0,     // ui.refresh.millis
					Boolean.parseBoolean(getProperty(properties, "experimental.gif.fair")),
					Boolean.parseBoolean(getProperty(properties, "experimental.borde.left.explorado")),
					Integer.parseInt(getProperty(properties,     "task.distribution.pos")),
					new ClassLoaderReaderForTilesFile(),
					Integer.parseInt(getProperty(properties,     "forkjoin.num.processes")));
			
			ResourceBundle.clearCache();
			
			System.out.println(); // to get a clean output
			
			solver.setupInicial();
		}
		
		private final Properties readProperties() throws IOException {
			Properties properties = new Properties();
			String file = "application.properties";
			properties.load(MainFaster.class.getClassLoader().getResourceAsStream(file));
			return properties;
		}
	}
	
}
