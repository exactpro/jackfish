////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.ISection;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.documents.config.AppEntry;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@WizardAttribute(
		name            	= R.SUPPORTED_CONTROLS_WIZARD_NAME,
		pictureName         = "SupportWizard.jpg",
		category            = WizardCategory.GUI_DICTIONARY,
		shortDescription    = R.SUPPORTED_CONTROL_WIZARD_SHORT_DESCRIPTION,
		experimental 		= false,
		strongCriteries     = true,
		criteries           = { ApplicationPool.class, AppEntry.class },
		detailedDescription = R.SUPPORTED_CONTROL_WIZARD_DETAILED_DESCRIPTION
)
public class SupportedControlWizard extends AbstractWizard
{
	private ApplicationPool applicationPool;
	private AppEntry appEntry;

	private Set<ControlKind> supportedControls;
	private GuiDictionary    dictionary;
	private TreeView<SimpleBean> treeView;

	@Override
	public void init(Context context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);

		this.applicationPool = super.get(ApplicationPool.class, parameters);
		this.appEntry = super.get(AppEntry.class, parameters);
	}

	@Override
	public boolean beforeRun()
	{
		try
		{
			this.supportedControls = this.applicationPool.supportedControlKinds(this.appEntry.toString());
			this.dictionary = this.applicationPool.getDictionary(this.appEntry);
		}
		catch (Exception e)
		{
			DialogsHelper.showError("" + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		borderPane.setPrefSize(600.0, 600.0);
		TreeView<SimpleBean> treeView = createTreeView();

		borderPane.setCenter(treeView);
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		return () ->
		{
			CommandBuilder builder = CommandBuilder.start();

			TreeItem<SimpleBean> rootItem = this.treeView.getRoot();
			if (rootItem.getChildren().isEmpty())
			{
				return builder.build();
			}

			for (TreeItem<SimpleBean> windowTreeItem : rootItem.getChildren())
			{
				String windowName = windowTreeItem.getValue().name;
				IWindow window = this.dictionary.getWindow(windowName);

				for (TreeItem<SimpleBean> sectionTreeItem : windowTreeItem.getChildren())
				{
					String sectionName = sectionTreeItem.getValue().name;
					ISection section = window.getSection(IWindow.SectionKind.valueOf(sectionName));

					for (TreeItem<SimpleBean> controlTreeItem : sectionTreeItem.getChildren())
					{
						SimpleBean controlSimpleBean = controlTreeItem.getValue();
						String controlName = controlSimpleBean.name;
						IControl oldControl;
						if (Str.IsNullOrEmpty(controlName))
						{
							oldControl = ((Section) section).getByIndex(controlSimpleBean.index);
						}
						else
						{
							 oldControl = section.getControlById(controlName);
						}

						IControl copyControl = Common.tryCatch(() -> AbstractControl.createCopy(oldControl, controlSimpleBean.value)
								, R.WIZARD_ERROR_ON_CREATE_COPY.get()
								, oldControl
						);
						builder.replaceControl(((Section) section), oldControl, copyControl);
					}
				}
			}
			if (!builder.isEmpty())
			{
				builder.saveDocument(this.dictionary);
			}

			return builder.build();
		};
	}

	private TreeView<SimpleBean> createTreeView()
	{
		this.treeView = new TreeView<>();
		this.treeView.setCellFactory(param -> new TreeCell<SimpleBean>(){
			@Override
			protected void updateItem(SimpleBean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					HBox box = new HBox();
					box.setAlignment(Pos.CENTER_LEFT);
					box.setSpacing(4);
					box.getChildren().add(new Label(item.name));
					if (item.value != null)
					{
						ComboBox<ControlKind> comboBox = new ComboBox<>();
						comboBox.getItems().setAll(supportedControls);
						box.getChildren().add(new Label(MessageFormat.format(R.WIZARD_OLD_VALUE_1.get(), item.value)));
						box.getChildren().add(new Label(R.WIZARD_SELECT_NEW_VALUE.get()));
						box.getChildren().add(comboBox);
						comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
							if (newValue != null)
							{
								item.value = newValue;
							}
						});
						comboBox.getSelectionModel().select(ControlKind.Any);
					}
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		TreeItem<SimpleBean> root = new TreeItem<>(new SimpleBean(MessageFormat.format(R.WIZARD_DICTIONARY_NAME_1.get(), this.dictionary.getNameProperty())));
		root.setExpanded(true);
		this.treeView.setRoot(root);

		this.dictionary.getWindows().forEach(window -> {
			TreeItem<SimpleBean> windowBean = new TreeItem<>(new SimpleBean(window.getName()));
			boolean needAddWindow = false;

			for (IWindow.SectionKind sectionKind : IWindow.SectionKind.values())
			{
				if (sectionKind == IWindow.SectionKind.Any)
				{
					continue;
				}
				TreeItem<SimpleBean> sectionBean = new TreeItem<>(new SimpleBean(sectionKind.name()));
				boolean needAddSection = false;

				ISection section = window.getSection(sectionKind);
				Collection<IControl> controls = section.getControls();
				for (IControl control : controls)
				{
					if (!this.supportedControls.contains(control.getBindedClass()))
					{
						needAddSection = true;
						SimpleBean itemBean = new SimpleBean(control.getID(), control.getBindedClass());
						if (Str.IsNullOrEmpty(control.getID()))
						{
							itemBean.index = ((Section) section).indexOf(((AbstractControl) control));
						}
						TreeItem<SimpleBean> controlBean = new TreeItem<>(itemBean);
						sectionBean.getChildren().add(controlBean);
					}
				}

				if (needAddSection)
				{
					needAddWindow = true;
					sectionBean.setExpanded(true);
					windowBean.getChildren().add(sectionBean);
				}
			}

			if (needAddWindow)
			{
				windowBean.setExpanded(true);
				root.getChildren().add(windowBean);
			}
		});

		if (root.getChildren().isEmpty())
		{
			root.getValue().name = R.WIZARD_DICTIONARY_OK.get();
		}
		return this.treeView;
	}

	private class SimpleBean
	{
		String name;
		ControlKind value;

		int index = -1;

		SimpleBean(String name)
		{
			this.name = name;
		}

		SimpleBean(String name, ControlKind value)
		{
			this.name = name;
			this.value = value;
		}

		public String getName()
		{
			return name;
		}
	}
}
