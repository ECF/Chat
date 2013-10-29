package org.eclipse.ecf.example.chat.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class NewChat {

	private static int CNT = 2;

	@Execute
	public void execute(MApplication app, EModelService ems, EPartService eps) {
		MPart newPart = ems.createModelElement(MPart.class);
		newPart.setElementId(getClass().getName() + CNT);
		newPart.setLabel("OSGi chat #" + CNT++);
		newPart.setContributionURI("bundleclass://org.eclipse.ecf.example.chat.ui/org.eclipse.ecf.example.chat.ui.parts.ChatPart");
		newPart.setCloseable(true);
		
		// Create a new sash container
		MPartSashContainer sash = ems.createModelElement(MPartSashContainer.class);
		sash.setElementId(getClass().getName()+ ".sash" + CNT);
		
		// Attach sash into parent element
		MPerspective perspective = (MPerspective) ems.find("org.eclipse.ecf.example.chat.ui.perspective", app);
		perspective.getChildren().add(sash);
		
		// New Stacks to see part's name
		MPartStack stack = ems.createModelElement(MPartStack.class);
		
		// Add newly created stack into the new sash container
		sash.getChildren().add(stack);
		
		// add newly created part into newly created stack
		stack.getChildren().add(newPart);
		
		// Find the current active part and move it into the newly created sash container
		MPart activePart = eps.getActivePart();
		MElementContainer<MUIElement> oldStack = activePart.getParent();
		oldStack.getParent().getChildren().remove(oldStack);
		sash.getChildren().add((MPartSashContainerElement) oldStack);
		
		eps.activate(newPart);
		eps.bringToTop(newPart);
	}
}