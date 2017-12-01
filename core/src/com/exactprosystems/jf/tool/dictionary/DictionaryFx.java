////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

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
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatchThrow;

public class DictionaryFx extends GuiDictionary
{
	public static final String DIALOG_DICTIONARY_SETTINGS = "DictionarySettings";
	private static final String NEW_DIALOG_NAME = "NewDialog";

	private static AbstractControl copyControl;
	private static Window          copyWindow;

	private AbstractEvaluator      evaluator;
	private ApplicationConnector   applicationConnector;
	private volatile boolean isWorking = false;

	private final MutableValue<String> out = new MutableValue<>();
	private final MutableValue<ApplicationStatusBean> appStatus = new MutableValue<>();

	private Task<Void> testingElementTask;

	//region variables for ActionsController
	private final MutableArrayList<MutableValue<String>> titlesList = new MutableArrayList<>();
	private final MutableArrayList<MutableValue<String>> appsList = new MutableArrayList<>();
	private final MutableValue<String> currentStoredApp = new MutableValue<>("");
	private final MutableValue<String> currentApp = new MutableValue<>("");
	private final MutableValue<ImageWrapper> image = new MutableValue<>();
	//endregion

	//region variables for NavigationController
	private final MutableValue<IWindow>              currentWindow         = new MutableValue<>();
	private final MutableValue<SectionKind>          currentSection        = new MutableValue<>();
	private final MutableValue<IControl>             currentElement        = new MutableValue<>();
	private final MutableArrayList<IControl>         elements              = new MutableArrayList<>();
	private final MutableArrayList<ControlWithState> testingElements       = new MutableArrayList<>();
	private BiConsumer<IWindow, String>              changeWindowName      = (window, newName) -> {};
	//TODO thiml about remove and add element consumer
	private Consumer<IControl>                       removeElementConsumer = element -> {};
	private BiConsumer<Integer, IControl>            addElementConsumer    = (i, e) -> {};
	//endregion

	//region variables for ElementInfoController

	//endregion

	public static List<String> convertToString(Collection<? extends MutableValue<String>> collection)
	{
		return collection.stream().map(Getter::get).collect(Collectors.toList());
	}

	public static List<MutableValue<String>> convertToMutable(Collection<? extends String> collection)
	{
		return collection.stream().map(MutableValue<String>::new).collect(Collectors.toList());
	}

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

	//region AbstractDocument
	@Override
	public void display() throws Exception
	{
		super.display();
		getNameProperty().fire();

		if (this.applicationConnector == null)
		{
			this.applicationConnector = new ApplicationConnector(getFactory());
		}
		this.applicationConnector.setApplicationListener((status1, appConnection1, throwable) -> this.appStatus.set(new ApplicationStatusBean(status1, appConnection1, throwable)));

		//display all windows
		super.fire();
		this.currentSection.set(SectionKind.Run);
		this.setCurrentWindow(getFirstWindow());

		this.appsList.from(convertToMutable(getFactory().getConfiguration().getApplications()));
		this.currentApp.fire();

		boolean isConnected = this.applicationConnector.getAppConnection() != null;
		ApplicationStatus status = isConnected ? ApplicationStatus.Connected : ApplicationStatus.Disconnected;
		AppConnection appConnection = isConnected ? this.applicationConnector.getAppConnection() : null;
		this.appStatus.set(new ApplicationStatusBean(status, appConnection, null));
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
		this.connectToApplicationFromStore();
	}

	MutableListener<String> currentApp()
	{
		return this.currentApp;
	}
	public void setCurrentApp(String adapter)
	{
		this.currentApp.set(adapter);
		this.applicationConnector.setIdAppEntry(adapter);
	}
	public AppConnection getApp()
	{
		return this.applicationConnector.getAppConnection();
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
	public SectionKind getCurrentSection()
	{
		return this.currentSection.get();
	}

	MutableArrayList<IControl> currentElements()
	{
		return this.elements;
	}
	MutableArrayList<ControlWithState> testingControls()
	{
		return this.testingElements;
	}

	MutableListener<IControl> currentElement()
	{
		return this.currentElement;
	}
	public void setCurrentElement(IControl control)
	{
		this.currentElement.set(control);
	}
	public IControl getCurrentElement()
	{
		return this.currentElement.get();
	}
	void onAddElement(BiConsumer<Integer, IControl> consumer)
	{
		this.addElementConsumer = consumer;
	}
	void onRemoveElement(Consumer<IControl> consumer)
	{
		this.removeElementConsumer = consumer;
	}

	MutableListener<ImageWrapper> imageProperty()
	{
		return this.image;
	}

	MutableListener<String> outProperty()
	{
		return this.out;
	}

	MutableListener<ApplicationStatusBean> appStatusProperty()
	{
		return this.appStatus;
	}

	//endregion

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

	public void dialogMove(IWindow window, Integer newIndex)
	{
		int lastIndex = super.indexOf(window);
		if (lastIndex == newIndex)
		{
			return;
		}
		Command undo = () ->
		{
			super.removeWindow(window);
			super.addWindow(lastIndex, (Window) window);
			this.currentWindow.set(window);
		};
		Command redo = () ->
		{
			super.removeWindow(window);
			super.addWindow(newIndex, (Window) window);
			this.setCurrentWindow(window);
		};
		addCommand(undo, redo);
	}

	public boolean checkDialogName(String name)
	{
		return super.getWindows().stream().noneMatch(window -> !Objects.equals(this.currentWindow.get(), window) && Objects.equals(window.getName(), name));
	}
	//endregion

	//region work with elements
	public void createNewElement() throws Exception
	{
		IWindow window = this.currentWindow.get();
		if (window != null)
		{
			AbstractControl newElement = AbstractControl.create(ControlKind.Any);
			IControl firstControl = window.getFirstControl(SectionKind.Self);
			Optional.ofNullable(firstControl).ifPresent(owner -> Common.tryCatch(() -> newElement.set(AbstractControl.ownerIdName, owner.getID()), "Error on set owner owner"));

			IControl oldElement = this.currentElement.get();
			Command undo = () ->
			{
				window.removeControl(newElement);
				this.removeElementConsumer.accept(newElement);
				this.currentElement.set(oldElement);
			};

			Command redo = () ->
			{
				window.addControl(this.currentSection.get(), newElement);
				this.addElementConsumer.accept(newElement.getSection().getControls().size() - 1, newElement);
				this.currentElement.set(newElement);
			};
			addCommand(undo, redo);
		}
	}

	public void removeCurrentElement()
	{
		IWindow window = this.currentWindow.get();
		SectionKind sectionKind = this.currentSection.get();
		ISection section = window.getSection(sectionKind);
		if (section instanceof Section)
		{
			IControl removedElement = this.currentElement.get();
			int indexOf = ((Section) section).indexOf((AbstractControl) removedElement);

			Command undo = () ->
			{
				section.addControl(indexOf, removedElement);
				this.addElementConsumer.accept(indexOf, removedElement);
				this.currentElement.set(removedElement);
			};

			Command redo = () ->
			{
				boolean ref = window.hasReferences(removedElement);
				boolean needRemove = true;
				if (ref)
				{
					needRemove = DialogsHelper.showQuestionDialog("This element is the owner for other elements", "Remove it anyway?");
				}
				if (needRemove)
				{
					((Section) section).removeControl(removedElement);
					this.removeElementConsumer.accept(removedElement);
					if (section.getControls().isEmpty())
					{
						this.currentElement.set(null);
					}
					else
					{
						this.currentElement.set(((Section) section).getByIndex(Math.max(0, indexOf - 1)));
					}
				}
			};
			addCommand(undo, redo);
		}

	}

	public void elementCopy() throws Exception
	{
		copyControl = AbstractControl.createCopy(this.currentElement.get());
	}

	public void elementPaste() throws Exception
	{
		IWindow window = this.currentWindow.get();
		if (copyControl != null && window != null)
		{
			AbstractControl copiedControl = AbstractControl.createCopy(copyControl);
			IControl oldControl = this.currentElement.get();

			SectionKind sectionKind = this.currentSection.get();
			Command undo = () ->
			{
				window.removeControl(copiedControl);
				this.removeElementConsumer.accept(copiedControl);
				this.currentElement.set(oldControl);
			};

			Command redo = () ->
			{
				ISection section = window.getSection(sectionKind);
				if (section != null)
				{
					section.addControl(copiedControl);
					this.addElementConsumer.accept(copiedControl.getSection().getControls().size() - 1, copiedControl);
				}
				this.currentElement.set(copiedControl);
			};

			addCommand(undo, redo);
		}
	}

	public void elementMove(IControl element, Integer newIndex)
	{
		IWindow window = this.currentWindow.get();
		SectionKind section = this.currentSection.get();
		if (window == null || section == null)
		{
			return;
		}
		int lastIndex = new ArrayList<>(window.getControls(section)).indexOf(element);
		if (lastIndex == newIndex)
		{
			return;
		}
		Command undo = () ->
		{
			window.removeControl(element);
			this.removeElementConsumer.accept(element);

			window.getSection(section).addControl(lastIndex, element);
			this.addElementConsumer.accept(lastIndex, element);

			this.currentElement.set(element);
		};

		Command redo = () ->
		{
			window.removeControl(element);
			this.removeElementConsumer.accept(element);

			window.getSection(section).addControl(newIndex, element);
			this.addElementConsumer.accept(newIndex, element);

			this.currentElement.set(element);
		};
		addCommand(undo, redo);
	}

	public void testingAllElements()
	{
		if (this.testingElementTask != null && this.testingElementTask.isRunning())
		{
			this.testingElementTask.cancel();
		}
		this.testingElementTask = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				DictionaryFx.this.checkIsWorking(() ->
				{
					DictionaryFx.this.testingElements.clear();
					IWindow window = DictionaryFx.this.currentWindow.get();
					SectionKind section = DictionaryFx.this.currentSection.get();
					if (window == null || section == null)
					{
						return;
					}

					Collection<IControl> testElements = window.getControls(section);
					if (testElements.isEmpty())
					{
						return;
					}
					Set<ControlKind> supported = DictionaryFx.this.applicationConnector.getAppConnection().getApplication().getFactory().supportedControlKinds();
					for (IControl element : testElements)
					{
						try
						{
							if (this.isCancelled())
							{
								return;
							}
							if (!supported.contains(element.getBindedClass()))
							{
								DictionaryFx.this.testingElements.add(new ControlWithState(element, "Not allowed", DictionaryFxController.Result.NOT_ALLOWED));
								continue;
							}
							Locator ownerLocator = Optional.ofNullable(window.getOwnerControl(element)).map(IControl::locator).orElse(null);
							Locator elementLocator = element.locator();
							Collection<String> all = DictionaryFx.this.service().findAll(ownerLocator, elementLocator);
							DictionaryFxController.Result result;
							if (all.size() == 1 || (Objects.equals(element.getAddition(), Addition.Many) && !all.isEmpty()))
							{
								result = DictionaryFxController.Result.PASSED;
							}
							else
							{
								result = DictionaryFxController.Result.FAILED;
							}
							DictionaryFx.this.testingElements.add(new ControlWithState(element, Integer.toString(all.size()), result));
						}
						catch (Exception e)
						{
							DictionaryFx.this.testingElements.add(new ControlWithState(element, "Error", DictionaryFxController.Result.FAILED));
						}
					}
				});
				return null;
			}
		};
		Thread thread = new Thread(this.testingElementTask);
		thread.setName("Thread testing all elements, thread id : " + thread.getId());
		thread.start();
	}
	//endregion

	//region element info
	public boolean isValidId(String id)
	{
		return Str.IsNullOrEmpty(id)
				|| this.currentWindow.get().allMatched((s, c) -> !Objects.equals(this.currentElement.get(), c) && Objects.equals(c.getID(), id)).isEmpty();
	}

	public void changeParameter(String parameterName, Object parameterValue) throws Exception
	{
		tryCatchThrow(() ->
		{
			IControl element = this.currentElement.get();
			if (element instanceof AbstractControl)
			{
				AbstractControl control = (AbstractControl) element;
				Object oldValue = control.get(parameterName);
				if (Objects.equals(oldValue, parameterValue))
				{
					return;
				}
				Command undo = () -> Common.tryCatch(() ->
				{
					control.set(parameterName, this.trimIfString(oldValue));
					this.currentElement.fire();
				},"");
				Command redo = () -> Common.tryCatch(() ->
				{
					control.set(parameterName, this.trimIfString(parameterValue));
					this.currentElement.fire();
				}, "");
				super.addCommand(undo, redo);
			}
		}, String.format("Cannot set field '%s' to value '%s'", parameterName, parameterValue));
	}

	public void changeControlKind(ControlKind kind) throws Exception
	{
		IControl element = this.currentElement.get();
		if (kind == element.getBindedClass())
		{
			return;
		}
		if (element instanceof AbstractControl)
		{
			AbstractControl oldControl = (AbstractControl) element;
			AbstractControl newControl = AbstractControl.createCopy(element, kind);

			IWindow window = this.currentWindow.get();
			SectionKind sectionKind = this.currentSection.get();

			Command undo = () ->
			{
				Section section = (Section) window.getSection(sectionKind);
				int index = section.replaceControl(newControl, oldControl);
				this.removeElementConsumer.accept(newControl);
				this.addElementConsumer.accept(index, oldControl);

				this.currentElement.set(oldControl);
			};
			Command redo = () ->
			{
				Section section = (Section) window.getSection(sectionKind);
				int index = section.replaceControl(oldControl, newControl);
				this.removeElementConsumer.accept(oldControl);
				this.addElementConsumer.accept(index,newControl);
				this.currentElement.set(newControl);
			};
			addCommand(undo, redo);
		}
	}

	public void goToOwner()
	{
		IControl element = this.currentElement.get();
		IWindow window = this.currentWindow.get();
		if (element != null && window != null)
		{
			IControl ownerControl = window.getControlForName(null, element.getOwnerID());
			if (ownerControl != null)
			{
				this.currentSection.set(ownerControl.getSection().getSectionKind());
				this.currentElement.set(ownerControl);
			}
		}
	}
	//endregion

	//region application
	public void startApplication() throws Exception
	{
		this.applicationConnector.setIdAppEntry(this.currentApp().get());
		this.applicationConnector.startApplication();
	}

	public void connectToApplication() throws Exception
	{
		this.applicationConnector.setIdAppEntry(this.currentApp().get());
		this.applicationConnector.connectApplication();
	}

	private void connectToApplicationFromStore()
	{
		String storedAppId = this.currentStoredApp.get();
		if (Str.IsNullOrEmpty(storedAppId))
		{
			this.applicationConnector.setIdAppEntry(null);
			this.applicationConnector.setAppConnection(null);
			this.appStatus.set(new ApplicationStatusBean(ApplicationStatus.Disconnected, null, null));
		}
		else
		{
			AppConnection appConnection = (AppConnection) getFactory().getConfiguration().getStoreMap().get(storedAppId);
			this.applicationConnector.setIdAppEntry(appConnection.getId());
			this.applicationConnector.setAppConnection(appConnection);
			this.appStatus.set(new ApplicationStatusBean(ApplicationStatus.ConnectingFromStore, appConnection, null));
		}
	}

	public void stopApplication() throws Exception
	{
		if (!getFactory().getConfiguration().getStoreMap().values().contains(this.applicationConnector.getAppConnection()))
		{
			this.applicationConnector.stopApplication();
			this.appStatus.set(new ApplicationStatusBean(ApplicationStatus.Disconnected, null, null));
		}
	}

	//endregion

	//region Do tab
	public void sendKeys(String text) throws Exception
	{
		this.checkIsWorking(() -> this.operate(Do.text(text), this.currentWindow.get(), this.currentElement.get()));
	}

	public void doIt(Object obj) throws Exception
	{
		this.checkIsWorking(() ->
		{
			IWindow window = this.currentWindow.get();
			IControl element = this.currentElement.get();
			if (obj instanceof Operation)
			{
				Operation operation = (Operation) obj;

				Optional<OperationResult> result = this.operate(operation, window, element);
				result.ifPresent(operate -> this.out.set(operate.humanablePresentation()));
			}
			else if (obj instanceof Spec)
			{
				Spec spec = (Spec) obj;

				Optional<CheckingLayoutResult> result = this.check(spec, window, element);
				result.ifPresent(check ->
				{
					if (check.isOk())
					{
						this.out.set("Check is passed");
					}
					else
					{
						this.out.set("Check is failed:");
						check.getErrors().forEach(this.out::set);
					}
				});
			}
		});
	}

	public void click() throws Exception
	{
		checkIsWorking(() -> this.operate(Do.click(), this.currentWindow.get(), this.currentElement.get()));
	}

	public void find() throws Exception
	{
		this.image.set(null);
		this.checkIsWorking(() ->
		{
			IWindow window = this.currentWindow.get();
			IControl element = this.currentElement.get();

			Locator owner = Optional.ofNullable(window.getOwnerControl(element)).map(IControl::locator).orElse(null);
			Locator locator = Optional.ofNullable(element).map(IControl::locator).orElse(null);

			IRemoteApplication service = this.service();
			Collection<String> all = service.findAll(owner, locator);
			all.forEach(this.out::set);

			ImageWrapper imageWrapper = service.getImage(owner, locator);
			this.image.set(imageWrapper);
		});
	}

	public void getValue() throws Exception
	{
		this.checkIsWorking(() ->
		{
			Optional<OperationResult> operate = this.operate(Do.getValue(), this.currentWindow.get(), this.currentElement.get());
			operate.ifPresent(opResult -> this.out.set(opResult.humanablePresentation()));
		});
	}
	//endregion

	//region Switch tab
	public void switchTo(String selectedItem) throws Exception
	{
		if (selectedItem == null)
		{
			return;
		}
		checkIsWorking(() ->
		{
			Map<String, String> map = new HashMap<>();
			map.put("Title", selectedItem);
			this.service().switchTo(map, true);
		});
	}

	public void switchToCurrent() throws Exception
	{
		this.checkIsWorking(() ->
		{
			IControl element = this.currentElement.get();
			if (element != null)
			{
				Locator owner = null;
				IWindow window = this.currentWindow.get();
				if(!Str.IsNullOrEmpty(element.getOwnerID()) && window != null)
				{
					IControl ownerElement = window.getControlForName(null, element.getOwnerID());
					if (ownerElement != null)
					{
						owner = ownerElement.locator();
					}
				}
				this.service().switchToFrame(owner, element.locator());
			}
		});
	}

	public void switchToParent() throws Exception
	{
		this.checkIsWorking(() -> this.service().switchToFrame(null, null));
	}

	public void refreshTitles() throws Exception
	{
		this.checkIsWorking(() ->
		{
			this.titlesList.from(convertToMutable(service().titles()));
			DialogsHelper.showNotifier("Titles refreshed successfully", Notifier.Success);
		});
	}
	//endregion

	//region Navigate tab
	public void navigateBack() throws Exception
	{
		this.checkIsWorking(() -> this.service().navigate(NavigateKind.BACK));
	}

	public void navigateForward() throws Exception
	{
		this.checkIsWorking(() -> this.service().navigate(NavigateKind.FORWARD));
	}

	public void refresh() throws Exception
	{
		this.checkIsWorking(() -> this.service().refresh());
	}

	public void closeWindow() throws Exception
	{
		this.checkIsWorking(() ->
			{
				String closedWindow = this.service().closeWindow();
				if (Str.IsNullOrEmpty(closedWindow))
				{
					throw new Exception("Can not close the window");
				}
			}
		);
	}
	//endregion

	//region NewInstance tab
	public void newInstance(Map<String, String> parameters) throws Exception
	{
		this.checkIsWorking(() ->
			{
				Map<String, String> evaluatedMap = new HashMap<>();

				for (Map.Entry<String, String> entry : parameters.entrySet())
				{
					evaluatedMap.put(entry.getKey(), String.valueOf(this.evaluator.evaluate(entry.getValue())));
				}
				this.service().newInstance(evaluatedMap);
			}
		);
	}
	//endregion

	//region Pos&Size tab
	public void moveTo(int x, int y) throws Exception
	{
		this.checkIsWorking(() -> this.service().moveWindow(x, y));
	}

	public void resize(Resize resize, int h, int w) throws Exception
	{
		this.checkIsWorking(() -> this.service().resize(resize, h, w));
	}
	//endregion

	//region Properties tab
	public void getProperty(String propertyName, Object propValue) throws Exception
	{
		this.checkIsWorking(() ->
			{
				Serializable property = null;
				if (propValue instanceof Serializable)
				{
					property = this.service().getProperty(propertyName, (Serializable) propValue);
				}
				else if (propValue == null)
				{
					property = this.service().getProperty(propertyName, null);
				}
				else
				{
					throw new Exception("You must set only Serializable or null value");
				}

				Optional.ofNullable(property)
						.map(Serializable::toString)
						.ifPresent(this.out::set);
			}
		);
	}

	public void setProperty(String propertyName, Object value) throws Exception
	{
		this.checkIsWorking(() ->
			{
				if (value instanceof Serializable)
				{
					this.service().setProperty(propertyName, (Serializable) value);
				}
				else if (value == null)
				{
					this.service().setProperty(propertyName, null);
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
		this.checkIsWorking(() -> this.service().moveDialog(this.getSelfLocator(), x, y));
	}

	public void dialogResize(Resize resize, int h, int w) throws Exception
	{
		this.checkIsWorking(() -> this.service().resizeDialog(getSelfLocator(), resize, h, w));
	}

	public void dialogGetProperty(String propertyName) throws Exception
	{
		this.checkIsWorking(() ->
				{
					Locator selfLocator = getSelfLocator();

					String result = null;
					if (Str.areEqual(propertyName, DialogGetProperties.sizeName))
					{
						result = this.service().getDialogSize(selfLocator).toString();
					}
					if (Str.areEqual(propertyName, DialogGetProperties.positionName))
					{
						result = this.service().getDialogPosition(selfLocator).toString();
					}
					Optional.ofNullable(result).ifPresent(this.out::set);
				}
		);
	}
	//endregion

	//region private methods
	private Optional<OperationResult> operate(Operation operation, IWindow window, IControl control) throws Exception
	{
		this.service().startNewDialog();
		IControl owner = window.getOwnerControl(control);
		IControl rows = window.getRowsControl(control);
		IControl header = window.getHeaderControl(control);

		//TODO why we create copy?
		AbstractControl abstractControl = AbstractControl.createCopy(control, owner, rows, header);
		OperationResult result = abstractControl.operate(this.service(), window, operation);
		return Optional.of(result);
	}

	private Optional<CheckingLayoutResult> check(Spec spec, IWindow window, IControl control) throws Exception
	{
		IControl owner = window.getOwnerControl(control);
		IControl rows = window.getRowsControl(control);
		IControl header = window.getHeaderControl(control);

		//TODO why we create copy?
		AbstractControl abstractControl = AbstractControl.createCopy(control, owner, rows, header);
		CheckingLayoutResult result = abstractControl.checkLayout(this.service(), window, spec);
		return Optional.of(result);
	}

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

	private void storeSettings(Settings settings) throws Exception
	{
		String absolutePath = new File(getNameProperty().get()).getAbsolutePath();
		String idAppEntry = this.currentApp.get();
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

	private Locator getSelfLocator() throws Exception
	{
		return Optional.ofNullable(this.currentWindow.get())
				.map(IWindow::getSelfControl)
				.map(IControl::locator)
				.orElseThrow(() -> new Exception("Can't find self control for current window."));
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

	private void checkIsWorking(Common.Function a) throws Exception
	{
		if (this.isApplicationRun())
		{
			if (this.isWorking)
			{
				DialogsHelper.showInfo("Please wait until previous command will be complete");
			}
			else
			{
				this.isWorking = true;
				try
				{
					a.call();
				}
				finally
				{
					this.isWorking = false;
				}
			}
		}
	}

	private Object trimIfString(Object value)
	{
		if (value instanceof String)
		{
			return ((String)value).trim();
		}
		return value;
	}
	//endregion

	public class ControlWithState implements Mutable
	{
		private IControl                      control;
		private String                        text;
		private DictionaryFxController.Result result;

		public ControlWithState(IControl control, String text, DictionaryFxController.Result result)
		{
			this.control = control;
			this.text = text;
			this.result = result;
		}

		public IControl getControl()
		{
			return control;
		}

		public String getText()
		{
			return text;
		}

		public DictionaryFxController.Result getResult()
		{
			return result;
		}

		@Override
		public boolean isChanged()
		{
			return false;
		}

		@Override
		public void saved()
		{

		}
	}
}
