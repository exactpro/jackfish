////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.exactprosystems.jf.api.app.DoSpec;
import com.exactprosystems.jf.api.app.PieceKind;
import com.exactprosystems.jf.api.app.Range;

public class FormulaParser
{
	public static List<FormulaPart> parse(String str)
	{
		AtomicInteger levelCounter = new AtomicInteger(0);
		
		Tokens tokens = new Tokens(str); 
		List<FormulaPart> pieces = new ArrayList<FormulaPart>();
		States state = States.BEGIN;
		FormulaPart current = null;
		
		Iterator<String> iter = tokens.iterator();
		while (iter.hasNext())
		{
			String token = iter.next();
			
			if (token.trim().isEmpty())
			{
				continue;
			}
			switch (state)
			{
				case BEGIN:
					state = nextState(States.DOT, token, t -> t.equals(DoSpec.class.getSimpleName()));
					break;
					
				case DOT:
					state = nextState(States.FUNC, token, t -> t.equals("."));
					break;

				case FUNC:
					PieceKind kind = PieceKind.findByName(token); 
					if (kind != null)
					{
						current = new FormulaPart(kind);
						pieces.add(current);
						switch ("" + kind.useName() + "_" + kind.useRange())
						{
							case "true_true":	state = States.OPEN_PARAM_SN; 	break; 
							case "true_false":	state = States.OPEN_PARAM_S; 	break;
							case "false_true":	state = States.OPEN_PARAM_N; 	break;
							case "false_false":	state = States.OPEN_PARAM; 		break;
						}
					}
					else
					{
						state = States.ERROR;
					}
					break;
					
				case OPEN_PARAM:
					state = nextState(States.CLOSE_PARAM, token, t -> t.equals("("));
					break;
					
				case OPEN_PARAM_S:
					state = nextState(States.PARAM_S, token, t -> t.equals("("));
					break;

				case OPEN_PARAM_N:
					state = nextState(States.PARAM_N, token, t -> t.equals("("));
					break;

				case PARAM_S:
					state = readStringFormula(state, States.DOT, levelCounter, current, token, c -> c.addName(token));
					break;

				case PARAM_N:
					state = readRangeFormula(state, States.DOT, levelCounter, current, token, c -> c.addFirst(token));
					break;

				case OPEN_PARAM_SN:
					state = nextState(States.PARAM_SN_S, token, t -> t.equals("("));
					break;

				case PARAM_SN_S:
					state = readStringFormula(state, States.PARAM_SN_N, levelCounter, current, token, c -> c.addName(token));
					break;

				case PARAM_SN_N:
					state = readRangeFormula(state, States.DOT, levelCounter, current, token, c -> c.addFirst(token));
					break;

				case CLOSE_PARAM:
					state = nextState(States.DOT, token, t -> t.equals(")"));
					break;

				case OPEN_RANGE:
					if (token.equals("("))
					{
						state = current.getRange().hasTwoArguments() ? States.RANGE_FIRST : States.RANGE_ONLY_FIRST;
					}
					else
					{
						state = States.ERROR;
					}
					break;
					
				case RANGE_FIRST:
					state = readRangeFormula(state, States.RANGE_SECOND, levelCounter, current, token, c -> c.addFirst(token));
					break;

				case RANGE_SECOND:
					state = readRangeFormula(state, States.CLOSE_PARAM, levelCounter, current, token, c -> c.addSecond(token));
					break;

				case RANGE_ONLY_FIRST:
					state = readRangeFormula(state, States.CLOSE_PARAM, levelCounter, current, token, c -> c.addFirst(token));
					break;
					
				case ERROR:
					return null;
					
				case END: 
					return pieces;
			}
		}
		return pieces;
	}
	
	
	private enum States 
	{ 
		BEGIN, 
		DOT, 
		FUNC, 
		OPEN_PARAM, 
		OPEN_PARAM_S, 
		OPEN_PARAM_N, 
		OPEN_PARAM_SN, 
		PARAM_S, 
		PARAM_N, 
		PARAM_SN_S, 
		PARAM_SN_N, 
		CLOSE_PARAM, 
		OPEN_RANGE,
		RANGE_FIRST,
		RANGE_SECOND,
		RANGE_ONLY_FIRST,
		ERROR, 
		END 
	};

	private static States nextState(States nextState, String token, Predicate<String> pred)
	{
		if (pred.test(token))
		{
			return nextState;
		}
		return States.ERROR;
	}

	private static States readRangeFormula(States prevState, States nextState, AtomicInteger levelCounter, FormulaPart current, String token, Consumer<FormulaPart> cons)
	{
		switch (token)
		{
			case "(":
				levelCounter.incrementAndGet();
				cons.accept(current);
				break;

			case ")":
				if (levelCounter.intValue() > 0)
				{
					levelCounter.decrementAndGet();
					cons.accept(current);
				}
				else
				{
					levelCounter.set(0);
					return nextState;
				}
				break;
				
			case ",":
				if (levelCounter.intValue() > 0)
				{
					cons.accept(current);
				}
				else
				{
					levelCounter.set(0);
					return nextState;
				}
				break;
				
			default:
				Range range = Range.findByName(token); 
				if (range != null)
				{
					current.setRange(range);
					return States.OPEN_RANGE;
				}
				
				cons.accept(current);
		}

		return prevState;
	}

	private static States readStringFormula(States prevState, States nextState, AtomicInteger levelCounter, FormulaPart current, String token, Consumer<FormulaPart> cons)
	{
		switch (token)
		{
			case "'":
				if (levelCounter.intValue() == 0)
				{
					levelCounter.incrementAndGet();
				}
				else
				{
					levelCounter.decrementAndGet();
				}
				
				break;

			case ",":
			case ")":
				if (levelCounter.intValue() > 0)
				{
					cons.accept(current);
				}
				else
				{
					levelCounter.set(0);
					return nextState;
				}
				break;
				
			default:
				if (levelCounter.intValue() > 0)
				{
					cons.accept(current);
				}
				else
				{
					return States.ERROR;
				}
		}

		return prevState;
	}

	static class Tokens implements Iterable<String>
	{
		public Tokens(String init)
		{
			this.str = init;
		}
		
		@Override
		public Iterator<String> iterator()
		{
			return new InnerIterator();
		}
		
		private class InnerIterator implements Iterator<String>
		{
			public InnerIterator()
			{
				this.sb = new StringBuilder();
				this.position = 0;
			}

			@Override
			public boolean hasNext()
			{
				sb.delete(0, sb.length());
				if (this.position < str.length())
				{
					char ch = str.charAt(this.position++);
					sb.append(ch);
					Group group = Group.find(ch);
					
					while (this.position < str.length())
					{
						ch = str.charAt(this.position++);
						Group newGroup = Group.find(ch);
						if(newGroup != group || (newGroup != Group.DIGIT && newGroup != Group.WORD))
						{
							this.position--;
							break;
						}
						sb.append(ch);
					}
				}
				return this.sb.length() > 0;
			}

			@Override
			public String next()
			{
				return this.sb.toString();
			}

			private StringBuilder sb;
			private int position = 0;
		}
		
		private String str;

		private enum Group
		{
			EMPTY	(ch -> " \n\t".indexOf(ch) >= 0),
			PUNCT 	(ch -> ",;:{}()[]".indexOf(ch) >= 0),
			DIGIT 	(ch -> ".0123456789".indexOf(ch) >= 0),
			WORD 	(ch -> (ch >= 'a' && ch <= 'z') || (ch >= 'A' ) || '_' == ch || '-' == ch),
			OTHER 	(ch -> true);

			public static Group find(char ch)
			{
				for (Group group : values())
				{
					if (group.pred.test(ch))
					{
						return group;
					}
				}
				return OTHER;
			}
			
			private Group(Predicate<Character> pred)
			{
				this.pred = pred;
			}
			private Predicate<Character> pred;
		}
	}

}
