/**
 * @author semteu
 *
 * 21 oct. 2015
 * StackedBartChartLoader.java
 */
package fr.ujf.soctrace.tools.analyzer.ted.chart;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.Rotation;

import fr.ujf.soctrace.tools.analyzer.ted.model.TedEvent;

/**
 * @author semteu
 * 21 oct. 2015
 * StackedBartChartLoader.java
 */
public class StackedBarChartLoader {
	
	private Map<Integer, Integer> mapTedEventIdTrace1ToEventOcc;
	private Map<Integer, Integer> mapTedEventIdTrace2ToEventOcc;
	private List<Entry<Integer, Float>> tedEventIdToDistancesList;
	private Map<Integer, String> mapTedEventIdToEventDescr;
	private List<Integer> keyset;
	private String trace1Name;
	private String trace2Name;
	
	public StackedBarChartLoader(){
		mapTedEventIdTrace1ToEventOcc = null;
		mapTedEventIdTrace2ToEventOcc = null;
		keyset = null;
	}
	
	public StackedBarChartLoader(Map<Integer, Integer>mapTedEventIdToOcc1,
			Map<Integer, Integer> mapTedEventIdToOcc2,
			List<Entry<Integer, Float>> distances){
		mapTedEventIdTrace1ToEventOcc = mapTedEventIdToOcc1;
		mapTedEventIdTrace2ToEventOcc = mapTedEventIdToOcc2;
		this.tedEventIdToDistancesList = distances;
		mapTedEventIdToEventDescr = null;
		keyset = new ArrayList<Integer>();
		for(Entry<Integer, Float> e: tedEventIdToDistancesList)
			keyset.add(e.getKey());

	}
	
	public StackedBarChartLoader(	Map<Integer, Integer>mapTedEventIdToOcc1, 
									Map<Integer, Integer>mapTedEventIdToOcc2,
									List<Entry<Integer, Float>> distances,
									Map<Integer, String>mapTedEventIdToEventDescr,
									String trace1Name,
									String trace2Name){
		mapTedEventIdTrace1ToEventOcc = mapTedEventIdToOcc1;
		mapTedEventIdTrace2ToEventOcc = mapTedEventIdToOcc2;
		this.mapTedEventIdToEventDescr = mapTedEventIdToEventDescr;
		tedEventIdToDistancesList = distances;
		keyset = new ArrayList<Integer>();
		for(Entry<Integer, Float> e: tedEventIdToDistancesList)
			keyset.add(e.getKey());
		this.trace1Name = trace1Name + " ";
		this.trace2Name = trace2Name + " ";
	}
	
	public StackedBarChartLoader(	Map<Integer, Integer>mapTedEventIdToOcc1, 
									Map<Integer, Integer>mapTedEventIdToOcc2,
									Set<Integer> eventIdSet,
									Map<Integer, String>mapTedEventIdToEventDescr,
									String trace1Name,
									String trace2Name){
		mapTedEventIdTrace1ToEventOcc = mapTedEventIdToOcc1;
		mapTedEventIdTrace2ToEventOcc = mapTedEventIdToOcc2;
		this.mapTedEventIdToEventDescr = mapTedEventIdToEventDescr;
		keyset = new ArrayList<Integer>();
		for(Integer e: eventIdSet)
			keyset.add(e);
		this.trace1Name = trace1Name + " ";
		this.trace2Name = trace2Name + " ";
	}

	
	/**
	 * Construct the dataset used for the creation of the stack bar chart 
	 * 
	 * @return	A dataset object
	 * 
	 */
	private CategoryDataset constructStackedChartDataset(){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				
		float prop1 = 0;
		float prop2 = 0;
		
		for(Integer key: keyset){
			int n1 = 0;
			int n2 = 0;
			if(mapTedEventIdTrace1ToEventOcc.containsKey(key)){
				n1 = mapTedEventIdTrace1ToEventOcc.get(key);
			}
			if(mapTedEventIdTrace2ToEventOcc.containsKey(key)){
				n2 = mapTedEventIdTrace2ToEventOcc.get(key);
			}
			
			prop1 = n1/(float)(n1 + n2);
			prop2 = n2/(float)(n1 + n2);
			
			System.out.println(mapTedEventIdToEventDescr.get(key) + ": " + prop1);
			System.out.println(mapTedEventIdToEventDescr.get(key) + ": " + prop2);
			
			
			dataset.addValue(prop1, 
					"trace 1",
					"e" + key);
			
			dataset.addValue(prop2, 
					"trace 2",
					"e" + key);
			
		}
		
		return dataset;
	}
    
	public JFreeChart makeStackedChart(){
		return createStackedChart(constructStackedChartDataset());
//		return createStackedChart(createDataset());
	}
	
	public JFreeChart createStackedChart(CategoryDataset dataset){
		
		final JFreeChart chart = ChartFactory.createStackedBarChart(
				"Event proportion", //Chart title
				"Event type", //Domain axis label
				"Percentage", //Range axis label
				dataset, //Data
				PlotOrientation.VERTICAL, //Plot orientation
				true, //Legend
				true, //tooltips
				false
		);
		

		StackedBarRenderer renderer = new StackedBarRenderer();
		renderer.setItemMargin(0.0); //Set the item margin
	
        Paint p1 = new GradientPaint(
            0.0f, 0.0f, new Color(0x22, 0x22, 0xFF), 0.0f, 0.0f, new Color(0x88, 0x88, 0xFF)
        );
        renderer.setSeriesPaint(0, p1);
         
        Paint p2 = new GradientPaint(
            0.0f, 0.0f, new Color(0x22, 0xFF, 0x22), 0.0f, 0.0f, new Color(0x88, 0xFF, 0x88)
        );
        renderer.setSeriesPaint(1, p2); 
        
        renderer.setGradientPaintTransformer(
            new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL)
        );
        
        
        CategoryAxis domainAxis = new CategoryAxis("Event Occurence (%)");
        domainAxis.setCategoryMargin(0.25);        
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDomainAxis(domainAxis);
        
        //plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        plot.setRenderer(renderer);
        plot.setFixedLegendItems(createLegendItems());
    	
		
		return chart;
	}
	
	/**
     * Creates a sample dataset.
     * 
     * @return A sample dataset.
     */
    public CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(20.3, "Product 1 (US)", "Jan 04");
        dataset.addValue(27.2, "Product 1 (US)", "Feb 04");
        dataset.addValue(19.7, "Product 1 (US)", "Mar 04");
        dataset.addValue(19.4, "Product 1 (Europe)", "Jan 04");
        dataset.addValue(10.9, "Product 1 (Europe)", "Feb 04");
        dataset.addValue(18.4, "Product 1 (Europe)", "Mar 04");
        dataset.addValue(16.5, "Product 1 (Asia)", "Jan 04");
        dataset.addValue(15.9, "Product 1 (Asia)", "Feb 04");
        dataset.addValue(16.1, "Product 1 (Asia)", "Mar 04");
        dataset.addValue(13.2, "Product 1 (Middle East)", "Jan 04");
        dataset.addValue(14.4, "Product 1 (Middle East)", "Feb 04");
        dataset.addValue(13.7, "Product 1 (Middle East)", "Mar 04");

        dataset.addValue(23.3, "Product 2 (US)", "Jan 04");
        dataset.addValue(16.2, "Product 2 (US)", "Feb 04");
        dataset.addValue(28.7, "Product 2 (US)", "Mar 04");
        dataset.addValue(12.7, "Product 2 (Europe)", "Jan 04");
        dataset.addValue(17.9, "Product 2 (Europe)", "Feb 04");
        dataset.addValue(12.6, "Product 2 (Europe)", "Mar 04");
        dataset.addValue(15.4, "Product 2 (Asia)", "Jan 04");
        dataset.addValue(21.0, "Product 2 (Asia)", "Feb 04");
        dataset.addValue(11.1, "Product 2 (Asia)", "Mar 04");
        dataset.addValue(23.8, "Product 2 (Middle East)", "Jan 04");
        dataset.addValue(23.4, "Product 2 (Middle East)", "Feb 04");
        dataset.addValue(19.3, "Product 2 (Middle East)", "Mar 04");

        dataset.addValue(11.9, "Product 3 (US)", "Jan 04");
        dataset.addValue(31.0, "Product 3 (US)", "Feb 04");
        dataset.addValue(22.7, "Product 3 (US)", "Mar 04");
        dataset.addValue(15.3, "Product 3 (Europe)", "Jan 04");
        dataset.addValue(14.4, "Product 3 (Europe)", "Feb 04");
        dataset.addValue(25.3, "Product 3 (Europe)", "Mar 04");
        dataset.addValue(23.9, "Product 3 (Asia)", "Jan 04");
        dataset.addValue(19.0, "Product 3 (Asia)", "Feb 04");
        dataset.addValue(10.1, "Product 3 (Asia)", "Mar 04");
        dataset.addValue(13.2, "Product 3 (Middle East)", "Jan 04");
        dataset.addValue(15.5, "Product 3 (Middle East)", "Feb 04");
        dataset.addValue(10.1, "Product 3 (Middle East)", "Mar 04");
        
        return dataset;
    }

	
	
    /**
     * Creates a sample chart.
     * 
     * @param dataset  the dataset for the chart.
     * 
     * @return A sample chart.
     */
    public JFreeChart createChart(CategoryDataset dataset) {

        final JFreeChart chart = ChartFactory.createStackedBarChart(
            "Stacked Bar Chart Demo 4",  // chart title
            "Category",                  // domain axis label
            "Value",                     // range axis label
            dataset,                     // data
            PlotOrientation.VERTICAL,    // the plot orientation
            true,                        // legend
            true,                        // tooltips
            false                        // urls
        );
        
        GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
        KeyToGroupMap map = new KeyToGroupMap("G1");
        map.mapKeyToGroup("Product 1 (US)", "G1");
        map.mapKeyToGroup("Product 1 (Europe)", "G1");
        map.mapKeyToGroup("Product 1 (Asia)", "G1");
        map.mapKeyToGroup("Product 1 (Middle East)", "G1");
        map.mapKeyToGroup("Product 2 (US)", "G2");
        map.mapKeyToGroup("Product 2 (Europe)", "G2");
        map.mapKeyToGroup("Product 2 (Asia)", "G2");
        map.mapKeyToGroup("Product 2 (Middle East)", "G2");
        map.mapKeyToGroup("Product 3 (US)", "G3");
        map.mapKeyToGroup("Product 3 (Europe)", "G3");
        map.mapKeyToGroup("Product 3 (Asia)", "G3");
        map.mapKeyToGroup("Product 3 (Middle East)", "G3");
        renderer.setSeriesToGroupMap(map); 
        
        renderer.setItemMargin(0.0); //Set the item margin
        Paint p1 = new GradientPaint(
            0.0f, 0.0f, new Color(0x22, 0x22, 0xFF), 0.0f, 0.0f, new Color(0x88, 0x88, 0xFF)
        );
        renderer.setSeriesPaint(0, p1);
        renderer.setSeriesPaint(4, p1);
        renderer.setSeriesPaint(8, p1);
         
        Paint p2 = new GradientPaint(
            0.0f, 0.0f, new Color(0x22, 0xFF, 0x22), 0.0f, 0.0f, new Color(0x88, 0xFF, 0x88)
        );
        renderer.setSeriesPaint(1, p2); 
        renderer.setSeriesPaint(5, p2); 
        renderer.setSeriesPaint(9, p2); 
        
        Paint p3 = new GradientPaint(
            0.0f, 0.0f, new Color(0xFF, 0x22, 0x22), 0.0f, 0.0f, new Color(0xFF, 0x88, 0x88)
        );
        renderer.setSeriesPaint(2, p3);
        renderer.setSeriesPaint(6, p3);
        renderer.setSeriesPaint(10, p3);
            
        Paint p4 = new GradientPaint(
            0.0f, 0.0f, new Color(0xFF, 0xFF, 0x22), 0.0f, 0.0f, new Color(0xFF, 0xFF, 0x88)
        );
        renderer.setSeriesPaint(3, p4);
        renderer.setSeriesPaint(7, p4);
        renderer.setSeriesPaint(11, p4);
        renderer.setGradientPaintTransformer(
            new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL)
        );
        
        SubCategoryAxis domainAxis = new SubCategoryAxis("Product / Month");
        domainAxis.setCategoryMargin(0.05);
        domainAxis.addSubCategory("Product 1");
        domainAxis.addSubCategory("Product 2");
        domainAxis.addSubCategory("Product 3");
        
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDomainAxis(domainAxis);
        //plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        plot.setRenderer(renderer);
        plot.setFixedLegendItems(createLegendItems());
    	
    	

        
        return chart;
        
    }
    
    /**
     * Creates the legend items for the chart.  In this case, we set them manually because we
     * only want legend items for a subset of the data series.
     * 
     * @return The legend items.
     */
    private LegendItemCollection createLegendItems() {
        LegendItemCollection result = new LegendItemCollection();
//        LegendItem item1 = new LegendItem("US", new Color(0x22, 0x22, 0xFF));
  //      LegendItem item2 = new LegendItem("Europe", new Color(0x22, 0xFF, 0x22));
    //    LegendItem item3 = new LegendItem("Asia", new Color(0xFF, 0x22, 0x22));
      //  LegendItem item4 = new LegendItem("Middle East", new Color(0xFF, 0xFF, 0x22));
//        result.add(item1);
  //      result.add(item2);
    //    result.add(item3);
      //  result.add(item4);
        LegendItem item1 = new LegendItem(trace1Name, new Color(0x22, 0x22, 0xFF));
        LegendItem item2 = new LegendItem(trace2Name, new Color(0x22, 0xFF, 0x22));
        result.add(item1);
        result.add(item2);
        return result;
    }

	

}
