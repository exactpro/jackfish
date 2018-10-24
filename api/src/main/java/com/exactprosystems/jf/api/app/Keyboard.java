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

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

public enum Keyboard implements Serializable
{
	ESCAPE(null), F1(null), F2(null), F3(null), F4(null), F5(null), F6(null), F7(null), F8(null), F9(null), F10(null), F11(null), F12(null),
	DIG1('1'), DIG2('2'), DIG3('3'), DIG4('4'), DIG5('5'), DIG6('6'), DIG7('7'), DIG8('8'), DIG9('9'), DIG0('0'), BACK_SPACE(null),               INSERT(null), HOME(null), PAGE_UP(null),
	TAB(null),		  Q('Q'), W('W'), E('E'), R('R'), T('T'), Y('Y'), U('U'), I('I'), O('O'), P('P'), SLASH('/'), BACK_SLASH('\\'),				  DELETE(null), END(null), PAGE_DOWN(null),
	CAPS_LOCK(null),   A('A'), S('S'), D('D'), F('F'), G('G'), H('H'), J('J'), K('K'), L('L'), SEMICOLON('|'), QUOTE('\''), DOUBLE_QUOTE('"'), ENTER(null),
	SHIFT(null),		Z('Z'), X('X'), C('C'), V('V'), B('B'), N('N'), M('M'),						DOT('.'),	 					UP(null),
	CONTROL(null),	ALT(null), SPACE(' '),														LEFT(null),DOWN(null),RIGHT(null),

	PLUS('+'), MINUS('-'),
	UNDERSCORE('_'),

	//numpad keys
	NUM_LOCK(null),
	NUM_DIVIDE('/'),
	NUM_SEPARATOR('/'),
	NUM_MULTIPLY('*'),
	NUM_MINUS('-'),
	NUM_DIG7('7'),
	NUM_DIG8('8'),
	NUM_DIG9('9'),
	NUM_PLUS('+'),
	NUM_DIG4('4'),
	NUM_DIG5('5'),
	NUM_DIG6('6'),
	NUM_DIG1('1'),
	NUM_DIG2('2'),
	NUM_DIG3('3'),
	NUM_DIG0('0'),
	NUM_DOT('.'),
	NUM_ENTER(null),
	;

	public static void main(String[] args)
	{
		for (Keyboard keyboard : Keyboard.values())
		{
			if (keyboard.name().startsWith("NUM"))
			{
				System.out.println("case \""+keyboard.name().toLowerCase()+"\" : return VirtualKeyCode.;");
			}
		}
	}

	private Keyboard(Character ch)
    {
        this.ch = ch;
    }

    public String getChar()
	{
		return this.ch == null ? "" : this.ch.toString();
	}
    
    static Keyboard byChar(char ch)
    {
        for (Keyboard keyboard : values())
        {
            if (keyboard.ch != null && keyboard.ch == ch)
            {
                return keyboard;
            }
        }
        return null;
    }
    
    private Character ch; 

	private static final long serialVersionUID = -1378685462384062328L;
}
