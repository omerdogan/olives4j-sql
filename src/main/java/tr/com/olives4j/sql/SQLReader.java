/*******************************************************************************
 *   Copyright (c) 2016, Omer Dogan.  All rights reserved.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *    
 *******************************************************************************/
package tr.com.olives4j.sql;

import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tr.com.olives4j.sql.util.CharSequenceReader;
import tr.com.olives4j.stree.StreeAnnotation;

/**
 * Parse sql text into SQL types
 * 
 * @author omer.dogan
 * 
 */
public class SQLReader {
	private enum SYNTAX {
		NA, NEW, START, INLINE_COMMENT, BLOCK_COMMENT_START, BLOCK_COMMENT, STRING, HINT, ANNOTATION;

		@SuppressWarnings("unused")
		public boolean isBlockComment() {
			return this == BLOCK_COMMENT_START || this == BLOCK_COMMENT || this == ANNOTATION || this == HINT;
		}
	}

	/*
	 * @param content
	 * @return List of query strings
	 */
	public static SQLCollection read(CharSequence content) {
		CharSequenceReader reader = new CharSequenceReader(content);
		return read(reader, null);
	}

	/*
	 * @param content
	 * @param options
	 * @return List of query strings
	 */
	public static SQLCollection read(CharSequence content, Options options) {
		return read(new CharSequenceReader(content), options);
	}

	/*
	 * 
	 * @return SQL collection type as a result of parsing given reader content
	 */
	public static SQLCollection read(Reader br) {
		return read(br, null);
	}

	/*
	 * 
	 * @return SQL collection type as a result of parsing given reader content with the given options
	 */
	public static SQLCollection read(Reader reader, Options options) {
		if (options == null) {
			options = new Options();
		}
		StringBuilder buffer = new StringBuilder();
		StringBuilder lastWord = new StringBuilder();
		String prevWord = "";

		SYNTAX currentTag = SYNTAX.NEW;
		List<SQL> sqls = new ArrayList<SQL>();

		CachedReader creader = new CachedReader(reader, Math.max(options.bufferSize, 32), 4);

		boolean isWhiteSpace = false;
		boolean isWordSeparator = false;
		boolean isPlsqlBlock = false;
		boolean isNewLine = false;

		int paramIndex = 0;
		int lastCommentStart = -1;
		int plsqlBeginDept = 0;
		boolean wasPlsqlBlock = false;
		int i = -1;
		char c = '\0', CL1 = '\0', CR1 = '\0', CR2 = '\0';

		SQL.Options sqlOptions = new SQL.Options();
		if (options.disableFormat) {
			sqlOptions.keepFormat = true;
		}

		SQL sql = new SQL(sqlOptions);

		boolean isSqlStarted = false;

		String plsqlEndName = "";
		boolean plsqlStartWord = false;

		boolean exit = false;
		try {
			while (true) {
				if (creader.hasNext()) {
					c = creader.read();
				} else {
					break;
				}

				CL1 = creader.prev(1);
				CR1 = creader.next(1);
				CR2 = creader.next(2);

				// // to trace each step
				// System.out.println(new
				// String(creader.chars).replaceAll("[\0\\n\\r]", "_") + " ::: "
				// + c + " :: "
				// + creader.pos);

				isWhiteSpace = Character.isWhitespace(c) || c == '\0';
				isWordSeparator = isWhiteSpace || match(c, options.wordSeperators);
				isNewLine = isWhiteSpace && (c == '\r' || c == '\n');

				// if in a hint block
				if (currentTag == SYNTAX.HINT) {
					// emits all comment block
					do {
						buffer.append(c);
						if ((creader.prev(1) == '*' && c == '/') && (buffer.length() - lastCommentStart) > 3) {
							break;
						}
						c = creader.read();
					} while (creader.hasNext());

					// if annotation not ended properly
					if (!(creader.prev(1) == '*' && c == '/')) {
						continue;
					}

					if (!options.keepComments) {
						buffer.setLength(lastCommentStart);
					}
					currentTag = SYNTAX.NA;
					continue;
				}
				// if in a trigger block
				else if (currentTag == SYNTAX.ANNOTATION) {
					// emits all comment block
					do {
						buffer.append(c);
						if ((creader.prev(1) == '*' && c == '/') && (buffer.length() - lastCommentStart) > 3) {
							break;
						}
						c = creader.read();
					} while (creader.hasNext());

					// if annotation not ended properly
					if (!(creader.prev(1) == '*' && c == '/')) {
						continue;
					}

					// if end of the trigger block
					processAnnotation(sql, isSqlStarted, buffer, lastCommentStart + 2, buffer.length() - 2, options);

					currentTag = SYNTAX.NA;
					continue;
				}
				// if start of a block comment (/* comment */),hint (/*+..*/),
				// or trigger(/*@...*/)
				else if (c == '/' && CR1 == '*') {
					// holds the start position of the comment block
					lastCommentStart = buffer.length();

					// if start of a hint block
					if (CR2 == '+') {
						currentTag = SYNTAX.HINT;
						buffer.append('/');
						continue;
					}
					// if start of a trigger block
					else if (CR2 == '@') {
						currentTag = SYNTAX.ANNOTATION;
						buffer.append('/');
						continue;
					}

					currentTag = SYNTAX.BLOCK_COMMENT;

					// emits all comment block
					do {
						if (options.keepComments) {
							buffer.append(c);
						}
						if ((creader.prev(1) == '*' && c == '/')) {
							break;
						}
						c = creader.read();
					} while (creader.hasNext());

					if (!(creader.prev(1) == '*' && c == '/')) {
						continue;
					}

					currentTag = SYNTAX.NA;
					continue;
				}
				// if start of inline comment
				else if (c == '-' && CR1 == '-') {
					lastCommentStart = buffer.length();

					boolean annotation = CR2 == '@';
					currentTag = SYNTAX.INLINE_COMMENT;
					do {
						if (options.keepComments || annotation) {
							buffer.append(c);
						}
						c = creader.read();
					} while (creader.hasNext() && (c != '\n'));

					if (annotation) {
						processAnnotation(sql, isSqlStarted, buffer, lastCommentStart + 2, buffer.length(), options);
					}

					currentTag = SYNTAX.NA;
					continue;
				}
				// if start of a string "....."
				else if ((c == '"' || c == '\'') && CL1 != '\\') {
					currentTag = SYNTAX.STRING;
					do {
						buffer.append(c);
						c = creader.read();
					} while (creader.hasNext() && !(c == '"' || c == '\''));
					buffer.append(c);
					currentTag = SYNTAX.NA;
					continue;
				}
				// if plsql separator
				else if (c == '/' && currentTag == SYNTAX.NEW) {
					// emit the character
					continue;
				}
				// this is the first character meaningful so sql block started
				else if (!isWhiteSpace && currentTag == SYNTAX.NEW) {
					currentTag = SYNTAX.START;
				}

				//
				if ((isWordSeparator) && lastWord.length() > 0) {
					// Handle inline/normal parameters
					final char lastWordStart = lastWord.charAt(0);
					String lastWordStr = lastWord.toString();

					if (lastWordStart == ':' && isJavaIdentifier(lastWordStr.substring(1))) {
						String paramName = lastWordStr.substring(1);
						buffer.setLength(buffer.length() - lastWord.length());
						sql.append(buffer);
						buffer.setLength(0);
						sql.append(new SQLBindNode().name(paramName));
						paramIndex++;
					} else if (lastWordStart == '$') {
						if (options.params != null && Character.isJavaIdentifierStart(lastWordStart)) {
							replaceInlineParam(options, buffer, lastWord);
						}
					}

					// Handle plsql block
					String lastWordUpr = lastWordStr.toUpperCase();
					prevWord = prevWord.toUpperCase();
					if (isPlsqlBlock) {
						if (lastWordUpr.equals("BEGIN") || lastWordUpr.equals("BEGİN")) {
							if (plsqlEndName.isEmpty()) {
								plsqlBeginDept++;
							}
						} else if (c == ';' && "END".equals(lastWordUpr) && plsqlEndName.isEmpty()) {
							plsqlBeginDept--;
							isPlsqlBlock = plsqlBeginDept > 0;
						} else if (c == ';' && "END".equals(prevWord) && lastWordUpr.equals(plsqlEndName)) {
							plsqlBeginDept--;
							isPlsqlBlock = plsqlBeginDept > 0;
						}
					} else if (plsqlStartWord == false) {
						if (lastWordUpr.equals("CREATE")) {
							plsqlStartWord = true;
						} else if (lastWordUpr.equals("BEGIN") || lastWordUpr.equals("BEGİN")) {
							isPlsqlBlock = true;
							plsqlBeginDept++;
						} else if (lastWordUpr.equals("DECLARE")) {
							isPlsqlBlock = true;
						}
						if (isPlsqlBlock) {
							wasPlsqlBlock = true;
						}
					} else if (plsqlStartWord == true) {
						if (options.plsqlStart != null) {
							Matcher matcher = options.plsqlStart.matcher(buffer);
							if (matcher.find()) {
								plsqlEndName = matcher.group(matcher.groupCount());
								if (plsqlEndName != null) {
									isPlsqlBlock = true;
									plsqlBeginDept++;
								}
							}
						}

						if (isPlsqlBlock) {
							wasPlsqlBlock = true;
						}
					}

					prevWord = lastWord.toString();
					lastWord.setLength(0);
				}

				if (!isPlsqlBlock && (c == ';' || c == '\0')) {
					currentTag = SYNTAX.NEW;

					if (wasPlsqlBlock) {
						buffer.append(';');
						wasPlsqlBlock = false;
					}
					if (buffer.length() > 0) {
						sql.append(buffer);
					}

					String sqlcontent = sql.toString().trim();
					if (!sqlcontent.isEmpty()) {
						sqls.add(sql);
					}

					buffer.setLength(0);
					plsqlBeginDept = 0;
					paramIndex = 0;
					prevWord = "";
					plsqlEndName = "";
					plsqlStartWord = false;
					sql = new SQL();
				} else {
					if (!isWordSeparator && c != '\0') {
						lastWord.append(c);
					}
					buffer.append(c);

					if (isNewLine && buffer.length() > 1) {
						sql.append(buffer);
						buffer.setLength(0);
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Could not parse query! Invalid index:" + i, e);
		} catch (Exception e) {
			throw new RuntimeException("Could not parse query!", e);
		}

		if (sqls.size() == 0 && sql.toString().trim().length() > 0) {
			sqls.add(sql);
		}

		return new SQLCollection(sqls);
	}

	/**
	 * 
	 * @param sql
	 * @param isBeforeSql
	 * @param buffer
	 * @param start
	 * @param end
	 */
	private static void processAnnotation(SQL sql, boolean isBeforeSql, StringBuilder buffer, int start, int end, Options options) {
		String expression = buffer.substring(start, end);
		

		String[] exprargs = expression.split("[ @\r\t\n]", 3);
		String triggerName = exprargs[1];
		triggerName = triggerName.toLowerCase();
		if (isJavaIdentifier(triggerName)) {
			sql.append(new StreeAnnotation(expression));

			if ("named".equals(triggerName)) {
				String aname = readJavaIdentifier(expression.toCharArray(), 6);
				sql.name(aname);
			}
		} else if (triggerName.startsWith(":")) {

			String paramName = triggerName.substring(1);
			SQLBindNode bind = new SQLBindNode().name(paramName);

			String[] params = exprargs.length < 3 ? new String[] {} : exprargs[2].split("[,]");
			for (int j = 0; j < params.length; j++) {
				String next = params[j].trim();
				if (next.equals("optional")) {
					bind.optional();
				} else if (next.startsWith("default")) {
					try {
						String val = next.substring(next.indexOf(":"));
						bind.defaultValue(val);
					} catch (Exception e) {
						throw new RuntimeException("Parse failed reading binding annotation " + paramName + " parameter: " + next, e);
					}
				}
			}

			if (!options.keepComments) {
				buffer.setLength(start - 2);
			}
			
			int bufferLength = buffer.length();
			int i = bufferLength - 1;
			for (; i >= 0; i--) {
				char ch = buffer.charAt(i);
				boolean isWhiteSpace = Character.isWhitespace(ch);
				if (!isWhiteSpace) {
					break;
				}
			}

			int last = i;
			int first = i;

			Object defaultValue = null;
			if (i > 0) {
				char ch = buffer.charAt(i);
				boolean instring = ch == '"' || ch == '\'';
				boolean ingroup = ch == ')';
				if (instring) {
					i--;
					for (; i >= 0; i--) {
						ch = buffer.charAt(i);

						boolean endstring = ch == '"' || ch == '\'';
						if (endstring && (i == 0 || buffer.charAt(i - 1) != '\\')) {
							defaultValue = buffer.substring(i, last + 1);
							first = i;
							break;
						}
					}
				} else if (ingroup) {
					i--;
					int deep = 1;
					for (; i >= 0; i--) {
						ch = buffer.charAt(i);

						instring = ch == '"' || ch == '\'';
						if (instring) {
							for (; i >= 0; i--) {
								ch = buffer.charAt(i);
								boolean endstring = ch == '"' || ch == '\'';
								if (endstring && (i == 0 || buffer.charAt(i - 1) != '\\')) {
									break;
								}
							}
						}

						if (ch == ')') {
							deep++;
						} else if (ch == '(') {
							deep--;
							if (deep == 0) {
								defaultValue = buffer.substring(i, last + 1);
								first = i;
								break;
							}
						}
					}
				} else {
					for (; i >= 0; i--) {
						ch = buffer.charAt(i);
						boolean isWhiteSpace = Character.isWhitespace(ch);
						if (isWhiteSpace) {
							defaultValue = buffer.substring(i + 1, last + 1);

							try {
								if (Character.isDigit(ch)) {
									defaultValue = NumberFormat.getInstance().parse((String) defaultValue);
								} else if (defaultValue.toString().equalsIgnoreCase("true") || defaultValue.toString().equalsIgnoreCase("false")) {
									defaultValue = Boolean.valueOf(defaultValue.toString());
								}
							} catch (Exception e) {
								throw new RuntimeException("Parse failed reading binding annotation " + paramName + " buffer: " + buffer, e);
							}
							first = i;
							break;
						}
					}
				}
			}

			if (defaultValue != null) {
				bind.defaultValue(defaultValue);
				buffer.setLength(first);
			}

			sql.append(buffer);
			buffer.setLength(0);
			sql.append(bind);
			sql.append("\r\n");
		}
	}

	/**
	 * 
	 * @param c
	 * @param isWhiteSpace
	 * @param seperators
	 * @return
	 */
	private static boolean match(char c, char[] seperators) {
		for (int si = 0; si < seperators.length; si++) {
			if (c == seperators[si]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param options
	 * @param buffer
	 * @param lastWord
	 * @return
	 */
	private static boolean replaceInlineParam(Options options, StringBuilder buffer, StringBuilder lastWord) {
		if (lastWord.charAt(0) != '$') {
			return false;
		}
		String key = lastWord.subSequence(1, lastWord.length()).toString();
		Object value = options.params.get(key);
		options.parameterCount++;
		if (value != null && value.toString().trim().length() > 0) {
			int start = buffer.length() - lastWord.length();
			buffer.replace(start, buffer.length(), value.toString());
			return true;
		} else {
			options.missedParameters.add(key);
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	private final static String readJavaIdentifier(char[] chars, int start) {
		int end = start;
		int ch = chars[start];

		boolean found = false;

		end = start;
		for (; end < chars.length; end++) {
			ch = chars[end];
			if (Character.isWhitespace(ch)) {
				continue;
			}
			if (!found) {
				if (!Character.isJavaIdentifierStart(ch)) {
					return null;
				}
				start = end;
				found = true;
			}

			if (!Character.isJavaIdentifierPart(ch)) {
				return null;
			}
		}

		return new String(chars, start, end - start);
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	private final static boolean isJavaIdentifier(String s) {
		// an empty or null string cannot be a valid identifier
		if (s == null || s.length() == 0) {
			return false;
		}

		char[] c = s.toCharArray();
		if (!Character.isJavaIdentifierStart(c[0])) {
			return false;
		}

		for (int i = 1; i < c.length; i++) {
			if (!Character.isJavaIdentifierPart(c[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * A Wrapper for Readers.
	 * 
	 * <pre>
	 * Use a cache area to hold previous and next available characters and
	 * guarantee access to maximum  number of characters as {@link #cacheSize} before and after the current reading position.
	 * </pre>
	 * 
	 * @author omer.dogan
	 * 
	 */
	private static final class CachedReader {
		Reader r;
		char[] chars;
		char[] prev;
		int cacheSize;
		int pos;
		boolean isInitialized;
		int lastLength;
		boolean noMoreRead;

		/**
		 * 
		 * @param reader
		 * @param bufferSize
		 * @param cacheLength
		 */
		public CachedReader(Reader reader, int bufferSize, int maxLookUpSize) {
			super();
			this.r = reader;
			this.chars = new char[bufferSize + cacheSize];
			this.prev = new char[maxLookUpSize];
			this.cacheSize = maxLookUpSize;
			isInitialized = false;
			noMoreRead = false;
		}

		/**
		 * @throws IOException
		 * 
		 */
		public void init() throws IOException {
			lastLength = r.read(chars, 0, chars.length);
			noMoreRead = lastLength < chars.length;
			isInitialized = true;
			pos = -1;
		}

		/**
		 * 
		 * @param i
		 * @return
		 * @throws IOException
		 */
		public char prev(int i) throws IOException {
			if (i > cacheSize) {
				throw new IndexOutOfBoundsException(
						"Index is out of the cache bound! Cache Size:" + cacheSize + ", pos:" + i + ", available character size:" + lastLength);
			}
			if (!isInitialized) {
				init();
			}

			int bufferSize = chars.length - cacheSize;
			int index = (pos % bufferSize) - i;
			if (index < 0) {
				return prev[prev.length + index];
			}

			return chars[index];
		}

		/**
		 * 
		 * @param i
		 * @return
		 * @throws IOException
		 */
		public char next(int i) throws IOException {
			if (i > cacheSize) {
				throw new IndexOutOfBoundsException(
						"Index is out of the cache bound! Cache Size:" + cacheSize + ", pos:" + i + ", available character size:" + lastLength);
			}
			if (!isInitialized) {
				init();
			}
			int bufferSize = chars.length - cacheSize;
			int index = (pos % bufferSize) + i;

			if (noMoreRead && index >= lastLength) {
				return '\0';
			} else if (index >= chars.length) {
				throw new IndexOutOfBoundsException(
						"Index is out of the cache bound! Cache Size:" + cacheSize + ", pos:" + index + ", available character size:" + chars.length);
			}

			return chars[index];
		}

		/**
		 * 
		 * @return
		 * @throws IOException
		 */
		public boolean hasNext() throws IOException {
			if (noMoreRead) {
				int readMaxLength = chars.length - cacheSize;
				int index = (pos % readMaxLength);
				return index < lastLength;
			}
			return true;
		}

		/**
		 * Utility method to iterate through the given reader. This method read
		 * character from reader chunk by chunk into the chars array as needed.
		 * This method cache the given number of characters in chars array
		 * 
		 * 
		 * @return
		 * @throws IOException
		 */
		private final char read() throws IOException {
			if (!isInitialized) {
				init();
			}

			++pos;
			int bufferSize = chars.length - cacheSize;
			int index = (pos % bufferSize);

			if (noMoreRead && index > lastLength) {
				return '\0';
			}

			if (index == 0 && pos > 0) {
				System.arraycopy(chars, bufferSize, chars, 0, cacheSize);
				System.arraycopy(chars, chars.length - cacheSize * 2, prev, 0, cacheSize);
				lastLength = r.read(chars, cacheSize, bufferSize) + cacheSize;
				noMoreRead = lastLength < bufferSize;
			}

			return chars[index];
		}
	}

	/**
	 * SQLReader options
	 */
	public static class Options {
		public int parameterCount;
		public int replacedParameterCount;
		public Set<String> missedParameters = new LinkedHashSet<String>();
		public Map<String, Object> params;
		public boolean keepComments = false;
		public boolean keepNewLines = true;
		public int bufferSize = 1024;
		public boolean disableFormat = true;
		public char[] wordSeperators = new char[] { ';', ',', '(', ')', '<', '>', '+', '-', '/', '*', '=', '|' };
		public Pattern plsqlStart = Pattern.compile(
				"CREATE\\s+(OR\\s+REPLACE)?\\s+(FUNCTION|PROCEDURE|PACKAGE(\\s+BODY)?+)\\s+([a-zA-Z_0-9]+\\.)*([a-zA-Z_0-9]+)$", Pattern.CASE_INSENSITIVE);

		/**
		 * Constructor
		 */
		public Options() {
			super();
		}

		/**
		 * 
		 * @param params
		 */
		public Options(Map<String, Object> params) {
			super();
			this.params = params;
		}

		/**
		 * 
		 * @param params
		 * @param keepComments
		 */
		public Options(Map<String, Object> params, boolean keepComments) {
			super();
			this.params = params;
			this.keepComments = keepComments;
		}

		/**
		 * 
		 * @return
		 */
		public boolean hasMissedParameters() {
			return missedParameters.size() > 0;
		}
	}
}
