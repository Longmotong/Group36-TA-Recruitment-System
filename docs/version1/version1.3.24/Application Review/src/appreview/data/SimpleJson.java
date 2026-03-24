package appreview.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser/writer based on Java SE only.
 */
public final class SimpleJson {
    private SimpleJson() {
    }

    /**
     * Parse JSON string into Map/List/String/Number/Boolean/null.
     *
     * @param text json text
     * @return parsed object
     */
    public static Object parse(String text) {
        return new Parser(text).parse();
    }

    /**
     * Serialize object into JSON text.
     *
     * @param value java object
     * @return json text
     */
    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, value, 0);
        return sb.toString();
    }

    private static void writeValue(StringBuilder sb, Object value, int indent) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append('"').append(escape((String) value)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map) {
            writeObject(sb, (Map<?, ?>) value, indent);
        } else if (value instanceof List) {
            writeArray(sb, (List<?>) value, indent);
        } else {
            sb.append('"').append(escape(value.toString())).append('"');
        }
    }

    private static void writeObject(StringBuilder sb, Map<?, ?> map, int indent) {
        sb.append("{");
        if (!map.isEmpty()) {
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i++ > 0) {
                    sb.append(",");
                }
                sb.append("\n").append(spaces(indent + 2));
                sb.append('"').append(escape(String.valueOf(entry.getKey()))).append("\": ");
                writeValue(sb, entry.getValue(), indent + 2);
            }
            sb.append("\n").append(spaces(indent));
        }
        sb.append("}");
    }

    private static void writeArray(StringBuilder sb, List<?> list, int indent) {
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\n").append(spaces(indent + 2));
            writeValue(sb, list.get(i), indent + 2);
        }
        if (!list.isEmpty()) {
            sb.append("\n").append(spaces(indent));
        }
        sb.append("]");
    }

    private static String spaces(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) {
            this.text = text == null ? "" : text.trim();
            this.index = 0;
        }

        private Object parse() {
            skipWs();
            Object value = parseValue();
            skipWs();
            return value;
        }

        private Object parseValue() {
            skipWs();
            if (index >= text.length()) {
                return null;
            }
            char c = text.charAt(index);
            if (c == '{') {
                return parseObject();
            }
            if (c == '[') {
                return parseArray();
            }
            if (c == '"') {
                return parseString();
            }
            if (c == 't' || c == 'f') {
                return parseBoolean();
            }
            if (c == 'n') {
                index += 4;
                return null;
            }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            index++;
            skipWs();
            if (peek('}')) {
                index++;
                return map;
            }
            while (index < text.length()) {
                String key = parseString();
                skipWs();
                expect(':');
                skipWs();
                Object val = parseValue();
                map.put(key, val);
                skipWs();
                if (peek('}')) {
                    index++;
                    break;
                }
                expect(',');
                skipWs();
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<Object>();
            index++;
            skipWs();
            if (peek(']')) {
                index++;
                return list;
            }
            while (index < text.length()) {
                list.add(parseValue());
                skipWs();
                if (peek(']')) {
                    index++;
                    break;
                }
                expect(',');
                skipWs();
            }
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    if (index >= text.length()) {
                        break;
                    }
                    char e = text.charAt(index++);
                    switch (e) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(e);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            String hex = text.substring(index, index + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            index += 4;
                            break;
                        default:
                            sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (text.startsWith("true", index)) {
                index += 4;
                return Boolean.TRUE;
            }
            index += 5;
            return Boolean.FALSE;
        }

        private Number parseNumber() {
            int start = index;
            while (index < text.length()) {
                char c = text.charAt(index);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    index++;
                } else {
                    break;
                }
            }
            String num = text.substring(start, index);
            if (num.contains(".") || num.contains("e") || num.contains("E")) {
                return Double.valueOf(num);
            }
            try {
                return Integer.valueOf(num);
            } catch (NumberFormatException ex) {
                return Long.valueOf(num);
            }
        }

        private void expect(char c) {
            if (index >= text.length() || text.charAt(index) != c) {
                throw new IllegalArgumentException("Invalid JSON near index " + index);
            }
            index++;
        }

        private boolean peek(char c) {
            return index < text.length() && text.charAt(index) == c;
        }

        private void skipWs() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }
    }
}
