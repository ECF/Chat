package org.eclipse.ecf.example.chat.ui.parts;

import java.util.Calendar;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class MessageComposite extends Composite {
	private Color fGray;
	private Color fGreen;	
	private Color fRed;
	
	private static class ContentProvider implements ITreeContentProvider {
		private Object newInput;

		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.newInput = newInput;

		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return newInput;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}
	}

	private class ViewerLabelProvider extends LabelProvider implements IColorProvider, ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ChatElement chatElement = (ChatElement) element;

			switch (columnIndex) {
			case 0:
				return chatElement.getDateString();
			case 1:
				return chatElement.getHandle();
			case 2:
				return chatElement.getMessage();
			default:
				return "??";
			}

		}

		@Override
		public Color getForeground(Object element) {
			ChatElement chatElement = (ChatElement) element;
			if (chatElement.isLocal()) {
				return fGray;
			} else if (chatElement.hasJoined()) {
				return fGreen;
			} else if (chatElement.hasLeft()) {
				return fRed;
			}
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	protected static final int DATE_WIDTH = 80;
	protected static final int HANDLE_WIDTH = 160;
	private GridTreeViewer fViewer;
	private Object fParent;
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(400, 400);
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		final MessageComposite com = new MessageComposite(shell, SWT.NONE);
		Button button = new Button(shell, SWT.PUSH);
		button.setText("Add Item");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				com.addItem(new ChatElement("text text text text " + e.time, "handle", Calendar.getInstance().getTime(),
						true));
			}
		});

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public MessageComposite(Composite parent, int style) {
		super(parent, style);

		fGray = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
		fGreen = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		fRed = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		Composite composite = formToolkit.createComposite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		formToolkit.paintBordersFor(composite);
				GridLayout gl_composite = new GridLayout(1, false);
				gl_composite.marginWidth = 0;
				gl_composite.marginHeight = 0;
				composite.setLayout(gl_composite);
		
				fViewer = new GridTreeViewer(composite, SWT.BORDER | SWT.V_SCROLL);
				final Grid grid = fViewer.getGrid();
				grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				
						GridColumn Date = new GridColumn(grid, SWT.NONE);
						Date.setWidth(DATE_WIDTH);
						
								GridColumn handle = new GridColumn(grid, SWT.NONE);
								handle.setWidth(HANDLE_WIDTH);
								handle.setWordWrap(true);
								
										final GridColumn messageColumn = new GridColumn(grid, SWT.NONE);
										messageColumn.setWordWrap(true);
										messageColumn.setWidth(150);
		fViewer.setContentProvider(new ContentProvider());
		fViewer.setLabelProvider(new ViewerLabelProvider());
		messageColumn.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				calculateHeight();
			}
		});

		grid.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int width = grid.getSize().x - DATE_WIDTH - HANDLE_WIDTH;
				messageColumn.setWidth(width);
				calculateHeight();
			}
		});
		fViewer.setInput(fParent);

		fParent = new Object();

	}

	public void addItem(ChatElement element) {
		fViewer.add(fParent, element);
		calculateHeight();
		fViewer.reveal(element);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	protected void calculateHeight() {
		for (GridItem item : fViewer.getGrid().getItems()) {
			GC gc = new GC(item.getDisplay());
			GridColumn gridColumn = fViewer.getGrid().getColumn(2);
			Point textBounds = gridColumn.getCellRenderer().computeSize(gc, gridColumn.getWidth(), SWT.DEFAULT, item);
			gc.dispose();
			item.setHeight(textBounds.y);
		}
	}
}
