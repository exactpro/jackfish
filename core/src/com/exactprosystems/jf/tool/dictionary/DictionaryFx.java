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
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFxController.Result;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;

import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatchThrow;

@XmlRootElement(name = "dictionary")
public class DictionaryFx extends GuiDictionary
{
	private static final String DIALOG_DICTIONARY_SETTINGS = "DictionarySettings";
	private static AbstractControl copyControl;
	private static Window copyWindow;
	private boolean isControllerInit = false;
	private LinkedHashMap<String, Object> storeMap;
	private String currentAdapterStore;
	private String currentAdapter;
	private DictionaryFxController controller;
	private AbstractEvaluator evaluator;
	private ApplicationConnector applicationConnector;

	public DictionaryFx(String fileName, DocumentFactory factory) throws Exception
	{
		this(fileName, factory, null);
	}

	public DictionaryFx(String fileName, DocumentFactory factory, String currentAdapter) throws Exception
	{
		super(fileName, factory);
		this.currentAdapter = currentAdapter;
		this.evaluator = factory.createEvaluator();
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();

		initController();

		displayTitle(getName());

		IWindow window = getFirstWindow();
		displayDialog(window, getWindows());
		displaySection(SectionKind.Run);
		if (window != null)
		{
			IControl control = window.getFirstControl(SectionKind.Run);
			displayElement(window, SectionKind.Run, control);
		}
		displayApplicationStatus(this.applicationConnector != null && this.applicationConnector.getAppConnection() != null ? ApplicationStatus.Connected : ApplicationStatus.Disconnected, null, null);
		displayApplicationControl(null);
		restoreSettings(getFactory().getSettings());
	}

	@Override
	public void create() throws Exception
	{
		super.create();
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
	}
	
	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		this.controller.saved(getName());
		displayTitle(getName());
		
		if (this.currentAdapter != null)
		{
			IApplicationFactory factory = getFactory().getConfiguration().getApplicationPool().loadApplicationFactory(this.currentAdapter);
			factory.init(this);
		}
		{
			getFactory().getConfiguration().dictionaryChanged(getName(), this);
		}
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
	public void close(Settings settings) throws Exception
	{
		super.close(settings);

		this.controller.close();

		stopApplication();
		storeSettings(settings);
	}

	//------------------------------------------------------------------------------------------------------------------
	public void displayStores() throws Exception
	{
        storeMap = (LinkedHashMap<String, Object>) getFactory().getConfiguration().getStoreMap();
        Collection<String> stories = new ArrayList<>();
        if (!storeMap.isEmpty())
        {
            stories.add("");
            storeMap.forEach((s, o) ->
            {
                if(o instanceof AppConnection)
                {
                    stories.add(s);
                }
            });
        }

		this.controller.displayStoreActionControl(stories, this.currentAdapterStore);
	}

	public void displayTitles() throws Exception
	{
		Collection<String> titles = null;
		if (isApplicationRun())
		{
			titles = this.applicationConnector.getAppConnection().getApplication().service().titles();
		}
		this.controller.displayTitles(titles);
	}

	public void setCurrentAdapter(String adapter)
	{
		this.currentAdapter = adapter;
	}

	public void setCurrentAdapterStore(String currentAdapterStore)
	{
		this.currentAdapterStore = currentAdapterStore;
	}

	public void startGrabbing() throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().startGrabbing();
		}
	}

	public void endGrabbing() throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().endGrabbing();
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
		displayElementInfo(window, sectionKind, control);
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
			if (oldName.equals(name))
			{
				return;
			}
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

	public void dialogTest(IWindow window, List<IControl> controls) throws Exception
	{
		if (isApplicationRun())
		{
			Set<ControlKind> supported = Arrays.stream(this.applicationConnector.getAppConnection().getApplication().getFactory().supportedControlKinds())
					.collect(Collectors.toSet());

			Thread thread = new Thread(new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{

					controls.forEach(control -> {
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
								Collection<String> all = applicationConnector.getAppConnection().getApplication().service().findAll(ownerLocator, control.locator());

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
			});
			thread.setName("Test dialog, thread id : " + thread.getId());
			thread.start();
		}
	}

	public void dialogMove(IWindow window, IWindow.SectionKind section, Integer newIndex) throws Exception
	{
		int lastIndex = this.indexOf(window);
		if (lastIndex == newIndex)
		{
			return;
		}
		Command undo = () -> Common.tryCatch(() -> {
			super.windows.remove(newIndex.intValue());
			this.addWindow(lastIndex, (Window) window);
			this.displayDialog(window, getWindows());
			displayElement(window, section, window.getFirstControl(section));
		}, "");
		Command redo = () -> Common.tryCatch(() -> {
			super.windows.remove(lastIndex);
			this.addWindow(newIndex, (Window) window);
			this.displayDialog(window, getWindows());
			displayElement(window, section, window.getFirstControl(section));
		}, "");
		addCommand(undo, redo);
	}

	//------------------------------------------------------------------------------------------------------------------
	public void elementNew(IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (window != null)
		{
			AbstractControl control = AbstractControl.create(ControlKind.Any);
			IControl firstControl = window.getFirstControl(SectionKind.Self);
			Optional.ofNullable(firstControl).ifPresent(owner -> Common.tryCatch(() -> control.set(AbstractControl.ownerIdName, owner.getID()), "Error on set owner owner"));
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
	
	public void elementRecord(double x, double y, IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (window == null)
		{
			throw new Exception("You need select a dialog at first.");
		}
		if (isApplicationRun())
		{
			IRemoteApplication service = this.applicationConnector.getAppConnection().getApplication().service();
			if (service != null)
			{
				Locator locator = service.getLocator(null, ControlKind.Any, (int) x, (int) y);
				if (locator != null)
				{
					ControlKind controlKind = locator.getControlKind();
					AbstractControl newControl = AbstractControl.create(controlKind);

					IControl selfControl = window.getSelfControl();
					String ownerId = selfControl == null ? null : selfControl.getID();
					
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

	public void elementRenew(double x, double y, IWindow window, IWindow.SectionKind sectionKind, IControl control)  throws Exception
	{
		if (window == null)
		{
			throw new Exception("You need select a dialog at first.");
		}
		if (isApplicationRun())
		{
			IRemoteApplication service = this.applicationConnector.getAppConnection().getApplication().service();
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

	public void checkNewId(IWindow currentWindow, String id) throws Exception
	{
		IControl controlForName = currentWindow.getControlForName(SectionKind.Run,id);
		if (controlForName != null)
		{
			this.controller.showInfo(String.format("Id with name '%s' already exist", id));
		}
	}

	public void elementMove(IWindow window, IWindow.SectionKind section, IControl control, Integer newIndex) throws Exception
	{
		if (window == null || section == null)
		{
			return;
		}
		int lastIndex = new ArrayList<>(window.getControls(section)).indexOf(control);
		if (lastIndex == newIndex)
		{
			return;
		}
		Command undo = () -> Common.tryCatch(() -> {
			window.removeControl(control);
			window.getSection(section).addControl(lastIndex, control);
			this.displayElement(window, section, control);
		}, "");

		Command redo = () -> Common.tryCatch(() -> {
			window.removeControl(control);
			window.getSection(section).addControl(newIndex, control);
			this.displayElement(window, section, control);
		}, "");
		addCommand(undo, redo);
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
				Command undo = () -> Common.tryCatch(() -> ((AbstractControl) control).set(parameter, oldValue), "");
				Command redo = () -> Common.tryCatch(() -> ((AbstractControl) control).set(parameter, value), "");
				addCommand(undo, redo);
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
			displaySection(sectionKind);
			displayElement(window, sectionKind, owner);
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	public void startApplication(String idAppEntry) throws Exception
	{
		this.applicationConnector.setIdAppEntry(idAppEntry);
		this.applicationConnector.startApplication();
	}

	public void connectToApplication(String idAppEntry) throws Exception
	{
		this.applicationConnector.setIdAppEntry(idAppEntry);
		this.applicationConnector.connectApplication();
	}

	public void connectToApplicationFromStore(String idAppStore) throws Exception
	{
		if(idAppStore.isEmpty() || idAppStore == null)
		{
			this.applicationConnector.setIdAppEntry(null);
			this.applicationConnector.setAppConnection(null);
			this.controller.displayApplicationStatus(ApplicationStatus.Disconnected, null, null);
		}
		else
		{
			AppConnection appConnection = (AppConnection) storeMap.get(idAppStore);
			this.applicationConnector.setIdAppEntry(appConnection.getId());
			this.applicationConnector.setAppConnection(appConnection);
			this.controller.displayApplicationStatus(ApplicationStatus.ConnectingFromStore, null, appConnection);
		}
	}

	public void stopApplication() throws Exception
	{
		this.applicationConnector.stopApplication();
	}

	public void sendKeys(String text, IControl control, IWindow window) throws Exception
	{
		this.operate(Do.text(text), window, control);
	}

	public void click(IControl control, IWindow window) throws Exception
	{
		this.operate(Do.click(), window, control);
	}

	public void find(IControl control, IWindow window) throws Exception
	{
		displayImage(null);
		if (isApplicationRun())
		{
			Locator owner = getLocator(window.getOwnerControl(control));
			Locator locator = getLocator(control);
			IRemoteApplication service = this.applicationConnector.getAppConnection().getApplication().service();
			Collection<String> all = service.findAll(owner, locator);
			for (String str : all)
			{
				this.controller.println(str);
			}
			ImageWrapper imageWrapper = service.getImage(owner, locator);
			displayImage(imageWrapper);
		}
	}

	public void doIt(Object obj, IControl control, IWindow window) throws Exception
	{
		if (obj instanceof Operation)
		{
			Operation operation = (Operation)obj;
			
			Optional<OperationResult> result = this.operate(operation, window, control);
			result.ifPresent(operate -> this.controller.println(operate.humanablePresentation()));
		}
		else if (obj instanceof Spec)
		{
			Spec spec = (Spec)obj;
			
			Optional<CheckingLayoutResult> result = this.check(spec, window, control);
			result.ifPresent(check -> 
			{
				if (check.isOk())
				{
					this.controller.println("Check is passed");
				}
				else
				{
					this.controller.println("Check is failed:");
					for (String err : check.getErrors())
					{
						this.controller.println("" + err);
					}
				}
			});
		}
		else
		{
			this.controller.println("" + obj);
		}
	}

	public void switchTo(String selectedItem) throws Exception
	{
		if (isApplicationRun())
		{
			String title = this.applicationConnector.getAppConnection().getApplication().service().switchTo(selectedItem, true);
			displayApplicationControl(title);
		}
	}
	
	public void refresh() throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().refresh();
			displayApplicationControl(null);
		}
	}

	public void switchToCurrent(IControl control) throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().switchToFrame(control.locator());
		}
	}

	public void switchToParent() throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().switchToFrame(null);
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	// private methods
	//------------------------------------------------------------------------------------------------------------------
	private boolean isApplicationRun()
	{
		return this.applicationConnector != null && this.applicationConnector.getAppConnection() != null && this.applicationConnector.getAppConnection().isGood();
	}

	private Optional<OperationResult> operate(Operation operation, IWindow window, IControl control) throws Exception
	{
		if (isApplicationRun())
		{
			IControl owner = window.getOwnerControl(control);
			IControl rows = window.getRowsControl(control);
			IControl header = window.getHeaderControl(control);

			AbstractControl abstractControl = AbstractControl.createCopy(control, owner, rows, header);
			OperationResult result = abstractControl.operate(this.applicationConnector.getAppConnection().getApplication().service(), window, operation);
			return Optional.of(result);
		}
		return Optional.empty();
	}

	private Optional<CheckingLayoutResult> check(Spec spec, IWindow window, IControl control) throws Exception
	{
		if (isApplicationRun())
		{
			IControl owner = window.getOwnerControl(control);
			IControl rows = window.getRowsControl(control);
			IControl header = window.getHeaderControl(control);

			AbstractControl abstractControl = AbstractControl.createCopy(control, owner, rows, header);
			CheckingLayoutResult result = abstractControl.checkLayout(this.applicationConnector.getAppConnection().getApplication().service(), window, spec);
			return Optional.of(result);
		}
		return Optional.empty();
	}

	private void initController() throws Exception
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(DictionaryFx.class.getResource("DictionaryTab.fxml"));
			this.controller.init(this, getFactory().getSettings(), getFactory().getConfiguration(), this.evaluator);
			getFactory().getConfiguration().register(this);
			this.isControllerInit = true;
			this.applicationConnector = new ApplicationConnector(getFactory());
			this.applicationConnector.setApplicationListener(this::displayApplicationStatus);
		}
	}

	private Locator getLocator(IControl control)
	{
		if (control != null)
		{
			return control.locator();
		}
		return null;
	}

	private void displayTitle(String name)
	{
		this.controller.displayTitle(name);
	}

	private void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		this.controller.displayDialog(window, windows);
	}

	private void displaySection(IWindow.SectionKind sectionKind) throws Exception
	{
		this.controller.displaySection(sectionKind);
	}

	private void displayElementInfo(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
		Collection<IControl> controls = null;
		Collection<IControl> owners = null;
		Collection<IControl> rows = null;
		IControl owner = null;
		IControl row = null;
		IControl header = null;
		if (window != null)
		{
			owners = controlsWithId(window, null);
			owner = window.getOwnerControl(control);
			rows = controlsWithId(window, null);
			row = window.getRowsControl(control);
			header = window.getHeaderControl(control);
		}
		this.controller.displayElementInfo(window, control, owners, owner, rows, row, header);
	}

	public void displayElement(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
    	Collection<IControl> controls = null;
    	Collection<IControl> owners = null;
    	Collection<IControl> rows = null;
		IControl owner = null;
    	IControl row = null;
    	IControl header = null;
    	if (window != null)
    	{
			controls = window.getControls(sectionKind);
    		owners = controlsWithId(window, null);
			owner = window.getOwnerControl(control);
			rows = controlsWithId(window, null);
    		row = window.getRowsControl(control);
    		header = window.getHeaderControl(control);
    	}
		this.controller.displaySection(sectionKind);
		this.controller.displayElement(controls, control);
		this.controller.displayElementInfo(window, control, owners, owner, rows, row, header);
	}

	public void displayElementWithoutInfo(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception {
		Collection<IControl> controls = null;
		if (window != null)
		{
			controls = window.getControls(sectionKind);
		}
		this.controller.displayElement(controls, control);
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

	private void displayApplicationStatus(ApplicationStatus status, AppConnection appConnection, Throwable throwable)
	{
		this.controller.displayApplicationStatus(status, throwable, appConnection);
	}

	private void displayApplicationControl(String title) throws Exception
	{
		Collection<String> entries = getFactory().getConfiguration().getApplications();
		this.controller.displayActionControl(entries, this.currentAdapter, title);
	}

	private void storeSettings(Settings settings) throws Exception
	{
		String absolutePath = new File(this.getName()).getAbsolutePath();
		String idAppEntry = this.currentAdapter;
		if (!Str.IsNullOrEmpty(idAppEntry))
		{
			settings.setValue(Main.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath, idAppEntry);
		}
		else
		{
			settings.remove(Main.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath);
		}
		settings.saveIfNeeded();
	}

	private void restoreSettings(Settings settings)
	{
		String absolutePath = new File(this.getName()).getAbsolutePath();
		Settings.SettingsValue value = settings.getValue(Main.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath);
		Optional.ofNullable(value).ifPresent(s -> {
			String idApp = s.getValue();
			this.applicationConnector.setIdAppEntry(idApp);
			this.controller.displayActionControl(null, idApp, null);
		});
	}
}
