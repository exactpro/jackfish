////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.gui.DialogGetProperties;
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
import com.exactprosystems.jf.documents.matrix.parser.Getter;
import com.exactprosystems.jf.documents.matrix.parser.MutableListener;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFxController.Result;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatchThrow;

public class DictionaryFx extends GuiDictionary
{
	private final static String NEW_DIALOG_NAME = "NewDialog";

	private static final String DIALOG_DICTIONARY_SETTINGS = "DictionarySettings";
	private static AbstractControl copyControl;
	private static Window          copyWindow;

	private AbstractEvaluator      evaluator;
	private ApplicationConnector   applicationConnector;
	private volatile boolean isWorking = false;
	private DictionaryFxController controller = new DictionaryFxController();

	//variables for actions controller

	//region variables for ActionsController
	private final MutableArrayList<MutableValue<String>> titlesList = new MutableArrayList<>();
	private final MutableArrayList<MutableValue<String>> appsList = new MutableArrayList<>();
	private final MutableValue<String> currentStoredApp = new MutableValue<>("");
	private final MutableValue<String> currentApp = new MutableValue<>("");
	//endregion

	//region variables for NavigationController
	private final MutableValue<IWindow> currentWindow = new MutableValue<>();
	private final MutableValue<SectionKind> currentSection = new MutableValue<>();
	private final MutableValue<IControl> currentElement = new MutableValue<>();
	private final MutableArrayList<IControl> elements = new MutableArrayList<>();
	private BiConsumer<IWindow, String> changeWindowName = (window, newName) -> {};
	//endregion

	//region variables for ElementInfoController

	//endregion

	public DictionaryFx(String fileName, DocumentFactory factory)
	{
		this(fileName, factory, null);
	}

	public DictionaryFx(String fileName, DocumentFactory factory, String currentAdapter)
	{
		super(fileName, factory);
		this.currentApp.set(currentAdapter);
		this.evaluator = factory.createEvaluator();
	}

	public static List<String> convertToString(Collection<? extends MutableValue<String>> collection)
	{
		return collection.stream().map(Getter::get).collect(Collectors.toList());
	}

	public static List<MutableValue<String>> convertToMutable(Collection<? extends String> collection)
	{
		return collection.stream().map(MutableValue<String>::new).collect(Collectors.toList());
	}

	//region AbstractDocument
	@Override
	public void display() throws Exception
	{
		super.display();
		getNameProperty().fire();

		//display all windows
		super.fire();
		this.currentSection.set(SectionKind.Run);
		this.setCurrentWindow(getFirstWindow());

		this.appsList.from(convertToMutable(getFactory().getConfiguration().getApplications()));
		this.currentApp.fire();


//		initController();


//		IWindow window = getFirstWindow();
//		displayDialog(window, getWindows());
//		displaySection(SectionKind.Run);
//		if (window != null)
//		{
//			IControl control = window.getFirstControl(SectionKind.Run);
//			displayElement(window, SectionKind.Run, control);
//		}
//		boolean isConnected = this.applicationConnector != null && this.applicationConnector.getAppConnection() != null;
//		displayApplicationStatus(isConnected ? ApplicationStatus.Connected : ApplicationStatus.Disconnected, isConnected ? this.applicationConnector.getAppConnection() : null, null);
//		displayApplicationControl(null);
//
//		restoreSettings(getFactory().getSettings());
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
			ButtonType desision = DialogsHelper.showSaveFileDialog(getNameProperty().get());
			if (desision == ButtonType.YES)
			{
				save(getNameProperty().get());
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
		stopApplication();
		storeSettings(getFactory().getSettings());
		super.close();
	}

	//endregion

	//region public methods
	AbstractEvaluator getEvaluator()
	{
		return this.evaluator;
	}

	MutableArrayList<MutableValue<String>> getTitles()
	{
		return this.titlesList;
	}

	MutableArrayList<MutableValue<String>> getAppsList()
	{
		return appsList;
	}

	MutableListener<String> currentStoredApp()
	{
		return currentStoredApp;
	}
	public void setCurrentStoredApp(String currentAdapterStore)
	{
		this.currentStoredApp.set(currentAdapterStore);
		//TODO add
	}

	MutableListener<String> currentApp()
	{
		return this.currentApp;
	}
	public void setCurrentApp(String adapter)
	{
		this.currentApp.set(adapter);
		//TODO add
	}

	MutableListener<IWindow> currentWindow()
	{
		return this.currentWindow;
	}
	public void setCurrentWindow(IWindow window)
	{
		this.currentWindow.set(window);
		this.displayElements();
	}
	public IWindow getCurrentWindow()
	{
		return this.currentWindow.get();
	}
	public void setChangeWindowName(BiConsumer<IWindow, String> biConsumer)
	{
		this.changeWindowName = biConsumer;
	}

	MutableListener<SectionKind> currentSection()
	{
		return this.currentSection;
	}
	public void setCurrentSection(SectionKind sectionKind)
	{
		this.currentSection.set(sectionKind);
		this.displayElements();
	}

	MutableArrayList<IControl> currentElements()
	{
		return this.elements;
	}
	MutableListener<IControl> currentElement()
	{
		return this.currentElement;
	}
	public void setCurrentElement(IControl control)
	{
		this.currentElement.set(control);
	}

	//endregion

	@Deprecated
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

	@Deprecated
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

	@Deprecated
	public void elementChanged(IWindow window, IWindow.SectionKind sectionKind, IControl control) throws Exception
	{
		((AbstractControl) control).correctAllXml();
		displayElementInfo(window, sectionKind, control);
	}


	//region new api

	//region work with dialogs
	public void createNewDialog()
	{
		Window newWindow = new Window(this.generateNewDialogName(NEW_DIALOG_NAME));
		newWindow.correctAll();

		IWindow oldWindow = this.currentWindow.get();

		Command undo = () ->
		{
			super.removeWindow(newWindow);
			this.currentWindow.set(oldWindow);
		};

		Command redo = () ->
		{
			super.addWindow(newWindow);
			this.currentWindow.set(newWindow);
		};

		addCommand(undo, redo);
	}

	public void removeCurrentDialog()
	{
		IWindow window = this.currentWindow.get();
		int indexOf = indexOf(window);

		Command undo = () ->
		{
			super.addWindow(indexOf, (Window) window);
			this.currentWindow.set(window);
		};

		Command redo = () ->
		{
			super.removeWindow(window);
			int size = getWindows().size();
			if (size == 0)
			{
				this.currentWindow.set(null);
			}
			else
			{
				IWindow newWindow = super.getWindows().toArray(new IWindow[size])[Math.max(0, indexOf - 1)];
				this.currentWindow.set(newWindow);
			}
		};

		addCommand(undo, redo);
	}

	public void dialogCopy() throws Exception
	{
		IWindow window = this.currentWindow.get();
		DictionaryFx.copyWindow = Window.createCopy((Window) window);
	}

	public void dialogPaste() throws Exception
	{
		if(DictionaryFx.copyWindow != null)
		{
			IWindow oldWindow = this.currentWindow.get();
			Window copiedWindow = Window.createCopy(DictionaryFx.copyWindow);
			copiedWindow.setName(this.generateNewDialogName(copyWindow.getName() + "_copy"));
			Command undo = () ->
			{
				super.removeWindow(copiedWindow);
				this.currentWindow.set(oldWindow);
			};

			Command redo = () ->
			{
				super.addWindow(copiedWindow);
				this.currentWindow.set(copiedWindow);
			};
			addCommand(undo, redo);
		}
		else
		{
			DialogsHelper.showError("No available dialogs for paste. Firstly copy some dialog before paste.");
		}

	}

	public boolean checkDialogName(String name)
	{
		return super.getWindows().stream().noneMatch(window -> !Objects.equals(this.currentWindow.get(), window) && Objects.equals(window.getName(), name));
	}

	public void dialogRename(IWindow window, String newName)
	{
		String oldName = window.getName();
		if (oldName.equals(newName))
		{
			return;
		}
		Command undo = () ->
		{
			window.setName(oldName);
			this.changeWindowName.accept(window, oldName);
		};

		Command redo = () ->
		{
			window.setName(newName);
			this.changeWindowName.accept(window, newName);
		};

		addCommand(undo, redo);

	}
	//endregion

	//endregion



	public void dialogTest(IWindow window, List<IControl> controls) throws Exception
	{

		Set<ControlKind> supported = this.applicationConnector.getAppConnection().getApplication().getFactory().supportedControlKinds();

		Thread thread = new Thread(new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				checkIsWorking(() -> controls.forEach(control ->
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

							Collection<String> all = service().findAll(owner, locator);

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
				}));

				return null;
			}
		});
		thread.setName("Test dialog, thread id : " + thread.getId());
		thread.start();

	}

	private void checkIsWorking(Common.Function a) throws Exception
	{
		if (isApplicationRun())
		{
			if (getIsWorking())
			{
				showNotification();
			}
			else
			{
				setIsWorking(true);
				try
				{
					a.call();
				}
				finally
				{
					setIsWorking(false);
				}
			}
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
			super.getWindows().remove(newIndex.intValue());
			this.addWindow(lastIndex, (Window) window);
			this.displayDialog(window, getWindows());
			displayElement(window, section, window.getFirstControl(section));
		}, "");
		Command redo = () -> Common.tryCatch(() ->
		{
			super.getWindows().remove(lastIndex);
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
		}
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
		}
	}

	public void parameterGoToOwner(IWindow window, IControl owner) throws Exception
	{
		checkIsWorking(() ->
		{
			if (owner != null)
			{
				SectionKind sectionKind = owner.getSection().getSectionKind();
				displaySection(sectionKind);
				displayElement(window, sectionKind, owner);
			}
		});
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
		if (!getFactory().getConfiguration().getStoreMap().values().contains(this.applicationConnector.getAppConnection()))
		{
			this.applicationConnector.stopApplication();
			displayApplicationControl(null);
		}
	}


	//region Do tab
	public void sendKeys(String text, IControl control, IWindow window) throws Exception
	{
		checkIsWorking(() -> this.operate(Do.text(text), window, control));
	}

	public void click(IControl control, IWindow window) throws Exception
	{
		checkIsWorking(() -> this.operate(Do.click(), window, control));
	}

	public void getValue(IControl control, IWindow window) throws Exception
	{
		checkIsWorking(() ->
		{
			Optional<OperationResult> operate = this.operate(Do.getValue(), window, control);
			operate.ifPresent(opResult -> this.controller.println(opResult.humanablePresentation()));
		});
	}

	public void find(IControl control, IWindow window) throws Exception
	{
		displayImage(null);
		checkIsWorking(() ->
		{
			Locator owner = getLocator(window.getOwnerControl(control));
			Locator locator = getLocator(control);
			IRemoteApplication service = service();
			Collection<String> all = service.findAll(owner, locator);
			for (String str : all)
			{
				this.controller.println(str);
			}
			ImageWrapper imageWrapper = service.getImage(owner, locator);
			displayImage(imageWrapper);
		});
	}

	public void doIt(Object obj, IControl control, IWindow window) throws Exception
	{
		checkIsWorking(() ->
		{
			if (obj instanceof Operation) {
				Operation operation = (Operation) obj;

				Optional<OperationResult> result = this.operate(operation, window, control);
				result.ifPresent(operate -> this.controller.println(operate.humanablePresentation()));
			} else if (obj instanceof Spec) {
				Spec spec = (Spec) obj;

				Optional<CheckingLayoutResult> result = this.check(spec, window, control);
				result.ifPresent(check ->
				{
					if (check.isOk()) {
						this.controller.println("Check is passed");
					} else {
						this.controller.println("Check is failed:");
						for (String err : check.getErrors()) {
							this.controller.println("" + err);
						}
					}
				});
			}
		});
	}
	//endregion

	//region Switch tab
	public void switchTo(String selectedItem) throws Exception
	{
		checkIsWorking(() ->
		{
			Map<String, String> map = new HashMap<>();
			map.put("Title", selectedItem);
			service().switchTo(map, true);
		});
	}

	public void switchToCurrent(IControl control, IWindow window) throws Exception
	{
		checkIsWorking(() ->
		{
			if (control != null)
			{
				Locator owner = null;
				if(!Str.IsNullOrEmpty(control.getOwnerID()) && window != null)
				{
					owner = window.getControlForName(null, control.getOwnerID()).locator();
				}
				service().switchToFrame(owner, control.locator());
			}
		});
	}

	public void switchToParent() throws Exception
	{
		checkIsWorking(() -> service().switchToFrame(null, null));
	}

	public void refreshTitles() throws Exception
	{
		this.checkIsWorking(() -> this.titlesList.from(convertToMutable(service().titles())));
	}
	//endregion

	//region Navigate tab
	public void navigateBack() throws Exception
	{
		checkIsWorking(() ->
			service().navigate(NavigateKind.BACK)
		);
	}

	public void navigateForward() throws Exception
	{
		checkIsWorking(() ->
			service().navigate(NavigateKind.FORWARD)
		);
	}

	public void refresh() throws Exception
	{
		checkIsWorking(() ->
			{
				service().refresh();
				displayApplicationControl(null);
			}
		);
	}

	public void closeWindow() throws Exception
	{
		checkIsWorking(() ->
			{
				String s = service().closeWindow();
				if (Str.areEqual(s, "")) {
					throw new Exception("Can not close the window");
				}
			}
		);
	}
	//endregion

	//region NewInstance tab
	public void newInstance(Map<String, String> parameters) throws Exception
	{
		checkIsWorking(() ->
			{
				Map<String, String> evaluatedMap = new HashMap<>();

				for (Map.Entry<String, String> entry : parameters.entrySet())
				{
					evaluatedMap.put(entry.getKey(), String.valueOf(this.evaluator.evaluate(entry.getValue())));
				}
				service().newInstance(evaluatedMap);
			}
		);
	}
	//endregion

	//region Change tab
	public void moveTo(int x, int y) throws Exception
	{
		checkIsWorking(() ->
			service().moveWindow(x, y)
		);
	}

	public void resize(Resize resize, int h, int w) throws Exception
	{
		checkIsWorking(() ->
			service().resize(resize, h, w)
		);
	}
	//endregion

	//region Properties tab
	public void getProperty(String propertyName, Object propValue) throws Exception
	{
		checkIsWorking(() ->
			{
				Serializable property = null;
				if (propValue instanceof Serializable)
				{
					property = service().getProperty(propertyName, (Serializable) propValue);
				}
				else if (propValue == null)
				{
					property = service().getProperty(propertyName, null);
				}
				else
				{
					throw new Exception("You must set only Serializable or null value");
				}

				Optional.ofNullable(property)
						.map(Serializable::toString)
						.ifPresent(this.controller::println);
			}
		);
	}

	public void setProperty(String propertyName, Object value) throws Exception
	{
		checkIsWorking(() ->
			{
				if (value instanceof Serializable)
				{
					service().setProperty(propertyName, (Serializable) value);
				}
				else if (value == null)
				{
					service().setProperty(propertyName, null);
				}
				else
				{
					throw new Exception("You must set only Serializable or null value");
				}
			}
		);
	}

	//endregion

	//region Dialog tab
	public void dialogMoveTo(int x, int y) throws Exception
	{
		checkIsWorking(() ->
			{
				Locator selfLocator = getLocatorForCurrentWindow();
				service().moveDialog(selfLocator, x, y);
			}
		);
	}

	public void dialogResize(Resize resize, int h, int w) throws Exception
	{
		checkIsWorking(() ->
			{
				Locator selfLocator = getLocatorForCurrentWindow();
				service().resizeDialog(selfLocator, resize, h, w);
			}
		);
	}

	public void dialogGetProperty(String propertyName) throws Exception
	{
		checkIsWorking(() ->
				{
					Locator selfLocator = getLocatorForCurrentWindow();

					String result = null;
					if(propertyName.equals(DialogGetProperties.sizeName))
					{
						Dimension dialogSize = service().getDialogSize(selfLocator);
						result = String.format("Dimension[width = %d, height = %d]", (int)dialogSize.getWidth(), (int)dialogSize.getHeight());
					}
					if(propertyName.equals(DialogGetProperties.positionName))
					{
						Point dialogPosition = service().getDialogPosition(selfLocator);
						result = String.format("Point[x = %d, y = %d]", (int)dialogPosition.getX(), (int)dialogPosition.getY());
					}
					Optional.ofNullable(result).ifPresent(this.controller::println);
				}
		);
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
		service().startNewDialog();
		if (isApplicationRun())
		{
			IControl owner = window.getOwnerControl(control);
			IControl rows = window.getRowsControl(control);
			IControl header = window.getHeaderControl(control);

			AbstractControl abstractControl = AbstractControl.createCopy(control, owner, rows, header);
			OperationResult result = abstractControl.operate(service(), window, operation);
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
			CheckingLayoutResult result = abstractControl.checkLayout(service(), window, spec);
			return Optional.of(result);
		}
		return Optional.empty();
	}

	private void initController() throws Exception
	{
//		if (!this.isControllerInit)
//		{
//			this.controller = Common.loadController(DictionaryFx.class.getResource("DictionaryTab.fxml"));
//			this.controller.init(this, getFactory().getSettings(), getFactory().getConfiguration(), this.evaluator);
//			getFactory().getConfiguration().register(this);
//			this.isControllerInit = true;
//			this.applicationConnector = new ApplicationConnector(getFactory());
//			this.applicationConnector.setApplicationListener(this::displayApplicationStatus);
//		}
	}

	private Locator getLocator(IControl control)
	{
		if (control != null)
		{
			return control.locator();
		}
		return null;
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

	//region private methods
	private String generateNewDialogName(String initial)
	{
		List<String> list = getWindows().stream().map(IWindow::getName).collect(Collectors.toList());
		if (!list.contains(initial))
		{
			return initial;
		}
		int i = 1;
		while (list.contains(initial + i))
		{
			i++;
		}
		return initial + i;
	}

	private void displayElements()
	{
		IWindow window = this.currentWindow.get();
		SectionKind sectionKind = this.currentSection.get();
		if (window != null)
		{
			this.elements.from(window.getControls(sectionKind));
		}
		else
		{
			this.elements.clear();
		}
		this.setCurrentElement(this.elements.isEmpty() ? null : this.elements.get(0));
	}

	private IRemoteApplication service()
	{
		return this.applicationConnector.getAppConnection().getApplication().service();
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
//		this.controller.displayActionControl(entries, this.currentAdapter, title);
	}

	private void storeSettings(Settings settings) throws Exception
	{
//		String absolutePath = new File(getNameProperty().get()).getAbsolutePath();
//		String idAppEntry = this.currentAdapter;
//		if (!Str.IsNullOrEmpty(idAppEntry))
//		{
//			settings.setValue(Settings.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath, idAppEntry);
//		}
//		else
//		{
//			settings.remove(Settings.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath);
//		}
//		settings.saveIfNeeded();
	}

	private void restoreSettings(Settings settings)
	{
		String absolutePath = new File(getNameProperty().get()).getAbsolutePath();
		Settings.SettingsValue value = settings.getValue(Settings.MAIN_NS, DIALOG_DICTIONARY_SETTINGS, absolutePath);
		Optional.ofNullable(value).ifPresent(s -> {
			String idApp = s.getValue();
			this.applicationConnector.setIdAppEntry(idApp);
			this.controller.displayActionControl(null, idApp, null);
		});
	}

	private void showNotification()
	{
		DialogsHelper.showInfo("Please wait until previous command will be complete");
	}

	private void setIsWorking(boolean b){
		this.isWorking = b;
	}

	private boolean getIsWorking(){
		return this.isWorking;
	}

	private IWindow getCurrentWindowOld()
	{
		return this.currentWindow.get();
	}

	private Locator getLocatorForCurrentWindow() throws Exception
	{
		IWindow currentWindow = getCurrentWindowOld();
		IControl selfControl = currentWindow.getSelfControl();

		if (selfControl == null)
		{
			throw new Exception("Can't find self control for current window.");
		}
		return selfControl.locator();
	}

	//endregion
}
