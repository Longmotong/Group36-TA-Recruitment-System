package com.ebuko.moapp.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser/stringifier (no external deps).
 *
 * Supports: objects, arrays, strings, numbers, booleans, null.
 * Target usage: reading/writing the repository's structured JSON data.
 */
public final class MiniJson {
  private MiniJson() {}

  public static Object parse(String text) {
    try {
      return new Parser(new StringReader(text)).parseValue();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static Object parse(Reader reader) {
    try {
      return new Parser(reader).parseValue();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String stringify(Object value, boolean pretty) {
    StringBuilder sb = new StringBuilder();
    writeValue(sb, value, 0, pretty);
    return sb.toString();
  }

  private static void writeValue(StringBuilder sb, Object value, int indent, boolean pretty) {
    if (value == null) {
      sb.append("null");
      return;
    }
    if (value instanceof String s) {
      sb.append('"').append(escapeString(s)).append('"');
      return;
    }
    if (value instanceof Boolean b) {
      sb.append(b ? "true" : "false");
      return;
    }
    if (value instanceof Number n) {
      sb.append(numberToString(n));
      return;
    }
    if (value instanceof Map<?, ?> m) {
      @SuppressWarnings("unchecked")
      Map<String, Object> obj = (Map<String, Object>) m;
      sb.append('{');
      if (pretty && !obj.isEmpty()) sb.append('\n');
      int i = 0;
      for (Map.Entry<String, Object> e : obj.entrySet()) {
        if (pretty) indent(sb, indent + 1);
        sb.append('"').append(escapeString(e.getKey())).append('"').append(':');
        if (pretty) sb.append(' ');
        writeValue(sb, e.getValue(), indent + 1, pretty);
        i++;
        if (i < obj.size()) {
          sb.append(',');
          if (pretty) sb.append('\n');
        }
      }
      if (pretty && !obj.isEmpty()) {
        sb.append('\n');
        indent(sb, indent);
      }
      sb.append('}');
      return;
    }
    if (value instanceof List<?> list) {
      sb.append('[');
      if (pretty && !list.isEmpty()) sb.append('\n');
      for (int i = 0; i < list.size(); i++) {
        if (pretty) indent(sb, indent + 1);
        writeValue(sb, list.get(i), indent + 1, pretty);
        if (i < list.size() - 1) {
          sb.append(',');
          if (pretty) sb.append('\n');
        }
      }
      if (pretty && !list.isEmpty()) {
        sb.append('\n');
        indent(sb, indent);
      }
      sb.append(']');
      return;
    }

    // Fallback
    sb.append('"').append(escapeString(String.valueOf(value))).append('"');
  }

  private static void indent(StringBuilder sb, int level) {
    for (int i = 0; i < level; i++) sb.append("  ");
  }

  private static String numberToString(Number n) {
    // Avoid scientific notation for integers if possible.
    if (n instanceof Double d) {
      if (Double.isNaN(d) || Double.isInfinite(d)) return "null";
      return stripTrailingZeros(d.toString());
    }
    if (n instanceof Float f) {
      if (Float.isNaN(f) || Float.isInfinite(f)) return "null";
      return stripTrailingZeros(f.toString());
    }
    return n.toString();
  }

  private static String stripTrailingZeros(String s) {
    if (!s.contains(".")) return s;
    // Keep it simple: remove trailing zeros and possibly trailing dot.
    while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
    if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
    return s;
  }

  private static String escapeString(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"': sb.append("\\\""); break;
        case '\\': sb.append("\\\\"); break;
        case '\b': sb.append("\\b"); break;
        case '\f': sb.append("\\f"); break;
        case '\n': sb.append("\\n"); break;
        case '\r': sb.append("\\r"); break;
        case '\t': sb.append("\\t"); break;
        default:
          if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
          else sb.append(c);
      }
    }
    return sb.toString();
  }

  private static final class Parser {
    private final Reader reader;
    private int nextChar = -2; // -2 means "unread"

    Parser(Reader reader) {
      this.reader = reader;
    }

    Object parseValue() throws IOException {
      skipWhitespace();
      int c = read();
      if (c == -1) throw new IllegalStateException("Unexpected EOF");

      if (c == '{') return parseObject();
      if (c == '[') return parseArray();
      if (c == '"') return parseString();
      if (c == 't' || c == 'f') return parseBoolean(c);
      if (c == 'n') {
        expectKeyword("ull", true); // already consumed 'n'
        return null;
      }
      if (c == '-' || (c >= '0' && c <= '9')) {
        return parseNumber(c);
      }

      throw new IllegalStateException("Unexpected character: " + (char) c);
    }

    private Map<String, Object> parseObject() throws IOException {
      Map<String, Object> obj = new LinkedHashMap<>();
      skipWhitespace();
      int c = read();
      if (c == '}') return obj;
      unread(c);

      while (true) {
        skipWhitespace();
        int keyQuote = read();
        if (keyQuote != '"') throw new IllegalStateException("Expected string key");
        String key = parseString();
        skipWhitespace();
        int colon = read();
        if (colon != ':') throw new IllegalStateException("Expected ':' after key");
        Object value = parseValue();
        obj.put(key, value);
        skipWhitespace();
        int next = read();
        if (next == ',') continue;
        if (next == '}') break;
        throw new IllegalStateException("Expected ',' or '}' in object");
      }
      return obj;
    }

    private List<Object> parseArray() throws IOException {
      List<Object> arr = new ArrayList<>();
      skipWhitespace();
      int c = read();
      if (c == ']') return arr;
      unread(c);

      while (true) {
        Object v = parseValue();
        arr.add(v);
        skipWhitespace();
        int next = read();
        if (next == ',') continue;
        if (next == ']') break;
        throw new IllegalStateException("Expected ',' or ']' in array");
      }
      return arr;
    }

    private String parseString() throws IOException {
      StringBuilder sb = new StringBuilder();
      while (true) {
        int c = read();
        if (c == -1) throw new IllegalStateException("Unexpected EOF in string");
        if (c == '"') break;
        if (c == '\\') {
          int esc = read();
          switch (esc) {
            case '"': sb.append('"'); break;
            case '\\': sb.append('\\'); break;
            case '/': sb.append('/'); break;
            case 'b': sb.append('\b'); break;
            case 'f': sb.append('\f'); break;
            case 'n': sb.append('\n'); break;
            case 'r': sb.append('\r'); break;
            case 't': sb.append('\t'); break;
            case 'u':
              sb.append((char) readUnicodeEscape());
              break;
            default:
              throw new IllegalStateException("Invalid escape: \\" + (char) esc);
          }
        } else {
          sb.append((char) c);
        }
      }
      return sb.toString();
    }

    private int readUnicodeEscape() throws IOException {
      int value = 0;
      for (int i = 0; i < 4; i++) {
        int c = read();
        if (c == -1) throw new IllegalStateException("Unexpected EOF in unicode escape");
        int digit = Character.digit((char) c, 16);
        if (digit < 0) throw new IllegalStateException("Invalid hex digit: " + (char) c);
        value = (value << 4) + digit;
      }
      return value;
    }

    private Boolean parseBoolean(int first) throws IOException {
      if (first == 't') {
        expectKeyword("rue", false);
        return Boolean.TRUE;
      }
      if (first == 'f') {
        expectKeyword("alse", false);
        return Boolean.FALSE;
      }
      throw new IllegalStateException("Invalid boolean");
    }

    private void expectKeyword(String rest, boolean alreadyConsumedN) throws IOException {
      // If alreadyConsumedN=true, caller consumed only 'n', and expects "ull".
      // Otherwise, caller has consumed 't' or 'f' etc.
      // We just read 'rest' characters.
      for (int i = 0; i < rest.length(); i++) {
        int c = read();
        if (c == -1 || c != rest.charAt(i)) {
          throw new IllegalStateException("Unexpected keyword");
        }
      }
    }

    private Number parseNumber(int firstChar) throws IOException {
      StringBuilder sb = new StringBuilder();
      sb.append((char) firstChar);
      while (true) {
        reader.mark(1);
        int c = read();
        if (c == -1) break;
        if ((c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
          sb.append((char) c);
        } else {
          unread(c);
          break;
        }
      }

      String s = sb.toString();
      if (s.indexOf('.') >= 0 || s.indexOf('e') >= 0 || s.indexOf('E') >= 0) {
        return Double.parseDouble(s);
      }
      try {
        return Long.parseLong(s);
      } catch (NumberFormatException ex) {
        return Double.parseDouble(s);
      }
    }

    private void skipWhitespace() throws IOException {
      while (true) {
        int c = read();
        if (c == -1) return;
        if (!Character.isWhitespace((char) c)) {
          unread(c);
          return;
        }
      }
    }

    private int read() throws IOException {
      if (nextChar != -2) {
        int c = nextChar;
        nextChar = -2;
        return c;
      }
      return reader.read();
    }

    private void unread(int c) {
      nextChar = c;
    }
  }
}

