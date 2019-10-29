/**
 * Copyright (c) 2019 Fabricio Lettieri fabri1983@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
	@BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
	@Fork(value = 5)
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

		// use a timeout to finish all tasks since those provided by @Warmup and @Measurement don't work
		public long timeoutTaskInSecs = 10;
		// we are going to create and initialize the solver and only benchmark
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
