package fr.ujf.soctrace.tools.analyzer.ted.ui;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;

public class TedTool extends FramesocTool {

	public TedTool() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void launch(IFramesocToolInput input) {
		System.out.println("Ted tool launched !");
		//Open the view
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try{
			TedMainView tedMainView = (TedMainView) window.getActivePage().showView(TedMainView.ID);
			System.out.println("tedMainView Initialized !");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public ParameterCheckStatus canLaunch(IFramesocToolInput input){
		return new ParameterCheckStatus(true,"");
	}

}
