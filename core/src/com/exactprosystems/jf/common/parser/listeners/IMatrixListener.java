package com.exactprosystems.jf.common.parser.listeners;

import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.Result;
import com.exactprosystems.jf.common.parser.items.MatrixItem;

public interface IMatrixListener extends Cloneable
{
	void		reset				(Matrix matrix);
	void		matrixStarted		(Matrix matrix);
	void		matrixFinished		(Matrix matrix, int passed, int failed);
	void		started				(Matrix matrix, MatrixItem item);
	void		paused				(Matrix matrix, MatrixItem item);
	void		finished			(Matrix matrix, MatrixItem item, Result result);
	void		error				(Matrix matrix, int lineNumber, MatrixItem item, String message);
	IMatrixListener clone () throws CloneNotSupportedException;
	String		getExceptionMessage	();
	boolean		isOk				();

}
