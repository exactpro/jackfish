////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.info.owner;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.get;

public class OwnerInfoController implements Initializable, ContainingParent
{
	public GridPane mainGrid;
	private Parent pane;

	public TextField tfOwnerAction;
	public TextField tfOwnerControl;
	public TextField tfOwnerUID;
	public TextField tfOwnerID;
	public TextField tfOwnerOperation;
	public TextField tfOwnerName;
	public TextField tfOwnerText;
	public TextField tfOwnerClass;
	public TextField tfOwnerTooltip;
	public TextField tfOwnerTitle;
	public TextField tfOwnerTimeout;
	public CheckBox checkBoxOwnerWeak;
	public TextField tfOwnerVisibility;
	public TextField tfOwnerAddition;
	public TextField tfOwnerXpath;

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfOwnerAction != null : "fx:id=\"tfOwnerAction\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerControl != null : "fx:id=\"tfOwnerControl\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerUID != null : "fx:id=\"tfOwnerUID\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerID != null : "fx:id=\"tfOwnerID\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerOperation != null : "fx:id=\"tfOwnerOperation\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerName != null : "fx:id=\"tfOwnerName\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerText != null : "fx:id=\"tfOwnerText\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerClass != null : "fx:id=\"tfOwnerClass\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerTooltip != null : "fx:id=\"tfOwnerTooltip\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerTitle != null : "fx:id=\"tfOwnerTitle\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerTimeout != null : "fx:id=\"tfOwnerTimeout\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert checkBoxOwnerWeak != null : "fx:id=\"checkBoxOwnerWeak\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerVisibility != null : "fx:id=\"tfOwnerVisibility\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerAddition != null : "fx:id=\"tfOwnerAddition\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		assert tfOwnerXpath != null : "fx:id=\"tfOwnerXpath\" was not injected: check your FXML file 'OwnerInfo.fxml'.";
		Platform.runLater(() -> ((BorderPane) this.pane).setCenter(BorderWrapper.wrap(this.mainGrid).color(Color.BLACK).title("Owner info").build()));
	}

	public void init(GridPane gridPane)
	{
		gridPane.add(this.pane, 0, 1);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayInfo(IControl owner)
	{
		Platform.runLater(() -> 
		{
			this.tfOwnerID.setText(get(owner, "", IControl::getID));
			this.tfOwnerUID.setText(get(owner, "", IControl::getUID));
			this.tfOwnerXpath.setText(get(owner, "", IControl::getXpath));
			this.tfOwnerClass.setText(get(owner, "", IControl::getClazz));
			this.tfOwnerName.setText(get(owner, "", IControl::getName));
			this.tfOwnerTitle.setText(get(owner, "", IControl::getTitle));
			this.tfOwnerAction.setText(get(owner, "", IControl::getAction));
			this.tfOwnerText.setText(get(owner, "", IControl::getText));
			this.tfOwnerTooltip.setText(get(owner, "", IControl::getTooltip));
			this.tfOwnerOperation.setText(get(owner, "", IControl::getExpression));
			this.tfOwnerControl.setText(get(owner, "", IControl::getBindedClass));
			this.tfOwnerAddition.setText(get(owner, "", IControl::getAddition));
			this.tfOwnerVisibility.setText(get(owner, "", IControl::getVisibility));
			this.tfOwnerTimeout.setText(get(owner, "0", IControl::getTimeout));
			this.checkBoxOwnerWeak.setSelected(owner == null ? false : owner.isWeak());
		} );
	}

	// ------------------------------------------------------------------------------------------------------------------
}
