package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import javafx.scene.control.TreeItem;

public class MatrixItemLink<S> {

    private final String originalDisplayedText;
    private final String displayedText;
    private final S style;
    private final TreeItem<MatrixItem> item;
    private final String link;

    MatrixItemLink(String originalDisplayedText, String displayedText, S style, TreeItem<MatrixItem> item) {
        this.originalDisplayedText = originalDisplayedText;
        this.displayedText = displayedText;
        this.style = style;
        this.item = item;
        this.link = item.getValue().getItemName();
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public boolean isReal() {
        return length() > 0;
    }

    public boolean shareSameAncestor(MatrixItemLink<S> other) {
        return link.equals(other.link) && originalDisplayedText.equals(other.originalDisplayedText);
    }

    public int length() {
        return displayedText.length();
    }

    public char charAt(int index) {
        return isEmpty() ? '\0' : displayedText.charAt(index);
    }

    public String getOriginalDisplayedText() { return originalDisplayedText; }

    public String getDisplayedText() {
        return displayedText;
    }

    public String getLink() {
        return link;
    }

    public TreeItem<MatrixItem> getItem() { return item; }

    public MatrixItemLink<S> subSequence(int start, int end) {
        return new MatrixItemLink<>(originalDisplayedText, displayedText.substring(start, end), style, item);
    }

    public MatrixItemLink<S> subSequence(int start) {
        return new MatrixItemLink<>(originalDisplayedText, displayedText.substring(start), style, item);
    }

    public S getStyle() {
        return style;
    }

    public MatrixItemLink<S> setStyle(S style) {
        return new MatrixItemLink<>(originalDisplayedText, displayedText, style, item);
    }

    public MatrixItemLink<S> mapDisplayedText(String text) {
        return new MatrixItemLink<>(originalDisplayedText, text, style, item);
    }

    @Override
    public String toString() {
        return isEmpty()
                ? String.format("EmptyHyperlink[original=%s style=%s link=%s]", originalDisplayedText, style, link)
                : String.format("RealHyperlink[original=%s displayedText=%s, style=%s, link=%s]",
                                    originalDisplayedText, displayedText, style, link);
    }

}
