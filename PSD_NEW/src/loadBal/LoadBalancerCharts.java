package loadBal;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

public class LoadBalancerCharts extends SwingWorker<Boolean, double[]>{

	final LinkedList<Double> fifo = new LinkedList<Double>();
	SwingWrapper<XYChart> sw;
	XYChart chart;
	ILoadBalancer lb;

	public LoadBalancerCharts(SwingWrapper<XYChart> sw, XYChart chart, ILoadBalancer lb) {
		this.sw = sw;
		this.chart = chart;
		this.lb = lb;
		fifo.add(20.0);
	}

	@Override
	protected Boolean doInBackground() throws Exception {

		while (!isCancelled()) {

			int req1 = lb.getRequests();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// eat it. caught when interrupt is called
				System.out.println("MySwingWorker shut down.");
			}

			int req2 = lb.getRequests();
			long latency = lb.getLatencty();

			int avgLatency = (int) (latency/((req2-req1)+1))+1;
			System.out.println(avgLatency);
			fifo.add((double) (req2-req1));
			//fifo.add((double) 0);
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

		chart.updateXYSeries("Throughput", null, mostRecentDataSet, null);
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

