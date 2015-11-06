////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.common.xml.control.AbstractControl;
import com.exactprosystems.jf.common.xml.gui.GuiDictionary;
import com.exactprosystems.jf.common.xml.gui.Section;
import com.exactprosystems.jf.common.xml.gui.Window;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFxController.Result;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatchThrow;

@XmlRootElement(name = "dictionary")
public class DictionaryFx extends GuiDictionary
{
	public static final String startParameters = "StartParameters";
	public static final String connectParameters = "ConnectParameters";

	private static AbstractControl copyControl;
	private static Window copyWindow;

	private String currentAdapter;
	private DictionaryFxController controller;
	private AppConnection appConnection;
	private Context context;
	private Task<Void> task;

	public DictionaryFx(String fileName, Configuration config) throws Exception
	{
		this(fileName, config, null);
	}

	public DictionaryFx(String fileName, Configuration config, String currentAdapter) throws Exception
	{
		super(fileName);
		this.context = new Context(null, null, null, config);
		this.currentAdapter = currentAdapter;
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();
		
		IWindow window = getFirstWindow();
		displayTitle(getName());
		displayDialog(window, getWindows());
		dislpaySection(SectionKind.Run);
		if (window != null)
		{
			IControl control = window.getFirstControl(SectionKind.Run);
			displayElement(window, SectionKind.Run, control);
		}
		displayApplicationStatus(this.appConnection != null ? ApplicationStatus.Connected : ApplicationStatus.Disconnected, null, null);
		displayApplicationControl(null);
	}

	@Override
	public void create() throws Exception
	{
		super.create();
		initController();
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		initController();
	}
	
	@Override
	public void save(String fileName) throws Exception
	{
		if (!hasName())
		{
			File file = DialogsHelper.showSaveAsDialog(this);
			if (file == null)
			{
				return;
			}
			
			fileName = file.getPath();
		}
		
		super.save(fileName);
		this.controller.saved(getName());
		displayTitle(getName());
	}

	@Override
	public boolean canClose() throws Exception
	{
		if (!super.canClose())
		{
			return false;
		}
		
		if (isChanged())
		{
			ButtonType desision = DialogsHelper.showSaveFileDialog(this.getName());
			if (desision == ButtonType.YES)
			{
				save(getName());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void close() throws Exception
	{
		super.close();

		this.context.getConfiguration().unregister(this);
		this.context.close();
		this.controller.close();

		stopApplication();
	}

    //------------------------------------------------------------------------------------------------------------------
	public void startGrabbing() throws Exception
	{
		if (this.appConnection != null && this.appConnection.isGood())
		{
			this.appConnection.getApplication().service().startGrabbing();
		}
	}

	public void endGrabbing() throws Exception
	{
		if (this.appConnection != null && this.appConnection.isGood())
		{
			this.appConnection.getApplication().service().endGrabbing();
		}
	}

    //------------------------------------------------------------------------------------------------------------------
	public void windowChanged(IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (window == null)
		{
			displayElement(window, sectionKind, null);
		}
		else
		{
			IControl control = window.getFirstControl(sectionKind);
			displayElement(window, sectionKind, control);
		}
	}

	public void sectionChanged(IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (window == null)
		{
			displayElement(window, sectionKind, null);
		}
		else
		{
			IControl control = window.getFirstControl(sectionKind);
			displayElement(window, sectionKind, control);
		}
	}


	public void elementChanged(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
		displayElement(window, sectionKind, control);
	}


	//------------------------------------------------------------------------------------------------------------------
	public void dialogNew(IWindow.SectionKind sectionKind) throws Exception
	{
		String nameNewWindow = "NewWindow";

		Window window = new Window(nameNewWindow);
		window.correctAll();

		Command undo = () -> Common.tryCatch(() -> {
			removeWindow(window);
			Collection<IWindow> windows = getWindows();
			IWindow oldWindow = (IWindow) windows.toArray()[windows.size() - 1];
			displayDialog(oldWindow, windows);
			displayElement(oldWindow, sectionKind, oldWindow.getFirstControl(sectionKind));
		}, "");

		Command redo = () -> Common.tryCatch(() -> {
			addWindow(window);
			displayDialog(window, getWindows());
			displayElement(window, sectionKind, null);
		}, "");

		addCommand(undo, redo);
		super.changed(true);
	}
	
	public void dialogDelete(IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (window != null)
		{
			int indexOf = indexOf(window);

			Command undo = () -> Common.tryCatch(() -> {
				addWindow(indexOf, (Window) window);
				displayDialog(window, getWindows());
				displayElement(window, sectionKind, window.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() -> {
				removeWindow(window);
				IWindow anotherWindow = getFirstWindow();
				if (anotherWindow != null)
				{
					displayDialog(anotherWindow, getWindows());
					displayElement(anotherWindow, sectionKind, anotherWindow.getFirstControl(sectionKind));
				}
			}, "");

			addCommand(undo, redo);
			super.changed(true);
		}
	}
	
	public void dialogCopy(IWindow window) throws Exception
	{
		copyWindow = Window.createCopy((Window)window);
	}
	
	public void dialogPaste(IWindow.SectionKind sectionKind) throws Exception
	{
		if (copyWindow != null)
		{
			Window clone = Window.createCopy(copyWindow);
			int indexOf = indexOf(clone);
			Command undo = () -> Common.tryCatch(() -> {
				removeWindow(clone);
				Collection<IWindow> windows = getWindows();
				IWindow oldWindow = (IWindow) windows.toArray()[Math.min(indexOf, windows.size() - 1)];
				displayDialog(oldWindow, windows);
				displayElement(oldWindow, sectionKind, oldWindow.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() -> {
				addWindow(clone);
				displayDialog(clone, getWindows());
				displayElement(clone, sectionKind, clone.getFirstControl(sectionKind));
			}, "");
			addCommand(undo, redo);
			super.changed(true);
		}
	}
	
	public void dialogRename(IWindow window, String name) throws Exception
	{
		if (window != null)
		{
			String oldName = window.getName();
			Command undo = () -> {
				window.setName(oldName);
				displayDialog(window, getWindows());
			};

			Command redo = () -> {
				window.setName(name);
				displayDialog(window, getWindows());
			};

			addCommand(undo, redo);
			super.changed(true);
		}
	}

	public void dialogTest(IWindow window) throws Exception
	{
		if (this.appConnection != null)
		{
			Set<ControlKind> supported = Arrays.stream(appConnection.getApplication().getFactory().supportedControlKinds())
					.collect(Collectors.toSet());
			
			Collection<IControl> controls = window.getControls(SectionKind.Run);
			this.controller.displayTestingControls(controls);
			new Thread(new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					
					controls.forEach(control -> 
					{
						try
						{
							if (!supported.contains(control.getBindedClass()))
							{
								controller.displayTestingControl(control, "Not allowed", Result.NOT_ALLOWED);
							}
							else
							{
								IControl owner = window.getOwnerControl(control);
								Locator ownerLocator = owner == null ? null : owner.locator();
								Collection<String> all = appConnection.getApplication().service().findAll(ownerLocator, control.locator());
								
								Result result = null;
								if (all.size() == 1 || (Addition.Many.equals(control.getAddition()) && all.size() > 0))
								{
									result = Result.PASSED;
								}
								else
								{
									result = Result.FAILED;
								}
								
								controller.displayTestingControl(control, String.valueOf(all.size()), result);
							}
						}
						catch (Exception e)
						{
							controller.displayTestingControl(control, "Error", Result.FAILED);
						}
					});
					return null;
				}
			}).start();
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	public void elementNew(IWindow window, IWindow.SectionKind sectionKind, boolean useSelfAsOwner) throws Exception
	{
		if (window != null)
		{
			AbstractControl control = AbstractControl.create(ControlKind.Any);
			if (useSelfAsOwner)
			{
				IControl firstControl = window.getFirstControl(SectionKind.Self);
				Optional.ofNullable(firstControl).ifPresent(owner -> Common.tryCatch(() -> control.set(AbstractControl.ownerIdName, owner.getID()), "Error on set owner owner"));
			}
			Command undo = () -> Common.tryCatch(() -> {
				window.removeControl(control);
				displayElement(window, sectionKind, window.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() -> {
				window.addControl(sectionKind, control);
				displayElement(window, sectionKind, control);
			}, "");
			addCommand(undo, redo);
			super.changed(true);
		}
		
	}

	public void elementDelete(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
		if (window != null && control != null)
		{
			boolean ref = window.hasReferences(control);

			Command undo = () -> Common.tryCatch(() -> {
				window.addControl(sectionKind, control);
				displayElement(window, sectionKind, control);
			}, "");

			Command redo = () ->
			{
				Common.tryCatch(() -> {
					if (!ref)
					{
						window.removeControl(control);
						IControl anotherControl = window.getFirstControl(sectionKind);
						displayElement(window, sectionKind, anotherControl);
					}
					else
					{
						ButtonType desision = DialogsHelper.showQuestionDialog("This element is the owner for other elements", "Remove it anyway?");

						if (desision == ButtonType.OK)
						{
							window.removeControl(control);
							IControl anotherControl = window.getFirstControl(sectionKind);
							displayElement(window, sectionKind, anotherControl);
						}
					}
				}, "");
			};
			addCommand(undo, redo);
			super.changed(true);
		}
	}
	
	public void elementCopy(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
		copyControl = AbstractControl.createCopy(control);
	}
	
	public void elementPaste(IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (copyControl != null && window != null)
		{
			AbstractControl copy = AbstractControl.createCopy(copyControl);

			Command undo = () -> Common.tryCatch(() -> {
				window.removeControl(copy);
				displayElement(window, sectionKind, window.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() -> {
				ISection section = window.getSection(sectionKind);
				if (section != null)
				{
					section.addControl(copy);
				}
				displayElement(window, sectionKind, copy);
			}, "");

			addCommand(undo, redo);
			super.changed(true);
		}
	}
	
	public void elementRecord(double x, double y, boolean useSelfAsOwner, IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (window == null)
		{
			throw new Exception("You need select a dialog at first.");
		}
		if (this.appConnection != null)
		{
			IRemoteApplication service = this.appConnection.getApplication().service();
			if (service != null)
			{
				Locator locator = service.getLocator(null, ControlKind.Any, (int) x, (int) y);
				if (locator != null)
				{
					ControlKind controlKind = locator.getControlKind();
					AbstractControl newControl = AbstractControl.create(controlKind);
					
					String ownerId = useSelfAsOwner ? getOwnerId(window) : null;
					
					newControl.set(AbstractControl.idName, locator.getId());
					newControl.set(AbstractControl.uidName, locator.getUid());
					newControl.set(AbstractControl.xpathName, locator.getXpath());
					newControl.set(AbstractControl.ownerIdName, ownerId);
					newControl.set(AbstractControl.clazzName, locator.getClazz());
					newControl.set(AbstractControl.nameName, locator.getName());
					newControl.set(AbstractControl.titleName, locator.getTitle());
					newControl.set(AbstractControl.actionName, locator.getAction());
					newControl.set(AbstractControl.additionName, null);
					newControl.set(AbstractControl.visibilityName, null);
					newControl.set(AbstractControl.weakName, locator.isWeak());
					newControl.set(AbstractControl.textName, locator.getText());
					newControl.set(AbstractControl.timeoutName, 0);
					newControl.set(AbstractControl.tooltipName, locator.getTooltip());
					newControl.set(AbstractControl.expressionName, null);
					newControl.set(AbstractControl.rowsName, null);
					newControl.set(AbstractControl.useNumericHeaderName, false);
					newControl.correctAllXml();

					AbstractControl copy = AbstractControl.createCopy(newControl);
					Command undo = () -> Common.tryCatch(() -> {
						window.removeControl(copy);
						displayElement(window, sectionKind, window.getFirstControl(sectionKind));
					}, "");
					Command redo = () -> Common.tryCatch(() -> {
						window.addControl(sectionKind, copy);
						displayElement(window, sectionKind, copy);
					}, "");
					addCommand(undo, redo);
					super.changed(true);
				}
				this.controller.println("record : " + locator + " locator " + (locator == null ? "not " : "") + "found");
			}
		}
	}

	public void elementRenew(double x, double y, boolean useSelfAsOwner, IWindow window, IWindow.SectionKind sectionKind, IControl control)  throws Exception
	{
		if (window == null)
		{
			throw new Exception("You need select a dialog at first.");
		}
		if (this.appConnection != null)
		{
			IRemoteApplication service = this.appConnection.getApplication().service();
			if (service != null)
			{
				Locator owner = getLocator(window.getOwnerControl(control));
				Locator old = getLocator(AbstractControl.createCopy(control));
				
				Locator locator = service.getLocator(owner, control.getBindedClass(), (int) x, (int) y);

				Command undo = () -> Common.tryCatch(() -> {
					((AbstractControl) control).renew(old);
					displayElement(window, sectionKind, control);
				},"");
				Command redo = () -> Common.tryCatch(() -> {
					if (locator != null)
					{
						((AbstractControl) control).renew(locator);
						displayElement(window, sectionKind, control);
					}
				}, "");
				addCommand(undo, redo);
				this.controller.println("renew : " + locator + " locator " + (locator == null ? "not " : "") + "found");
				super.changed(true);
			}
		}
	}

	
	//------------------------------------------------------------------------------------------------------------------
	public void parameterSetId(IWindow window, IWindow.SectionKind sectionKind, IControl control, Object value) throws Exception
	{
		tryCatchThrow(() ->
		{
			if (control != null && control instanceof AbstractControl)
			{
				AbstractControl copy = AbstractControl.createCopy(control);
				String oldName = copy.getID();
				Command undo = () -> Common.tryCatch(() -> {
					((AbstractControl) control).set(AbstractControl.idName, oldName);
					displayElement(window, sectionKind, control);
				}, "");
				Command redo = () -> Common.tryCatch(() -> {
					((AbstractControl) control).set(AbstractControl.idName, value);
					displayElement(window, sectionKind, control);
				}, "");
				addCommand(undo, redo);
				super.changed(true);
			}
		}, "Cannot set field '" + AbstractControl.idName + "' to value '" + value + "'");
	}
	
	public void parameterSetOwner(IWindow window, SectionKind sectionKind, IControl control, String ownerId) throws Exception
	{
		tryCatchThrow(() ->
		{
			if (control != null && control instanceof AbstractControl)
			{
				AbstractControl copy = AbstractControl.createCopy(control);
				String oldOwnerId = copy.getOwnerID();
				Command undo = () -> Common.tryCatch(() -> {
					((AbstractControl)control).set(AbstractControl.ownerIdName, oldOwnerId);
					displayElement(window, sectionKind, control);
				}, "");
				Command redo = () -> Common.tryCatch(() -> {
					((AbstractControl)control).set(AbstractControl.ownerIdName, ownerId);
					displayElement(window, sectionKind, control);
				}, "");
				addCommand(undo, redo);

				super.changed(true);
			}
		}, "Cannot set field '" + AbstractControl.ownerIdName + "' to value '" + ownerId + "'");
	}

	public void parameterSet(IWindow window, IWindow.SectionKind sectionKind, IControl control, String parameter, Object value) throws Exception
	{
		tryCatchThrow(() ->
		{
			if (control != null && control instanceof AbstractControl)
			{
				AbstractControl copy = AbstractControl.createCopy(control);
				Object oldValue = copy.get(parameter);
				Command undo = () -> Common.tryCatch(() -> {
					((AbstractControl) control).set(parameter, oldValue);
					displayElement(window, sectionKind, control);
				}, "");
				Command redo = () -> Common.tryCatch(() -> {
					((AbstractControl) control).set(parameter, value);
					displayElement(window, sectionKind, control);
				}, "");
				addCommand(undo, redo);

//				((AbstractControl)control).set(parameter, value);
				super.changed(true);
			}
		}, "Cannot set field '" + parameter + "' to value '" + value + "'");
	}
	
	public void parameterSetControlKind(IWindow window, IWindow.SectionKind sectionKind, IControl control, ControlKind kind) throws Exception
	{
		if (control != null && window != null && sectionKind != null)
		{
			AbstractControl oldControl = AbstractControl.createCopy(control);
			AbstractControl newControl = AbstractControl.createCopy(control, kind);
			Command undo = () -> Common.tryCatch(() -> {
				if (oldControl.getBindedClass() != newControl.getBindedClass())
				{
					Section section = (Section) window.getSection(sectionKind);
					section.replaceControl(newControl, control);
					displayElement(window, sectionKind, control);
				}
			}, "");
			Command redo = () -> Common.tryCatch(() -> {
				if (control.getBindedClass() != kind)
				{
					Section section = (Section) window.getSection(sectionKind);
					section.replaceControl(control, newControl);
					displayElement(window, sectionKind, newControl);
				}
			}, "");
			addCommand(undo, redo);
			super.changed(true);
		}
	}
	
	public void parameterSetXpath(IWindow window, IWindow.SectionKind sectionKind, IControl control, String xpath) throws Exception
	{
		tryCatchThrow(() ->
		{
			if (control != null && control instanceof AbstractControl)
			{
				AbstractControl copy = AbstractControl.createCopy(control);
				String oldValue = copy.getXpath();
				Command undo = () -> Common.tryCatch(() -> 
				{
					((AbstractControl) control).set(AbstractControl.xpathName, oldValue);
					displayElement(window, sectionKind, control);
				}, "");
				Command redo = () -> Common.tryCatch(() -> 
				{
					((AbstractControl) control).set(AbstractControl.xpathName, xpath);
					displayElement(window, sectionKind, control);
				}, "");
				addCommand(undo, redo);

				super.changed(true);
			}
		}, "Cannot set field 'xpath' to value '" + xpath + "'");
	}
	
	public void parameterGoToOwner(IWindow window, IControl owner) throws Exception
	{
		if (owner != null)
		{
			SectionKind sectionKind = owner.getSection().getSectionKind();
			dislpaySection(sectionKind);
			displayElement(window, sectionKind, owner);
		}
	}

	//------------------------------------------------------------------------------------------------------------------

	public void startApplication(String idAppEntry) throws Exception
	{
		if (this.appConnection == null)
		{
			startConnectApplication(idAppEntry, true);
		}
	}

	public void connectToApplication(String idAppEntry) throws Exception
	{
		if (this.appConnection == null)
		{
			startConnectApplication(idAppEntry, false);
		}
	}

	public void stopApplication() throws Exception
	{
		if (this.task != null && this.task.isRunning() && !this.task.isDone())
		{
			this.task.cancel();
			this.task = null;
		}
		if (this.appConnection != null)
		{
			this.appConnection.close();
			this.appConnection = null;
		}
		
//		displayApplicationControl("");
		displayApplicationStatus(ApplicationStatus.Disconnected, null, this.appConnection);
	}

	public void sendKeys(String text, IControl control, IWindow window) throws Exception
	{
		if (this.appConnection != null)
		{
			Locator owner = getLocator(getOwner(control, window));
			Locator locator = getLocator(control);
			Locator addition = getLocator(getRows(control, window));
			IRemoteApplication service = appConnection.getApplication().service();

			service.operate(owner, locator, addition, null, Do.text(text));
			
//			displayApplicationControl(null);
		}
	}

	public void click(IControl control, IWindow window) throws Exception
	{
		if (this.appConnection != null)
		{
			Locator owner = getLocator(getOwner(control, window));
			Locator locator = getLocator(control);
			Locator rows = getLocator(getRows(control, window));
			Locator header = getLocator(getHeader(control, window));
			
			IRemoteApplication service = this.appConnection.getApplication().service();
			service.operate(owner, locator, rows, header, Do.click());
			
//			displayApplicationControl(null);
		}
	}

	public void find(IControl control, IWindow window) throws Exception
	{
		displayImage(null);
		if (this.appConnection != null)
		{
			Locator owner = getLocator(getOwner(control, window));
			Locator locator = getLocator(control);
			IRemoteApplication service = this.appConnection.getApplication().service();
			Collection<String> all = service.findAll(owner, locator);
			for (String str : all)
			{
				this.controller.println(str);
			}
			ImageWrapper imageWrapper = service.getImage(owner, locator);
			displayImage(imageWrapper);
			
//			displayApplicationControl(null);
		}
	}

	public void operate(Operation operation, IControl control, IWindow window) throws Exception
	{
		if (this.appConnection != null)
		{
			Locator owner = getLocator(getOwner(control, window));
			Locator locator = getLocator(control);
			Locator rows = getLocator(getRows(control, window));
			Locator header = getLocator(getHeader(control, window));
			//TODO use AbstracControl.operate()
			IRemoteApplication application = appConnection.getApplication().service();
			operation.tune(window);
			OperationResult operate = application.operate(owner, locator, rows, header, operation);
			if (operate.isColorMapIsFilled())
			{
				this.controller.println(operate.getColorMap().entrySet().toString());
			}
			else if (operate.isMapFilled())
			{
				this.controller.println(operate.getMap().entrySet().toString());
			}
			else if (operate.isArrayFilled())
			{
				this.controller.println(Arrays.deepToString(operate.getArray()));
			}
			else
			{
				String val = operate.getText();
				if (!Str.IsNullOrEmpty(val))
				{
					this.controller.println("" + val);
				}
			}

//			displayApplicationControl(null);
		}
	}

	public void switchTo(String selectedItem) throws Exception
	{
		if (this.appConnection != null)
		{
			String title = this.appConnection.getApplication().service().switchTo(selectedItem);
			displayApplicationControl(title);
		}
	}
	
	public void refresh() throws Exception
	{
		if (this.appConnection != null)
		{
			this.appConnection.getApplication().service().refresh();
			displayApplicationControl(null);
		}
	}

	
	//------------------------------------------------------------------------------------------------------------------
	// private methods
	//------------------------------------------------------------------------------------------------------------------
	private void initController() throws Exception
	{
		this.controller = Common.loadController(DictionaryFx.class.getResource("DictionaryTab.fxml"));
		this.controller.init(this, this.context);
		this.context.getConfiguration().register(this);
	}

	private String getOwnerId(IWindow window)
	{
		ISection section = window.getSection(IWindow.SectionKind.Self);
		if (section == null || section.getControls().isEmpty())
		{
			return null;
		}
		return section.getControls().iterator().next().getID();
	}

	private Locator getLocator(IControl control)
	{
		if (control != null)
		{
			return control.locator();
		}
		return null;
	}

	private IControl getOwner(IControl control, IWindow window) throws Exception
	{
		if (window != null && control != null && control.getOwnerID() != null)
		{
			return window.getControlForName(null, control.getOwnerID());
		}
		return null;
	}

	private IControl getRows(IControl control, IWindow window) throws Exception
	{
		if (window != null && control != null && control.getRowsId() != null)
		{
			return window.getControlForName(null, control.getRowsId());
		}
		return null;
	}

	private IControl getHeader(IControl control, IWindow window) throws Exception
	{
		if (window != null && control != null && control.getRowsId() != null)
		{
			return window.getControlForName(null, control.getHeaderId());
		}
		return null;
	}

	private void startConnectApplication(final String idAppEntry, final boolean isStart) throws Exception
	{
		if (idAppEntry == null)
		{
			throw new Exception("You should choose app entry at first.");
		}
		ApplicationPool applicationPool = context.getApplications();
		
		String parametersName 	= isStart ? startParameters : connectParameters;
		String title 			= isStart ? "Start " : "Connect ";
		String[] strings 		= isStart ? applicationPool.wellKnownStartArgs(idAppEntry) : applicationPool.wellKnownConnectArgs(idAppEntry);

		Settings settings = this.context.getConfiguration().getSettings();
		final Map<String, String> parameters = settings.getMapValues(Settings.APPLICATION + idAppEntry, parametersName, strings);

		ButtonType desision = DialogsHelper.showParametersDialog(title + idAppEntry, parameters, context.getEvaluator());
		
		if (desision == ButtonType.CANCEL)
		{
			return;
		}
		
		settings.setMapValues(Settings.APPLICATION + idAppEntry, parametersName, parameters);
		settings.saveIfNeeded();

		// evaluate parameters 
		AbstractEvaluator evaluator = this.context.getEvaluator();
		Iterator<Entry<String, String>> iterator = parameters.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, String> entry = iterator.next();
			String name = entry.getKey();
			String expression = entry.getValue();
			try
			{
				Object value = evaluator.evaluate(expression);
				entry.setValue(String.valueOf(value));
			}
			catch (Exception e)
			{
				throw new Exception ("Error in " + name + " = " + expression + " :" + e.getMessage(), e);
			}
		}

		this.task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				ApplicationPool applicationPool = context.getApplications();
				
				displayApplicationStatus(ApplicationStatus.Connecting, null, null);
				if (isStart)
				{
					appConnection = applicationPool.startApplication(idAppEntry, parameters);
				}
				else
				{
					appConnection = applicationPool.connectToApplication(idAppEntry, parameters);
				}
				return null;
			}
		};

		this.task.setOnSucceeded(workerStateEvent -> displayApplicationStatus(ApplicationStatus.Connected, null, this.appConnection));
		this.task.setOnFailed(workerStateEvent -> displayApplicationStatus(ApplicationStatus.Disconnected, task.getException(), null));
		Thread thread = new Thread(task);
		thread.setName("Start app " + thread.getId());
		thread.setDaemon(true);
		thread.start();
	}

	private void displayTitle(String name)
	{
		this.controller.displayTitle(getName());
	}

	private void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		this.controller.displayDialog(window, windows);
	}

	private void dislpaySection(IWindow.SectionKind sectionKind) throws Exception
	{
		this.controller.displaySection(sectionKind);
	}

	private void displayElement(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
		IControl owner = getOwner(control, window);
		
    	Collection<IControl> controls = null;
    	Collection<IControl> owners = null;
    	Collection<IControl> rows = null;
    	IControl row = null;
    	IControl header = null;
    	if (window != null)
    	{
			controls = window.getControls(sectionKind);
    		owners = controlsWithId(window, null);
    		rows = controlsWithId(window, null);
    		row = window.getRowsControl(control);
    		header = window.getHeaderControl(control);
    	}
		this.controller.displaySection(sectionKind);
		this.controller.displayElement(controls, control, owners, owner, rows, row, header);
	}

	private Collection<IControl> controlsWithId(IWindow window, IWindow.SectionKind sectionKind)
	{
		return window.getControls(sectionKind)
				.stream()
				.filter(c -> c.getID() != null && !c.getID().isEmpty())
				.collect(Collectors.toList());
	}

	private void displayImage(ImageWrapper imageWrapper) throws Exception
	{
		this.controller.displayImage(imageWrapper);
	}

	private void displayApplicationStatus(ApplicationStatus status, Throwable throwable, AppConnection appConnection)
	{
		this.controller.displayApplicationStatus(status, throwable, appConnection);
	}

	private void displayApplicationControl(String title) throws Exception
	{
		Collection<String> entries = this.context.getConfiguration().getApplications();
		Collection<String> titles = null;
		
		if (this.appConnection != null)
		{
			titles = this.appConnection.getApplication().service().titles();		
		}
		
		this.controller.displayActionControl(entries, this.currentAdapter, titles, title);
	}

	private static final Logger logger = Logger.getLogger(DictionaryFx.class);
}
