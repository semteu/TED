package fr.ujf.soctrace.tools.analyzer.ted.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeLabelProvider;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.query.iterators.EventIterator;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedConstants;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedInput;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedStatus;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedInput.InputStatus;
import fr.ujf.soctrace.tools.analyzer.ted.controller.TedInput.Operation;
import fr.ujf.soctrace.tools.analyzer.ted.model.DataNode;
import fr.ujf.soctrace.tools.analyzer.ted.model.GStreamerTedAdapter;
import fr.ujf.soctrace.tools.analyzer.ted.model.GStreamerTedIterator;
import fr.ujf.soctrace.tools.analyzer.ted.model.KPTraceTedAdapter;
import fr.ujf.soctrace.tools.analyzer.ted.model.KPTraceTedIterator;
import fr.ujf.soctrace.tools.analyzer.ted.model.TedAdapter;
import fr.ujf.soctrace.tools.analyzer.ted.operation.TedProcessor;


public class TedMainView extends ViewPart implements IFramesocBusListener{

	private final static Logger logger = LoggerFactory.getLogger(TedMainView.class);

	/**
	 * View ID as defined in the manifest
	 */
	public static final String ID = "fr.ujf.soctrace.tools.analyzer.ted.ui.TedMainView"; //$NON-NLS-1$
	
	/**
	 * TedTool
	 */
	private Tool tedTool= null;

	/**
	 * Map: key <-> Trace
	 */
	
	private Map<Integer, Trace> mapTraces;
	
	/**
	 * List of traces
	 */
	private List<Trace> traces;

	/**
	 * Manage the bus topics
	 */
	private FramesocBusTopicList topics;

	/**
	 * The input for the TED algorithm
	 */
	private TedInput currentInput;
	
	/**
	 * Ted user interface components 
	 */
	
	// Header components
	private Label lblTitle;
	
	//Trace Selection components
	private Label lblRefTrace;
	private Label lblDiagTrace;
	private Label lblDataModel;
	
	private Combo cmbRefTrace;
	private Combo cmbDiagTrace;
	private Combo cmbDataModel;
	
	//Distance type components selection
	private Button btnDropDistance;
	private Button btnOccDistance;
	private Button btnTempDistance;
	private Button btnAnyAnomaly;
	private Button btnAllAnomalies;
	
	//Threshold input component
	private Label lblThreshold;
	
	private Text txtThreshold;
	
	//Command buttons components
	private Button btnDiagnose;
	private Button btnReset;
	
	//Result view 
	private Label lblChartView;
	private Label lblResults;
	private Label lblDecision;
	

	private TreeViewer treeviewResults;
	private Text txtDecision; 
	
	
	private ChartComposite barChartView;
	
	private DataNode treeNode;
	
	
	Display display = Display.getCurrent();
	Color blue = display.getSystemColor(SWT.COLOR_BLUE);
	Color red = display.getSystemColor(SWT.COLOR_RED);
	Color gray = display.getSystemColor(SWT.COLOR_GRAY);
	Color green = display.getSystemColor(SWT.COLOR_GREEN);
	Color yellow = display.getSystemColor(SWT.COLOR_YELLOW);
	Color white = display.getSystemColor(SWT.COLOR_WHITE);
	
	/**
	 * 
	 * @throws SoCTraceException
	 */
	public TedMainView() {
		//Data structures initialization
		mapTraces = new TreeMap<Integer, Trace>();
		currentInput = new TedInput();
		
		// Register the topic necessary to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED);
		topics.registerAll();
		
		//Loading data
		loadingDataFromFramesoc();
		
		treeNode = null;
		
	}
	
	/**
	 * This method allow to connect to Framesoc database and to load main data
	 * like trace list
	 */
	private void loadingDataFromFramesoc(){
		ITraceSearch search = null;
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting())
				return;
			search = new TraceSearch().initialize();
			tedTool = search.getToolByName(TedConstants.TOOL_NAME);
			if (tedTool == null) {
				MessageDialog.openError(getSite().getShell(), "Error",
						"TED is not registered to FrameSoC!");
			}
			update(search);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			try {
				if (search != null)
					search.uninitialize();
			} catch (SoCTraceException e) {
				logger.error("Unable to uninitialize the search interface");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update infos about traces stored in the Framesoc trace database
	 * 
	 * @param traceSearch
	 * 					search interface object
	 * @throws SoCTraceException
	 */
	private void update(ITraceSearch traceSearch) throws SoCTraceException{
		
		mapTraces.clear();
		
		//Get traces from Framesoc database
		List<Trace> traces = traceSearch.getTraces();
		int key = 0;
		for(Trace trace : traces){
			mapTraces.put(key, trace);
			key++;	
		}
		
	}
	
	/**
	 * This function initializes the UI components of form header 	
	 * @param parent
	 */
	private void initializeHeaderUIComponents(Composite parent){
		
		Composite cmpHeaderContainer = new Composite(parent, SWT.NONE);
		cmpHeaderContainer.setLayout(new GridLayout(1, false));
		cmpHeaderContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true, false, 1, 1));
		
		lblTitle =  new Label(cmpHeaderContainer, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		FontData[] fontData = lblTitle.getFont().getFontData();
		fontData[0].setHeight(14);
		lblTitle.setFont( new Font(display, fontData[0]));
		lblTitle.setText("TED - Trace Diagnosis");		
	}
	
	
	private void initializeLeftPartView(Composite parent){
		
		Composite cmpLeftPart = new Composite(parent, SWT.NONE);
		cmpLeftPart.setLayout(new GridLayout(2,false));
		
		lblRefTrace = new Label(cmpLeftPart, SWT.NONE);
		lblRefTrace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblRefTrace.setText("Reference Trace:");
		
		cmbRefTrace = new Combo(cmpLeftPart, SWT.READ_ONLY);
		cmbRefTrace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		lblDiagTrace = new Label(cmpLeftPart, SWT.NONE);
		lblDiagTrace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDiagTrace.setText("Suspicious Trace:");
		
		cmbDiagTrace = new Combo(cmpLeftPart, SWT.READ_ONLY);
		cmbDiagTrace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		cmbDiagTrace.addSelectionListener(listener);
		
		lblDataModel = new Label(cmpLeftPart, SWT.NONE);
		lblDataModel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDataModel.setText("Data model processing:");
		
		cmbDataModel = new Combo(cmpLeftPart, SWT.READ_ONLY);
		cmbDataModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		
		Group grpFrameAnomalyDetectionType = new Group(cmpLeftPart, SWT.NONE | SWT.NO_BACKGROUND);
		grpFrameAnomalyDetectionType.setLayout(new GridLayout(1, false));
		grpFrameAnomalyDetectionType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		grpFrameAnomalyDetectionType.setText("Anomaly Type");
		
		Composite cmpAnomalyType = new Composite(grpFrameAnomalyDetectionType, SWT.NONE);
		cmpAnomalyType.setLayout(new GridLayout(3, false));
		cmpAnomalyType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
				
		btnOccDistance = new Button(cmpAnomalyType,SWT.RADIO);
		btnOccDistance.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnOccDistance.setText("1. A/V/S Desynchronization");
//		btnDropDistance.addSelectionListener(listener);
		
		btnDropDistance = new Button(cmpAnomalyType,SWT.RADIO);
		btnDropDistance.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnDropDistance.setText("2. Player crash");
//		btnDropDistance.addSelectionListener(listener);
		
		btnTempDistance = new Button(cmpAnomalyType,SWT.RADIO);
		btnTempDistance.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnTempDistance.setText("3. Slow stream");
//		btnTempDistance.addSelectionListener(listener);
		
		btnAnyAnomaly = new Button(cmpAnomalyType,SWT.RADIO);
		btnAnyAnomaly.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnAnyAnomaly.setText("4. Any of 1, 2 or 3");
//		btnTempDistance.addSelectionListener(listener);
		
		btnAllAnomalies = new Button(cmpAnomalyType,SWT.RADIO);
		btnAllAnomalies.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnAllAnomalies.setText("5. All (1, 2 and 3)");
//		btnTempDistance.addSelectionListener(listener);
		
		lblThreshold = new Label(cmpLeftPart, SWT.NONE);
		lblThreshold.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblThreshold.setText("Threshold:");
		
		txtThreshold = new Text(cmpLeftPart, SWT.RIGHT| SWT.BORDER);
		txtThreshold.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		txtThreshold.setText("0");
		
		Composite cmpCommandLayout = new Composite(cmpLeftPart, SWT.NONE);
		cmpCommandLayout.setLayout(new GridLayout(3, false));
		cmpCommandLayout.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 2, 1));
		
		btnDiagnose =  new Button(cmpLeftPart, SWT.NONE);
		btnDiagnose.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
		btnDiagnose.setText("Diagnose");
		btnDiagnose.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void  widgetSelected(SelectionEvent e){
				runDiagnostic();
			}
		});
				
		
		
	}
	
	private void initializeRightPartView(Composite parent){
		Composite cmpRightPart = new Composite(parent, SWT.NONE);
		cmpRightPart.setLayout(new GridLayout(2,false));
		
		lblResults = new Label(cmpRightPart, SWT.NONE);
		lblResults.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblResults.setText("Results");
		
		treeviewResults = new TreeViewer(cmpRightPart, SWT.BORDER);
		treeviewResults.setContentProvider(new TreeContentProvider());
		treeviewResults.setLabelProvider(new TreeLabelProvider());
		Tree treeResults = treeviewResults.getTree();
		treeResults.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		lblDecision = new Label(cmpRightPart, SWT.NONE);
		lblDecision.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDecision.setText("Decision");
		
		txtDecision = new Text(cmpRightPart, SWT.CENTER | SWT.READ_ONLY);
		txtDecision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDecision.setText("");
		
		lblChartView = new Label(cmpRightPart, SWT.NONE);
		lblChartView.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		lblChartView.setText("Result chart view");
		
		//TODO:: Remove txtProcessing view
//		txtProcessingView =  new Text(cmpRightPart, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
//		GridData gData =  new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
//		gData.heightHint = MAX_NUMBER_LINES * txtProcessingView.getLineHeight();
//		txtProcessingView.setLayoutData(gData);

		
		Composite chartCmpLayout = new Composite(cmpRightPart, SWT.NONE);
		chartCmpLayout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		chartCmpLayout.setLayout(new GridLayout(2, false));
		
		barChartView = new ChartComposite(chartCmpLayout, SWT.NONE);
		barChartView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		barChartView.setChart(null);
		
		
		
		Composite cmpCommandLayout = new Composite(cmpRightPart, SWT.NONE);
		cmpCommandLayout.setLayout(new GridLayout(2, false));
		cmpCommandLayout.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 2, 1));
		
//		btnSave =  new Button(cmpCommandLayout, SWT.NONE);
//		btnSave.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
//		btnSave.setText("Save");
		
		btnReset =  new Button(cmpCommandLayout, SWT.NONE);
		btnReset.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnReset.setText("Reset");
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				initializeView();
			}
		});
		
		//Initialize Components
		initializeView();
				
	}
	
	private void initializeView(){
		
//		loadingDataFromFramesoc();
		
		refreshTraceList();
		initializeTraceCombo(cmbRefTrace);
		initializeTraceCombo(cmbDiagTrace);
		initializeDataModelCombo();
		
		btnAllAnomalies.setSelection(false);
		btnAnyAnomaly.setSelection(false);
		btnDropDistance.setSelection(false);
		btnOccDistance.setSelection(false);
		btnTempDistance.setSelection(false);
		txtThreshold.setText("0");
		treeviewResults.setInput(null);
		treeviewResults.refresh();
		txtDecision.setText("");
		lblResults.setText("Results");
		barChartView.setChart(null);
		barChartView.forceRedraw();
		
		
	}
	
	private Trace getTrace(Combo combo){
	
		if(combo.getSelectionIndex() == -1 || !mapTraces.containsKey(combo.getSelectionIndex()))
			return null;
		
		return mapTraces.get(combo.getSelectionIndex());
		
	}
	
	
	
	/**
	 * This method set the input status of the current view
	 * @return the input status of the view
	 */
	private InputStatus checkInput(){
		
		currentInput.refTrace = getTrace(cmbRefTrace);
		if(currentInput.refTrace == null)
			return InputStatus.INPUT_NO_REFTRACE;
		
		currentInput.diagTrace = getTrace(cmbDiagTrace);
		if(currentInput.diagTrace == null)
			return InputStatus.INPUT_NO_DIAGTRACE;
		
		if(currentInput.refTrace.getType().getId() != currentInput.diagTrace.getType().getId())
			return InputStatus.INPUT_DIFF_TRACETYPE;
		
		currentInput.eventCategory = cmbDataModel.getSelectionIndex();
		if(currentInput.eventCategory != EventCategory.LINK && 
				currentInput.eventCategory != EventCategory.STATE &&
				currentInput.eventCategory != EventCategory.PUNCTUAL_EVENT){
			return InputStatus.INPUT_NO_DATAMODELSELECTED;
		}
		
		currentInput.threshold = Float.valueOf(txtThreshold.getText());
		
		if(btnOccDistance.getSelection()){
			if(currentInput.threshold <= 0)
				return InputStatus.INPUT_BAD_THRESHOLD;
			
			currentInput.operation = Operation.OCCURRENCE_DISTANCE;
		}
		else if(btnDropDistance.getSelection()){
			if(currentInput.threshold < 0)
				return InputStatus.INPUT_BAD_THRESHOLD;
			
			currentInput.operation = Operation.DROPPING_DISTANCE;
		}
		else if(btnTempDistance.getSelection()){
			if(currentInput.threshold < 0)
				return InputStatus.INPUT_BAD_THRESHOLD;
			
			currentInput.operation = Operation.TEMPORAL_DISTANCE;
		}
		else if (btnAnyAnomaly.getSelection()) {
			if(currentInput.threshold <= 0)
				return InputStatus.INPUT_BAD_THRESHOLD;
			
			currentInput.operation = Operation.ANY_DISTANCE;
		}
		else if (btnAllAnomalies.getSelection()) {
			if(currentInput.threshold <= 0)
				return InputStatus.INPUT_BAD_THRESHOLD;
			
			currentInput.operation = Operation.ALL_DISTANCE;
		}
		else {
			return InputStatus.INPUT_NO_OPERATIONSELECTED;
		}
		
		return InputStatus.INPUT_OK;
		
	}
	
	/**
	 * Method used to launch job for processing distance
	 */
	private void runDiagnostic(){
		
		InputStatus inputStatus = checkInput();
		
		if(inputStatus == InputStatus.INPUT_NO_REFTRACE){
			MessageDialog.openError(getSite().getShell(), "Parameter Error",
					"No reference trace selected !");
			cmbRefTrace.setFocus();
			return;
		}
		else if (inputStatus == InputStatus.INPUT_BAD_REFTRACE) {
			MessageDialog.openError(getSite().getShell(), "Parameter Error",
					"Bad reference trace selected !");
			cmbRefTrace.setFocus();
			return;
		}
		else if (inputStatus == InputStatus.INPUT_NO_DIAGTRACE) {
			MessageDialog.openError(getSite().getShell(), "Parameter Error",
					"No trace to diagnose selected !");
			cmbDiagTrace.setFocus();
			return;
		}
		else if (inputStatus == InputStatus.INPUT_BAD_DIAGTRACE) {
			MessageDialog.openError(getSite().getShell(), "Parameter Error",
					"Bad trace to diagnose selected !");
			cmbDiagTrace.setFocus();
			return;
		}
		else if(inputStatus == InputStatus.INPUT_DIFF_TRACETYPE){
			MessageDialog.openError(getSite().getShell(), "Parameter Error", 
					"The traces selected are not the same type !");
			cmbRefTrace.setFocus();
			return;
		}
		else if(inputStatus == InputStatus.INPUT_NO_DATAMODELSELECTED){
			MessageDialog.openError(getSite().getShell(), "Parameter Error", 
					"Bad or no data model processing selected !");
			cmbDataModel.setFocus();
			return;
		}
		else if (inputStatus == InputStatus.INPUT_NO_OPERATIONSELECTED) {
			MessageDialog.openError(getSite().getShell(), "Parameter Error",
					"No comparison operator selected !");
			return;
		}
		else if (inputStatus == InputStatus.INPUT_BAD_THRESHOLD) {
			MessageDialog.openError(getSite().getShell(), "Parameter Error",
					"Bad threshold provided ! ");
			txtThreshold.selectAll();
			txtThreshold.setFocus();
			return;
		}
		
		txtDecision.setText("");
		barChartView.setChart(null);
		barChartView.forceRedraw();
		
		//Launch TED algorithm processing
		//Launch a new job
		
		Job job = new Job("TED Tool"){
			
			@Override
			public IStatus run(IProgressMonitor monitor){
				
				monitor.beginTask("TED Tool", IProgressMonitor.UNKNOWN);
				
				TraceDBObject traceDB1 = null;
				TraceDBObject traceDB2 = null;
				
				
				try{
					
					traceDB1 = TraceDBObject.openNewInstance(currentInput.refTrace.getDbName());
					currentInput.refAdapter = loadNewAdapter(traceDB1, currentInput.refTrace);
					
					traceDB2 = TraceDBObject.openNewInstance(currentInput.diagTrace.getDbName());
					currentInput.diagAdapter = loadNewAdapter(traceDB2, currentInput.diagTrace);
					
					treeNode = new DataNode("Comparison results");
					
										
					final TedProcessor processor = new TedProcessor(currentInput, barChartView,
							txtDecision, treeNode);
					
					final TedStatus status = processor.run(monitor);
										
					traceDB1.close();
					traceDB2.close();
					monitor.done();
				}
				catch(Exception e){
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				finally{
					logger.debug("clear the adapter and close the DB");
					DBObject.finalClose(traceDB1);						
					adapterClear(currentInput.refAdapter);
					DBObject.finalClose(traceDB2);
					adapterClear(currentInput.diagAdapter);
//					Display.getDefault().syncExec(new Runnable() {
//						@Override
//						public void run() {
//						}
//					});
				}
				
				
				Display.getDefault().syncExec(new Runnable() {
				
					@Override
					public void run() {
						lblResults.setText("Comparison results between " + currentInput.refTrace.getAlias() +
								" and " + currentInput.diagTrace.getAlias());
						updateTreeViewResults();

					}
				});


				
				
				return Status.OK_STATUS;
				
			}
			
		};
		
		job.setUser(true);
		job.schedule();
		
	}
	
	/**
	 * Load the TedAdapter corresponding to the passed trace DB. Default TedAdapter is the GStreamer one.
	 */
	private TedAdapter loadNewAdapter(TraceDBObject traceDB, Trace trace) throws SoCTraceException {
		TedAdapter adapter = null;
		EventIterator iterator = null;
		if (trace.getType().getName().toLowerCase().startsWith("GStreamer.hadas".toLowerCase())) {
			iterator = new GStreamerTedIterator(traceDB);
			adapter = new GStreamerTedAdapter(traceDB, iterator);
		} else if (trace.getType().getName().toLowerCase().startsWith("com.st.framesoc.kptrace".toLowerCase())
				|| trace.getType().getName().toLowerCase().startsWith("KPTrace".toLowerCase())) {
			iterator = new KPTraceTedIterator(traceDB);
			adapter = new KPTraceTedAdapter(traceDB, iterator);
		} else {
			// Defaut adapter is KPTrace
			iterator = new KPTraceTedIterator(traceDB);
			adapter = new KPTraceTedAdapter(traceDB, iterator);
		}
		return adapter;
	}
	
	private void adapterClear(TedAdapter adapter) {
		try {
			if (adapter != null)
				adapter.clear();
		} catch (SoCTraceException e) {
			logger.error("Error clearing the adapter");
			e.printStackTrace();
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
//		System.out.println("Initializing TedMainView components !");
		parent.setLayout(new GridLayout(1, false));
		
		//Drawing the header part of the view
		initializeHeaderUIComponents(parent);
		
		//Drawing the body part
		SashForm sashBodyContainer = new SashForm(parent, SWT.NONE);
		sashBodyContainer.setLayout(new GridLayout(2, false));
		sashBodyContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		//Drawing the left part of the body
		initializeLeftPartView(sashBodyContainer);
		
		//Drawing the right part of the body
		initializeRightPartView(sashBodyContainer);

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	private void initializeTraceCombo(Combo combo){
		
		if(combo == null)
			return;
		
		combo.removeAll();
		
		Iterator< Entry<Integer, Trace> > itMapTrace = mapTraces.entrySet().iterator();
		
		while(itMapTrace.hasNext()){
			Entry<Integer, Trace> entry = itMapTrace.next();
			combo.add(entry.getValue().getAlias(), entry.getKey());
		}
	}
	
	private void initializeDataModelCombo(){
		cmbDataModel.removeAll();
		cmbDataModel.add("Punctual event", EventCategory.PUNCTUAL_EVENT);
		cmbDataModel.add("State", EventCategory.STATE);
		cmbDataModel.add("Link", EventCategory.LINK);
		
	}
	
	private DataNode[] adapt(DataNode tree){
		DataNode[] trees = new DataNode[1];
		trees[0] = tree;
		return trees;
	}
	
	private void updateTreeViewResults(){
//		System.out.println(treeNode);
		treeviewResults.setInput(adapt(treeNode));
		treeviewResults.refresh();
//		System.out.println(treeNode);
		
	}
	
	@Override
	public void dispose(){
		// Unregister subscribed topics
		if(topics != null){
			topics.unregisterAll();
		}
		
		super.dispose();
	}
	
	
	/**
	 * Will only receive message of the topic we subscribed to (i.e.
	 * TOPIC_UI_TRACES_SYNCHRONIZED). If we subscribed to more than one topics,
	 * it would have been necessary to check the received type of the message.
	 */
	@Override
	public void handle(FramesocBusTopic topic, Object data){
		// Update trace list 
		refreshTraceList();
	}
	
	/**
	 * Refresh trace list
	 */
	void refreshTraceList(){
		try{
			// Reload trace List
			loadTraces();
		}
		catch(SoCTraceException e){
			e.printStackTrace();
		}
		
		// Update the displayed trace list
		mapTraces.clear();
		
		//Get traces from Framesoc database
		int key = 0;
		for(Trace trace : traces){
			mapTraces.put(key, trace);
			key++;	
		}
		
		initializeTraceCombo(cmbRefTrace);
		initializeTraceCombo(cmbDiagTrace);
		
	}
	
	/**
	 * Load traces present in the database
	 * 
	 * @throws SoCTraceException
	 */
	public void loadTraces() throws SoCTraceException {
		final SystemDBObject sysDB = FramesocManager.getInstance().getSystemDB();
		final TraceQuery traceQuery = new TraceQuery(sysDB);
		traces = traceQuery.getList();
		sysDB.close();
		
		// Sort alphabetically
		Collections.sort(traces, new Comparator<Trace>(){
			@Override
			public int compare(final Trace arg0, final Trace arg1){
				return arg0.getAlias().compareTo(arg1.getAlias());
			}
		});
	}
}
