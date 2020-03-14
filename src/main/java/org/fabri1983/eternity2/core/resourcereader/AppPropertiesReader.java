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

package org.fabri1983.eternity2.core.resourcereader;

import java.io.IOException;
import java.util.Properties;

public class AppPropertiesReader {

	public static final String MAX_CICLOS_PRINT_STATS = 				"max.cycles.print.stats";
	public static final String ON_MAX_REACHED_SAVE_STATUS = 			"on.max.reached.save.status";
	public static final String MIN_POS_SAVE_PARTIAL = 					"min.pos.save.partial";
	public static final String EXPLORATION_LIMIT = 						"exploration.limit";
	public static final String TARGET_ROLLBACK_POS = 					"target.rollback.pos";
	public static final String UI_SHOW = 								"ui.show";
	public static final String UI_PER_PROC = 							"ui.per.proc";
	public static final String UI_CELL_SIZE = 							"ui.cell.size";
	public static final String UI_REFRESH_MILLIS = 						"ui.refresh.millis";
	public static final String NUM_TASKS = 								"num.tasks";
	
	public static final Properties readProperties() throws IOException {
		Properties properties = new Properties();
		String file = "application.properties";
		properties.load(AppPropertiesReader.class.getClassLoader().getResourceAsStream(file));
		return properties;
	}

	public static final String getProperty(Properties properties, String key) {
		String sysProp = System.getProperty(key);
		if (sysProp != null && !"".equals(sysProp))
			return sysProp;
		return properties.getProperty(key);
	}
	
}
