/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.controls.field;

import com.exactprosystems.jf.tool.CssVariables;
import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public abstract class CustomFieldSkin extends TextFieldSkin
{
	private static final PseudoClass HAS_RIGHT_NODE = PseudoClass.getPseudoClass("right-node-visible");

	private       Node      right;
	private       StackPane rightPane;
	private final TextField control;

	CustomFieldSkin(final TextField control)
	{
		super(control, new TextFieldBehavior(control));

		this.control = control;
		this.updateChildren();

		super.registerChangeListener(rightProperty(), "RIGHT_NODE");
		super.registerChangeListener(control.focusedProperty(), "FOCUSED");
	}

	public abstract ObjectProperty<Node> rightProperty();

	@Override
	protected void handleControlPropertyChanged(String p)
	{
		super.handleControlPropertyChanged(p);
		if (Objects.equals(p, "RIGHT_NODE"))
		{
			this.updateChildren();
		}
	}

	@Override
	protected void layoutChildren(double x, double y, double w, double h)
	{
		final double fullHeight = h + super.snappedTopInset() + super.snappedBottomInset();

		final double rightWidth = rightPane == null ? 0.0 : super.snapSize(this.rightPane.prefWidth(fullHeight));

		final double textFieldStartX = super.snapPosition(x) + super.snapSize(0.0);
		final double textFieldWidth = w - super.snapSize(0.0) - super.snapSize(rightWidth);

		super.layoutChildren(textFieldStartX, 0, textFieldWidth, fullHeight);

		if (this.rightPane != null)
		{
			final double rightStartX = w - rightWidth + super.snappedLeftInset();
			this.rightPane.resizeRelocate(rightStartX, 0, rightWidth, fullHeight);
		}
	}

	@Override
	protected double computePrefWidth(double h, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		final double pw = super.computePrefWidth(h, topInset, rightInset, bottomInset, leftInset);
		final double rightWidth = this.rightPane == null ? 0.0 : super.snapSize(this.rightPane.prefWidth(h));

		return pw + rightWidth + leftInset + rightInset;
	}

	@Override
	protected double computePrefHeight(double w, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		final double ph = super.computePrefHeight(w, topInset, rightInset, bottomInset, leftInset);
		final double rightHeight = this.rightPane == null ? 0.0 : super.snapSize(this.rightPane.prefHeight(-1));
		return Math.max(ph, rightHeight);
	}

	private void updateChildren()
	{
		Node newRight = rightProperty().get();
		if (newRight != null)
		{
			super.getChildren().remove(this.rightPane);
			this.rightPane = new StackPane(newRight);
			this.rightPane.setAlignment(Pos.CENTER_RIGHT);
			this.rightPane.getStyleClass().add(CssVariables.CUSTOM_FIELD_RIGHT_PANE);
			super.getChildren().add(this.rightPane);
			this.right = newRight;
		}
		control.pseudoClassStateChanged(HAS_RIGHT_NODE, this.right != null);
	}
}