package tr.com.olives4j.sql.util;

import java.util.List;

public class SQLFormatter {
	public static final SQLFormatter INSTANCE = new SQLFormatter();

	/**
	 * 
	 * @return
	 */
	public StringBuilder format(String sql) {
		return format(sql, 0, null);
	}

	/**
	 * 
	 * @param sql
	 * @param indent
	 * @param params
	 * @param buffer
	 * @return
	 */
	public final StringBuilder format(CharSequence sql, int indent, final List<?> params) {
		return format(sql, indent, params, new StringBuilder(), false);
	}

	/**
	 * 
	 * @param sql
	 * @param indent
	 * @param params
	 * @param buffer
	 * @return
	 */
	public final StringBuilder format(CharSequence sql, int indent, List<?> params, StringBuilder buffer, boolean keepFormat) {
		StringBuilder lastWord = new StringBuilder();
		char NL = '\n';
		boolean lastiswhite = false;

		int baseIndent = indent;
		char[] chars = sql.toString().toCharArray();
		int valIndex = 0;
		boolean beforeWhere = true;
		boolean instring = false;
		int intSubQueryPad = 0;
		int groupingCount = 0;
		try {

			// buffer.append(NL);
			if (buffer.length() == 0 || buffer.charAt(buffer.length() - 1) == '\n') {
				for (int i = 0; i < indent; i++) {
					buffer.append(" ");
				}
			}
			for (int index = 0; index < chars.length; index++) {
				char c = chars[index];
				String sLastWord = lastWord.toString();

				if (!keepFormat) {
					if (sLastWord.equals("where") || sLastWord.equals("from") //
							|| sLastWord.equals("group") //
							|| sLastWord.toString().equals("set") //
							|| sLastWord.equals("having") //
							|| sLastWord.equals("order") //
							|| sLastWord.equals("left")//
							|| sLastWord.equals("inner")//
							|| sLastWord.equals("outer")) {
						buffer.insert(buffer.length() - sLastWord.length(), NL);
						for (int i2 = 0; i2 < intSubQueryPad + baseIndent; i2++) {
							buffer.insert(buffer.length() - sLastWord.length(), " ");
						}

						if (beforeWhere && sLastWord.equals("where")) {
							beforeWhere = false;
						}
					} else if (sLastWord.equals("and") && beforeWhere == false && !instring) {
						if (buffer.charAt(buffer.length() - sLastWord.length() - 1) != NL) {
							buffer.insert(buffer.length() - sLastWord.length(), NL);
							for (int i2 = 0; i2 < intSubQueryPad + (baseIndent * 2); i2++) {
								buffer.insert(buffer.length() - sLastWord.length(), " ");
							}
						}
					}
				}

				if ((c == '"' || c == '\'') && (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != '\\')) {
					instring = !instring;
				}

				if (!instring && sLastWord.equals("select")) {
					boolean subQuery = false;
					int tmpi2 = 0;
					for (int tmpi = buffer.length() - 1; tmpi >= 0; tmpi--) {
						char tmpch = buffer.charAt(tmpi);
						tmpi2++;
						if (tmpch == '(') {
							intSubQueryPad = 0;
							subQuery = true;
						} else if (subQuery && tmpch == '\n') {
							groupingCount = 1;
							intSubQueryPad = tmpi2 - 7;
							break;
						}
					}
				}

				if (c == '(') {
					groupingCount++;
				} else if (c == ')') {
					groupingCount--;
				}

				if (groupingCount == 0) {
					intSubQueryPad = 0;
				}

				if (c == '?' && !instring) {
					buffer.append(c);

					buffer.append(" /* ");
					if (params != null && valIndex < params.size()) {
						Object value = params.get(valIndex);
						String[] val = (value + "").split("[\r\n]+", 2);
						buffer.append(val[0]);
					}
					buffer.append("*/");
					valIndex++;

					if ((index + 3) < chars.length && chars[index + 1] == '.' && chars[index + 2] == '.' && chars[index + 3] == '.') {
						index += 3;
					}
				}
				if (!keepFormat) {
					if (Character.isWhitespace(c)) {
						if (!lastiswhite) {
							buffer.append(c);
						}
						lastiswhite = true;
					} else {
						buffer.append(c);
						lastiswhite = false;
						if (c == NL) {
							for (int i2 = 0; i2 < intSubQueryPad + 8; i2++) {
								buffer.append(" ");
							}
						}
					}
				} else {
					buffer.append(c);
				}

				lastWord.append(c);
				if (Character.isWhitespace(c) || c == '(' || c == ')') {
					lastWord.setLength(0);
				}
			}
			// buffer.append(sql.subSequence(j, sql.length()));
			return buffer;
		} catch (RuntimeException e) {
			buffer.setLength(0);
			buffer.append(sql);
			buffer.append("\nWITH VALUES:\n");
			buffer.append(params);
			buffer.append(" -- format failed with " + e);
			return buffer;
		}
	}
}
