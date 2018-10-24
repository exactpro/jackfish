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

package com.exactprosystems.jf.documents.matrix.parser.items.end;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemAttribute;
import com.exactprosystems.jf.documents.matrix.parser.items.RawMessage;

@MatrixItemAttribute(
		constantGeneralDescription = R.END_RAW_MESSAGE_DESCRIPTION,
		shouldContain 	= { Tokens.EndRawMessage },
		mayContain 		= { },
		closes			= RawMessage.class,
		real			= false,
		hasValue 		= false,
		hasParameters 	= false,
		hasChildren 	= false
)
public class EndRawMessage extends MatrixItem
{
	@Override
	protected MatrixItem makeCopy()
	{
		return new EndRawMessage();
	}
}
