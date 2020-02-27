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

package org.fabri1983.eternity2.faster.benchmark;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.resourcereader.AppPropertiesReader;
import org.fabri1983.eternity2.core.resourcereader.ClassLoaderReaderForFile;
import org.fabri1983.eternity2.faster.SolverFaster;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

public class MainFasterBenchmark {

	public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 3)
    @Measurement(iterations = 3)
	@Fork(value = 1)
	public void atacar(MainFasterBenchmarkContextProvider context) {
		context.solver.atacarForBenchmark(context.timeoutTaskInSecs);
	}
	
	@State(Scope.Benchmark)
	public static class MainFasterBenchmarkContextProvider {

		// use a timeout to finish all tasks since those provided by @Warmup and @Measurement don't work
		public long timeoutTaskInSecs = 10;
		// we are going to create and initialize the solver and only benchmark
		public SolverFaster solver;
		
		@Setup(Level.Iteration)
		public void setup() throws IOException {
			
			Properties properties = AppPropertiesReader.readProperties();
			
			solver = SolverFaster.build(
					Long.parseLong(getProperty(properties,       AppPropertiesReader.MAX_CICLOS_PRINT_STATS)),
					Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.MAX_CICLOS_SAVE_STATUS)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.MIN_POS_SAVE_PARTIAL)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.EXPLORATION_LIMIT)),
					Integer.parseInt(getProperty(properties,     AppPropertiesReader.MAX_PARTIAL_FILES)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.TARGET_ROLLBACK_POS)),
					false, // AppPropertiesReader.UI_SHOW
					false, // AppPropertiesReader.UI_PER_PROC
					0,     // AppPropertiesReader.UI_CELL_SIZE
					0,     // AppPropertiesReader.UI_REFRESH_MILLIS)),
					Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.EXPERIMENTAL_GIF_FAIR)),
					Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.EXPERIMENTAL_BORDE_LEFT_EXPLORADO)),
					Integer.parseInt(getProperty(properties,     AppPropertiesReader.TASK_DISTRIBUTION_POS)),
					Integer.parseInt(getProperty(properties,     AppPropertiesReader.NUM_TASKS)));
			
			properties = null;
			
			System.out.println(); // to get a clean output
			
			solver.setupInicial(new ClassLoaderReaderForFile());
			ResourceBundle.clearCache();
		}
		
		private String getProperty(Properties properties, String key) {
			return AppPropertiesReader.getProperty(properties, key);
		}

		@TearDown(Level.Iteration)
		public void doTearDown() {
			solver.resetForBenchmark();
			System.gc();
		}
		
	}
	
}
