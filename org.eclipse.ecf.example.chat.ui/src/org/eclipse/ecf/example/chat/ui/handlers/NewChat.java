package org.eclipse.ecf.example.chat.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class NewChat {

	private static final String PART_STACK_ID = "org.eclipse.ecf.example.chat.ui.partstack.0";

	@Execute
	public void execute(MApplication app, EModelService ems, EPartService eps) {
		MPart newPart = ems.createModelElement(MPart.class);
		newPart.setLabel("OSGi chat #2");
		newPart.setContributionURI("bundleclass://org.eclipse.ecf.example.chat.ui/org.eclipse.ecf.example.chat.ui.parts.ChatPart");
		
		MPartStack stack = (MPartStack) ems.find(PART_STACK_ID, app);
		stack.getChildren().add(newPart);
		
		eps.activate(newPart);
		eps.bringToTop(newPart);
	}
}