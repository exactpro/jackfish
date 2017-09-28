package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.awt.MouseInfo;
import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ConsoleArea extends StyleClassedTextArea
{
    private ArrayList<Link> list;
    private int charIndex;

	public ConsoleArea()
    {
        createContextMenu();
    }

    public ConsoleArea(Consumer<TreeItem<MatrixItem>> moveToMatrixItem)
    {
        createContextMenu();
        this.list = new ArrayList<>();
        this.setMouseOverTextDelay(Duration.ofMillis(10));
        this.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> this.charIndex = e.getCharacterIndex());
        this.setOnMouseReleased(event ->
        {
            for (Link link : this.list)
            {
				if (this.charIndex > link.getStart() && this.charIndex < link.getEnd())
				{
					moveToMatrixItem.accept(link.getItem());
					break;
				}
			}
        });
    }

    private void createContextMenu()
    {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setAutoFix(true);
        contextMenu.setAutoHide(true);

        MenuItem itemClear = new MenuItem("Clear");
        itemClear.setOnAction(e -> this.clear());
        contextMenu.getItems().addAll(itemClear);

        this.setOnContextMenuRequested(e -> contextMenu.show(
                this.getScene().getWindow(),
                MouseInfo.getPointerInfo().getLocation().getX(),
                MouseInfo.getPointerInfo().getLocation().getY()));
    }

    public void appendDefaultText(String text)
    {
        this.appendStyledText(text, false, null, CssVariables.CONSOLE_DEFAULT_TEXT);
    }

    public void appendDefaultTextOnNewLine(String text)
    {
        this.appendStyledText(text, this.getText().length() > 0, null, CssVariables.CONSOLE_DEFAULT_TEXT);
    }

    public void appendErrorText(String text)
    {
        this.appendStyledText(text, false, null, CssVariables.CONSOLE_ERROR_ITEM);
    }

    public void appendErrorTextOnNewLine(String text)
    {
        this.appendStyledText(text, this.getText().length() > 0, null, CssVariables.CONSOLE_ERROR_ITEM);
    }

    public void appendMatrixItemLink(String text, TreeItem <MatrixItem> item)
    {
        this.appendStyledText(text, true, item, CssVariables.CONSOLE_PAUSED_ITEM);
    }

    private void appendStyledText(String text, boolean newLine, TreeItem <MatrixItem> item, String style)
    {
        int start = this.getLength();
        this.appendText(newLine ? "\n" + text : text);
        this.setStyleClass(start, this.getLength(), style);
        if (item != null)
        {
            this.list.add(new Link(start, this.getLength(), item));
        }
        if (this.totalHeightEstimateProperty().getValue() != null)
        {
            this.setEstimatedScrollY(this.getTotalHeightEstimate() - this.getHeight());
        }
    }

    private class Link
    {
        int start;
        int end;
        TreeItem <MatrixItem> item;

        private Link(int start, int end, TreeItem <MatrixItem> item) {
            this.start = start;
            this.end = end;
            this.item = item;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public TreeItem<MatrixItem> getItem() {
            return item;
        }
    }

    @Override
    public void clear()
    {
        if(this.list != null)
        {
            this.list.clear();
        }
        super.clear();
    }
}
