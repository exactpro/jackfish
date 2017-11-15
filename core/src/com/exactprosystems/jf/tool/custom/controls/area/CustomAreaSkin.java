////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.area;

import com.exactprosystems.jf.tool.CssVariables;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public abstract class CustomAreaSkin extends TextAreaSkin
{
	private static final PseudoClass HAS_RIGHT_NODE = PseudoClass.getPseudoClass("right-node-visible");

	private Node right;
	private StackPane rightPane;

	private final TextArea control;

	public CustomAreaSkin(final TextArea control) {
		super(control);

		this.control = control;
		updateChildren();

		registerChangeListener(rightProperty(), "RIGHT_NODE");
		registerChangeListener(control.focusedProperty(), "FOCUSED");
	}

	public abstract ObjectProperty<Node> rightProperty();

	@Override
	protected void handleControlPropertyChanged(String p) {
		super.handleControlPropertyChanged(p);
		if (Objects.equals(p, "RIGHT_NODE")) {
			updateChildren();
		}
	}

	private void updateChildren() {
		Node newRight = rightProperty().get();
		if (newRight != null) {
			getChildren().remove(rightPane);
			rightPane = new StackPane(newRight);
			rightPane.setAlignment(Pos.CENTER_LEFT);
			rightPane.getStyleClass().add(CssVariables.CUSTOM_AREA_RIGHT_PANE);
			getChildren().add(rightPane);
			right = newRight;
		}
		control.pseudoClassStateChanged(HAS_RIGHT_NODE, right != null);
	}

	@Override
	protected void layoutChildren(double x, double y, double w, double h) {
		final double fullHeight = h + snappedBottomInset();

		final double rightWidth = rightPane == null ? 0.0 : snapSize(rightPane.prefWidth(fullHeight));

		final double textFieldStartX = snapPosition(x) + snapSize(0.0);
		final double textFieldWidth = w - snapSize(0.0) - snapSize(rightWidth);

		super.layoutChildren(textFieldStartX, 0, textFieldWidth, fullHeight);

		if (rightPane != null) {
			final double rightStartX = w - rightWidth + snappedLeftInset();
			rightPane.resizeRelocate(rightStartX, 0, rightWidth, 20);
		}
	}

	@Override
	protected double computePrefWidth(double h, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double pw = super.computePrefWidth(h, topInset, rightInset, bottomInset, leftInset);
		final double rightWidth = rightPane == null ? 0.0 : snapSize(rightPane.prefWidth(h));

		return pw + rightWidth + leftInset + rightInset;
	}

	@Override
	protected double computePrefHeight(double w, double topInset, double rightInset, double bottomInset, double leftInset) {
		final double ph = super.computePrefHeight(w, topInset, rightInset, bottomInset, leftInset);
		final double rightHeight = rightPane == null ? 0.0 : snapSize(rightPane.prefHeight(-1));
		return Math.max(ph, rightHeight);
	}
}
