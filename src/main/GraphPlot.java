package main;
import java.awt.Color;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;


public class GraphPlot extends ApplicationFrame {
	final XYSeriesCollection collection = new XYSeriesCollection();;
	public GraphPlot(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}
	
	public void addThresholdSeries(String label, Map<Integer, Double> data){
		final XYSeries xySeries = new XYSeries(label);
		
		for(int key : data.keySet()){
			xySeries.add(key, data.get(key));
		}
		
		collection.addSeries(xySeries);		
	}
	
	public void plotData(String xAxisLabel){
		final XYDataset dataset = collection;
	    
	    // create the chart...
	    final JFreeChart chart = ChartFactory.createXYLineChart(
	        "Movement along each axis",      // chart title
	        xAxisLabel,                      // x axis label
	        "Distance from origin along axis",                      // y axis label
	        dataset,                  // data
	        PlotOrientation.VERTICAL,
	        true,                     // include legend
	        true,                     // tooltips
	        false                     // urls
	    );
	    
	    
	    chart.setBackgroundPaint(Color.white);
	    
	    LegendTitle legend = chart.getLegend();
	    legend.setPosition(RectangleEdge.TOP);
	    
	    final XYPlot plot = chart.getXYPlot();
	    plot.setBackgroundPaint(Color.lightGray);
//	    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
	    plot.setDomainGridlinePaint(Color.white);
	    plot.setRangeGridlinePaint(Color.white);
	    
	    final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	    renderer.setSeriesLinesVisible(0, true);
	    renderer.setSeriesLinesVisible(1, true);
	    renderer.setSeriesLinesVisible(2, true);
	    plot.setRenderer(renderer);

	    // change the auto tick unit selection to integer units only...
	    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	    // OPTIONAL CUSTOMISATION COMPLETED.
	    
	    final ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new java.awt.Dimension(1300, 675));
	    setContentPane(chartPanel);	
	}
}
