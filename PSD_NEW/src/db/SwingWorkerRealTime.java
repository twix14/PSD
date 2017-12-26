/**
 * Copyright 2015-2017 Knowm Inc. (http://knowm.org) and contributors.
 * Copyright 2011-2015 Xeiam LLC (http://xeiam.com) and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package db;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

/**
 * Creates a real-time chart using SwingWorker
 */
public class SwingWorkerRealTime extends SwingWorker<Boolean, double[]>{

	final LinkedList<Double> fifo = new LinkedList<Double>();
	SwingWrapper<XYChart> sw;
	XYChart chart;
	IWideBoxDB db;

	public SwingWorkerRealTime(SwingWrapper<XYChart> sw, XYChart chart, IWideBoxDB db) {
		this.sw = sw;
		this.chart = chart;
		this.db = db;
		fifo.add(0.0);
	}

	@Override
	protected Boolean doInBackground() throws Exception {

		while (!isCancelled()) {

			int req1 = db.getRequests();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// eat it. caught when interrupt is called
				System.out.println("MySwingWorker shut down.");
			}
			
			int req2 = db.getRequests();
			fifo.add((double) (req2-req1));
			if (fifo.size() > 500) {
				fifo.removeFirst();
			}
			
			double[] array = new double[fifo.size()];
			for (int i = 0; i < fifo.size(); i++) {
				array[i] = fifo.get(i);
			}
			publish(array);
		}

		return true;
	}

	@Override
	protected void process(List<double[]> chunks) {

		double[] mostRecentDataSet = chunks.get(chunks.size() - 1);

		chart.updateXYSeries("Rates", null, mostRecentDataSet, null);
		sw.repaintChart();

		long start = System.currentTimeMillis();
		long duration = System.currentTimeMillis() - start;
		try {
			Thread.sleep(40 - duration); // 40 ms ==> 25fps
			// Thread.sleep(400 - duration); // 40 ms ==> 2.5fps
		} catch (InterruptedException e) {
			System.out.println("InterruptedException occurred.");
		}
	}
}
