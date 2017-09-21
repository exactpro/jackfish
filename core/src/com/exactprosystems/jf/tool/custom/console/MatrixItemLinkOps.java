package com.exactprosystems.jf.tool.custom.console;

import org.fxmisc.richtext.model.SegmentOps;

import java.util.Optional;

public class MatrixItemLinkOps<S> implements SegmentOps<MatrixItemLink<S>, S>
{

    @Override
    public int length(MatrixItemLink<S> hyperlink) {
        return hyperlink.length();
    }

    @Override
    public char charAt(MatrixItemLink<S> hyperlink, int index) {
        return hyperlink.charAt(index);
    }

    @Override
    public String getText(MatrixItemLink<S> hyperlink) {
        return hyperlink.getDisplayedText();
    }

    @Override
    public MatrixItemLink<S> subSequence(MatrixItemLink<S> hyperlink, int start, int end) {
        return hyperlink.subSequence(start, end);
    }

    @Override
    public MatrixItemLink<S> subSequence(MatrixItemLink<S> hyperlink, int start) {
        return hyperlink.subSequence(start);
    }

    @Override
    public S getStyle(MatrixItemLink<S> hyperlink) {
        return hyperlink.getStyle();
    }

    @Override
    public MatrixItemLink<S> setStyle(MatrixItemLink<S> hyperlink, S style) {
        return hyperlink.setStyle(style);
    }

    @Override
    public Optional<MatrixItemLink<S>> join(MatrixItemLink<S> currentSeg, MatrixItemLink<S> nextSeg) {
        if (currentSeg.isEmpty()) {
            if (nextSeg.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(nextSeg);
            }
        } else {
            if (nextSeg.isEmpty()) {
                return Optional.of(currentSeg);
            } else {
                return concatHyperlinks(currentSeg, nextSeg);
            }
        }
    }

    private Optional<MatrixItemLink<S>> concatHyperlinks(MatrixItemLink<S> leftSeg, MatrixItemLink<S> rightSeg) {
        if (!leftSeg.shareSameAncestor(rightSeg)) {
            return Optional.empty();
        }

        String original = leftSeg.getOriginalDisplayedText();
        String leftText = leftSeg.getDisplayedText();
        String rightText = rightSeg.getDisplayedText();
        int leftOffset = 0;
        int rightOffset = 0;
        for (int i = 0; i <= original.length() - leftText.length(); i++) {
            if (original.regionMatches(i, leftText, 0, leftText.length())) {
                leftOffset = i;
                break;
            }
        }
        for (int i = 0; i <= original.length() - rightText.length(); i++) {
            if (original.regionMatches(i, rightText, 0, rightText.length())) {
                rightOffset = i;
                break;
            }
        }

        if (rightOffset + rightText.length() == leftOffset) {
            return Optional.of(leftSeg.mapDisplayedText(rightText + leftText));
        } else if (leftOffset + leftText.length() == rightOffset) {
            return Optional.of(leftSeg.mapDisplayedText(leftText + rightText));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public MatrixItemLink<S> createEmpty() {
        return new MatrixItemLink<>("", "", null, null);
    }
}
