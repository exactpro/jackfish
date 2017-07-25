////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.actions.ReadableValue;
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
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFxController.Result;
import com.exactprosystems.jf.tool.dictionary.dialog.DialogWizard;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatchThrow;

@XmlRootElement(name = "dictionary")
public class DictionaryFx extends GuiDictionary
{
	private static final String DIALOG_DICTIONARY_SETTINGS = "DictionarySettings";
	private static AbstractControl copyControl;
	private static Window          copyWindow;
	private boolean isControllerInit = false;
	private String                 currentAdapterStore;
	private String                 currentAdapter;
	private DictionaryFxController controller;
	private AbstractEvaluator      evaluator;
	private ApplicationConnector   applicationConnector;

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
		if (this.controller != null)
		{
			this.controller.saved(getName());
			displayTitle(getName());
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
		Map<String, Object> storeMap = getFactory().getConfiguration().getStoreMap();
		Collection<String> stories = new ArrayList<>();
		if (!storeMap.isEmpty())
		{
			stories.add("");
			storeMap.forEach((s, o) ->
			{
				if (o instanceof AppConnection)
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
		((AbstractControl) control).correctAllXml();
		displayElementInfo(window, sectionKind, control);
	}


	//------------------------------------------------------------------------------------------------------------------
	public void dialogNew(IWindow.SectionKind sectionKind) throws Exception
	{
		String nameNewWindow = "NewWindow";

		Window window = new Window(nameNewWindow);
		window.correctAll();

		Command undo = () -> Common.tryCatch(() ->
		{
			removeWindow(window);
			Collection<IWindow> windows = getWindows();
			IWindow oldWindow = (IWindow) windows.toArray()[windows.size() - 1];
			displayDialog(oldWindow, windows);
			displayElement(oldWindow, sectionKind, oldWindow.getFirstControl(sectionKind));
		}, "");

		Command redo = () -> Common.tryCatch(() ->
		{
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

			Command undo = () -> Common.tryCatch(() ->
			{
				addWindow(indexOf, (Window) window);
				displayDialog(window, getWindows());
				displayElement(window, sectionKind, window.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() ->
			{
				removeWindow(window);
				IWindow anotherWindow = getFirstWindow();
				displayDialog(anotherWindow, getWindows());
				if (anotherWindow != null)
				{
					displayElement(anotherWindow, sectionKind, anotherWindow.getFirstControl(sectionKind));
				}
				else
				{
					clearElements(sectionKind);
				}
			}, "");

			addCommand(undo, redo);
			super.changed(true);
		}
	}

	public void dialogCopy(IWindow window) throws Exception
	{
		copyWindow = Window.createCopy((Window) window);
	}

	public void dialogPaste(IWindow.SectionKind sectionKind) throws Exception
	{
		if (copyWindow != null)
		{
			Window clone = Window.createCopy(copyWindow);
			int indexOf = indexOf(clone);
			Command undo = () -> Common.tryCatch(() ->
			{
				removeWindow(clone);
				Collection<IWindow> windows = getWindows();
				IWindow oldWindow = (IWindow) windows.toArray()[Math.min(indexOf, windows.size() - 1)];
				displayDialog(oldWindow, windows);
				displayElement(oldWindow, sectionKind, oldWindow.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() ->
			{
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
			Command undo = () ->
			{
				window.setName(oldName);
				displayDialog(window, getWindows());
			};

			Command redo = () ->
			{
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
			Set<ControlKind> supported = this.applicationConnector.getAppConnection().getApplication().getFactory().supportedControlKinds();

			Thread thread = new Thread(new Task<Void>()
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
								Locator owner = getLocator(window.getOwnerControl(control));
								Locator locator = getLocator(control);

								Collection<String> all = applicationConnector.getAppConnection().getApplication().service().findAll(owner, locator);

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
		Command undo = () -> Common.tryCatch(() ->
		{
			super.windows.remove(newIndex.intValue());
			this.addWindow(lastIndex, (Window) window);
			this.displayDialog(window, getWindows());
			displayElement(window, section, window.getFirstControl(section));
		}, "");
		Command redo = () -> Common.tryCatch(() ->
		{
			super.windows.remove(lastIndex);
			this.addWindow(newIndex, (Window) window);
			this.displayDialog(window, getWindows());
			displayElement(window, section, window.getFirstControl(section));
		}, "");
		addCommand(undo, redo);
	}

	public void openDialogWizard(IWindow window) throws Exception
	{
		if (!isApplicationRun())
		{
			DialogsHelper.showError("Application not starting.\nStart application before call the wizard");
			return;
		}
		IControl selfControl = window.getSelfControl();
		if (selfControl == null)
		{
			DialogsHelper.showError("Self control is null.\nFill the self control before call the wizard");
			return;
		}
		if (Str.IsNullOrEmpty(selfControl.getID()))
		{
			DialogsHelper.showError("Self should have ID.");
			return;
		}
		Window copyWindow = Window.createCopy(((Window) window));
		copyWindow.setName(window.getName());
		DialogWizard wizard = new DialogWizard(this, copyWindow, this.applicationConnector.getAppConnection());
		wizard.setOnAccept(w -> Common.tryCatch(() ->
		{
			int index = this.indexOf(window);
			this.removeWindow(window);
			this.addWindow(index, copyWindow);
			this.displayDialog(copyWindow, getWindows());
			this.displayElement(copyWindow, SectionKind.Run, copyWindow.getFirstControl(SectionKind.Run));

		}, "Error on hiding wizard"));
		Task<Integer> task = new Task<Integer>()
		{
			@Override
			protected Integer call() throws Exception
			{
				DialogsHelper.showInfo("Start found self control...\nPlease, wait");
				controller.setDisableWizardButton(true);
				Locator owner = getLocator(copyWindow.getOwnerControl(selfControl));
				Locator locator = getLocator(selfControl);
				IRemoteApplication service = applicationConnector.getAppConnection().getApplication().service();
				Collection<String> all = service.findAll(owner, locator);
				return all.size();
			}
		};
		task.setOnSucceeded(e ->
		{
			controller.setDisableWizardButton(false);
			Integer count = (Integer) e.getSource().getValue();
			if (count == 0)
			{
				DialogsHelper.showError("Self control not found");
				return;
			}
			if (count != 1)
			{
				DialogsHelper.showError("Found " + count + " instead of 1.");
				return;
			}
			wizard.show();
		});
		new Thread(task).start();
	}

	//------------------------------------------------------------------------------------------------------------------
	public void elementNew(IWindow window, IWindow.SectionKind sectionKind) throws Exception
	{
		if (window != null)
		{
			AbstractControl control = AbstractControl.create(ControlKind.Any);
			IControl firstControl = window.getFirstControl(SectionKind.Self);
			Optional.ofNullable(firstControl).ifPresent(owner -> Common.tryCatch(() -> control.set(AbstractControl.ownerIdName, owner.getID()), "Error on set owner owner"));
			Command undo = () -> Common.tryCatch(() ->
			{
				window.removeControl(control);
				displayElement(window, sectionKind, window.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() ->
			{
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

			Command undo = () -> Common.tryCatch(() ->
			{
				window.addControl(sectionKind, control);
				displayElement(window, sectionKind, control);
			}, "");

			Command redo = () ->
			{
				Common.tryCatch(() ->
				{
					if (!ref)
					{
						window.removeControl(control);
						IControl anotherControl = window.getFirstControl(sectionKind);
						displayElement(window, sectionKind, anotherControl);
					}
					else
					{
						boolean needRemove = DialogsHelper.showQuestionDialog("This element is the owner for other elements", "Remove it anyway?");

						if (needRemove)
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

			Command undo = () -> Common.tryCatch(() ->
			{
				window.removeControl(copy);
				displayElement(window, sectionKind, window.getFirstControl(sectionKind));
			}, "");

			Command redo = () -> Common.tryCatch(() ->
			{
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

	public boolean checkDialogName(IWindow currentWindow, String name)
	{
		long count = super.windows.stream().filter(w -> !Objects.equals(currentWindow, w) && Objects.equals(w.getName(), name)).count();
		return count == 0;
	}

	public boolean checkNewId(IWindow currentWindow, IControl currentControl, String id)
	{
		if (Str.IsNullOrEmpty(id))
		{
			return true;
		}
		List<IControl> all = currentWindow.allMatched((s, c) -> !Objects.equals(currentControl, c) && Objects.equals(c.getID(), id));
		return all.isEmpty();
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
		Command undo = () -> Common.tryCatch(() ->
		{
			window.removeControl(control);
			window.getSection(section).addControl(lastIndex, control);
			this.displayElement(window, section, control);
		}, "");

		Command redo = () -> Common.tryCatch(() ->
		{
			window.removeControl(control);
			window.getSection(section).addControl(newIndex, control);
			this.displayElement(window, section, control);
		}, "");
		addCommand(undo, redo);
	}


	//------------------------------------------------------------------------------------------------------------------
	public void parameterSet(IWindow window, IWindow.SectionKind sectionKind, IControl control, String parameter, Object value) throws Exception
	{
		tryCatchThrow(() ->
		{
			if (control != null && control instanceof AbstractControl)
			{
				AbstractControl copy = AbstractControl.createCopy(control);
				Object oldValue = copy.get(parameter);
				Command undo = () -> Common.tryCatch(() ->
				{
					((AbstractControl) control).set(parameter, trimIfString(oldValue));
					displayElement(window, sectionKind, control);
				}, "");
				Command redo = () -> Common.tryCatch(() ->
				{
					((AbstractControl) control).set(parameter, trimIfString(value));
					displayElement(window, sectionKind, control);
				}, "");
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
			Command undo = () -> Common.tryCatch(() ->
			{
				if (oldControl.getBindedClass() != newControl.getBindedClass())
				{
					Section section = (Section) window.getSection(sectionKind);
					section.replaceControl(newControl, control);
					displayElement(window, sectionKind, control);
				}
			}, "");
			Command redo = () -> Common.tryCatch(() ->
			{
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
		if (idAppStore.isEmpty() || idAppStore == null)
		{
			this.applicationConnector.setIdAppEntry(null);
			this.applicationConnector.setAppConnection(null);
			this.controller.displayApplicationStatus(ApplicationStatus.Disconnected, null, null, key -> null);
		}
		else
		{
			AppConnection appConnection = (AppConnection) getFactory().getConfiguration().getStoreMap().get(idAppStore);
			this.applicationConnector.setIdAppEntry(appConnection.getId());
			this.applicationConnector.setAppConnection(appConnection);
			this.controller.displayApplicationStatus(ApplicationStatus.ConnectingFromStore, null, appConnection, key -> getListProvider(appConnection, key));
		}
	}

	public void stopApplication() throws Exception
	{
		this.applicationConnector.stopApplication();
		displayApplicationControl(null);
	}


	//region Do tab
	public void sendKeys(String text, IControl control, IWindow window) throws Exception
	{
		this.operate(Do.text(text), window, control);
	}

	public void click(IControl control, IWindow window) throws Exception
	{
		this.operate(Do.click(), window, control);
	}

	public void getValue(IControl control, IWindow window) throws Exception
	{
		Optional<OperationResult> operate = this.operate(Do.getValue(), window, control);
		operate.ifPresent(opResult -> this.controller.println(opResult.humanablePresentation()));
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
			Operation operation = (Operation) obj;

			Optional<OperationResult> result = this.operate(operation, window, control);
			result.ifPresent(operate -> this.controller.println(operate.humanablePresentation()));
		}
		else if (obj instanceof Spec)
		{
			Spec spec = (Spec) obj;

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
			this.controller.println("Entered string is not Operation or Spec" + obj);
		}
	}
	//endregion

	//region Switch tab
	public void switchTo(String selectedItem) throws Exception
	{
		if (isApplicationRun())
		{
		    Map<String, String> map = new HashMap<>();
		    map.put("Title", selectedItem);
			this.applicationConnector.getAppConnection().getApplication().service().switchTo(map, true);
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
	//endregion

	//region Navigate tab
	public void navigateBack() throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().navigate(NavigateKind.BACK);
		}
	}

	public void navigateForward() throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().navigate(NavigateKind.FORWARD);
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

	public void closeWindow() throws Exception
	{
		if (isApplicationRun())
		{
			String s = this.applicationConnector.getAppConnection().getApplication().service().closeWindow();
			if (Str.areEqual(s, ""))
			{
				throw new Exception("Can not close the window");
			}
		}
	}
	//endregion

	//region NewInstance tab
	public void newInstance(Map<String, String> parameters) throws Exception
	{
		if (isApplicationRun())
		{
			Map<String, String> evaluatedMap = new HashMap<>();

			for (Map.Entry<String, String> entry : parameters.entrySet())
			{
				evaluatedMap.put(entry.getKey(), String.valueOf(this.evaluator.evaluate(entry.getValue())));
			}
			this.applicationConnector.getAppConnection().getApplication().service().newInstance(evaluatedMap);
		}
	}
	//endregion

	//region Change tab
	public void moveTo(int x, int y) throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().moveWindow(x, y);
		}
	}

	public void resize(boolean min, boolean max, boolean normal, int h, int w) throws Exception
	{
		if (isApplicationRun())
		{
			this.applicationConnector.getAppConnection().getApplication().service().resize(h, w, max, min, normal);
		}
	}
	//endregion

	//region Properties tab
	public void getProperty(String propertyName, Object propValue) throws Exception
	{
		if (isApplicationRun())
		{
			Serializable property = null;
			if (propValue instanceof Serializable)
			{
				property = this.applicationConnector.getAppConnection().getApplication().service().getProperty(propertyName, (Serializable) propValue);
			}
			else if (propValue == null)
			{
				property = this.applicationConnector.getAppConnection().getApplication().service().getProperty(propertyName, null);
			}
			else
			{
				throw new Exception("You must set only Serializable or null value");
			}

			Optional.ofNullable(property)
					.map(Serializable::toString)
					.ifPresent(this.controller::println);
		}
	}

	public void setProperty(String propertyName, Object value) throws Exception
	{
		if (isApplicationRun())
		{
			if (value instanceof Serializable)
			{
				this.applicationConnector.getAppConnection().getApplication().service().setProperty(propertyName, (Serializable) value);
			}
			else if (value == null)
			{
				this.applicationConnector.getAppConnection().getApplication().service().setProperty(propertyName, null);
			}
			else
			{
				throw new Exception("You must set only Serializable or null value");
			}
		}
	}

	//endregion


	//------------------------------------------------------------------------------------------------------------------
	// private methods
	//------------------------------------------------------------------------------------------------------------------
    private Object trimIfString(Object value)
    {
        if (value instanceof String)
        {
            return ((String)value).trim();
        }
        return value;
    }
    
	private boolean isApplicationRun()
	{
		boolean isRun = this.applicationConnector != null && this.applicationConnector.getAppConnection() != null && this.applicationConnector.getAppConnection().isGood();
		if (!isRun)
		{
			DialogsHelper.showInfo("Start a application before doing any actions");
		}
		return isRun;
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

	public void displayDialog(IWindow window, Collection<IWindow> windows)
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
		IControl reference = null;
		if (window != null)
		{
			owners = controlsWithId(window, null);
			owner = window.getOwnerControl(control);
			rows = controlsWithId(window, null);
			row = window.getRowsControl(control);
			header = window.getHeaderControl(control);
			reference = window.getReferenceControl(control);
		}
		this.controller.displayElementInfo(window, control, owners, owner, rows, row, header, reference);
	}

	public void displayElement(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
		Collection<IControl> controls = null;
		Collection<IControl> owners = null;
		Collection<IControl> rows = null;
		IControl owner = null;
		IControl row = null;
		IControl header = null;
		IControl reference = null;
		if (window != null)
		{
			controls = window.getControls(sectionKind);
			owners = controlsWithId(window, null);
			owner = window.getOwnerControl(control);
			rows = controlsWithId(window, null);
			row = window.getRowsControl(control);
			header = window.getHeaderControl(control);
			reference = window.getReferenceControl(control);
		}
		this.controller.displaySection(sectionKind);
		this.controller.displayElement(controls, control);
		this.controller.displayElementInfo(window, control, owners, owner, rows, row, header, reference);
	}

	public void clearElements(IWindow.SectionKind sectionKind)
	{
		this.controller.displaySection(sectionKind);
		this.controller.displayElement(Collections.emptyList(), null);
		this.controller.displayElementInfo(null, null, Collections.emptyList(), null, Collections.emptyList(), null, null, null);
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
		this.controller.displayApplicationStatus(status, throwable, appConnection, key -> {
			if (appConnection != null)
			{
				return getListProvider(appConnection, key);
			}
			return null;
		});
	}

	private ListProvider getListProvider(AppConnection appConnection, String key)
	{
		IApplicationFactory factory = appConnection.getApplication().getFactory();
		if (factory.canFillParameter(key))
		{
			return () -> Arrays.stream(factory.listForParameter(key))
					.map(this.evaluator::createString)
					.map(ReadableValue::new)
					.collect(Collectors.toList());
		}
		return null;
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
			settings.setValue(Settings.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath, idAppEntry);
		}
		else
		{
			settings.remove(Settings.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath);
		}
		settings.saveIfNeeded();
	}

	private void restoreSettings(Settings settings)
	{
		String absolutePath = new File(this.getName()).getAbsolutePath();
		Settings.SettingsValue value = settings.getValue(Settings.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath);
		Optional.ofNullable(value).ifPresent(s -> {
			String idApp = s.getValue();
			this.applicationConnector.setIdAppEntry(idApp);
			this.controller.displayActionControl(null, idApp, null);
		});
	}
}
