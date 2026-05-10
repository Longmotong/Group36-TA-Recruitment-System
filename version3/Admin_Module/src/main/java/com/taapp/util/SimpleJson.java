package com.taapp.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleJson {
    private SimpleJson() {}

    public static Object parse(String json) {
        if (json == null) throw new IllegalArgumentException("json is null");
        Parser p = new Parser(json);
        Object v = p.parseValue();
        p.skipWhitespace();
        if (!p.isEof()) {
            throw p.error("Trailing content");
        }
        return v;
    }

    public static Map<String, Object> parseObject(String json) {
        Object v = parse(json);
        if (!(v instanceof Map)) throw new IllegalArgumentException("Not a JSON object");
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) v;
        return obj;
    }

    private static final class Parser {
        private final String s;
        private int i = 0;

        Parser(String s) { this.s = s; }

        boolean isEof() { return i >= s.length(); }

        void skipWhitespace() {
            while (!isEof()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
                else break;
            }
        }

        RuntimeException error(String msg) {
            int from = Math.max(0, i - 20);
            int to = Math.min(s.length(), i + 20);
            String ctx = s.substring(from, to).replace("\n", "\\n").replace("\r", "\\r");
            return new IllegalArgumentException(msg + " at " + i + " near \"" + ctx + "\"");
        }

        Object parseValue() {
            skipWhitespace();
            if (isEof()) throw error("Unexpected EOF");
            char c = s.charAt(i);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
            throw error("Unexpected char '" + c + "'");
        }

        Map<String, Object> parseObject() {
            expect('{');
            skipWhitespace();
            Map<String, Object> obj = new LinkedHashMap<>();
            if (peek('}')) {
                i++;
                return obj;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object val = parseValue();
                obj.put(key, val);
                skipWhitespace();
                if (peek('}')) {
                    i++;
                    return obj;
                }
                expect(',');
            }
        }

        List<Object> parseArray() {
            expect('[');
            skipWhitespace();
            List<Object> arr = new ArrayList<>();
            if (peek(']')) {
                i++;
                return arr;
            }
            while (true) {
                Object v = parseValue();
                arr.add(v);
                skipWhitespace();
                if (peek(']')) {
                    i++;
                    return arr;
                }
                expect(',');
            }
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (!isEof()) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (isEof()) throw error("Unterminated escape");
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (i + 4 > s.length()) throw error("Invalid unicode escape");
                            String hex = s.substring(i, i + 4);
                            i += 4;
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                            } catch (NumberFormatException ex) {
                                throw error("Invalid unicode escape");
                            }
                            break;
                        default:
                            throw error("Invalid escape \\" + e);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw error("Unterminated string");
        }

        Object parseNumber() {
            int start = i;
            if (peek('-')) i++;
            while (!isEof() && Character.isDigit(s.charAt(i))) i++;
            boolean isFloat = false;
            if (!isEof() && s.charAt(i) == '.') {
                isFloat = true;
                i++;
                while (!isEof() && Character.isDigit(s.charAt(i))) i++;
            }
            if (!isEof() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                isFloat = true;
                i++;
                if (!isEof() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
                while (!isEof() && Character.isDigit(s.charAt(i))) i++;
            }
            String num = s.substring(start, i);
            try {
                if (!isFloat) {
                    long l = Long.parseLong(num);
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                    return l;
                }
                return Double.parseDouble(num);
            } catch (NumberFormatException e) {
                throw error("Invalid number");
            }
        }

        Boolean parseBoolean() {
            if (match("true")) return Boolean.TRUE;
            if (match("false")) return Boolean.FALSE;
            throw error("Invalid boolean");
        }

        Object parseNull() {
            if (match("null")) return null;
            throw error("Invalid null");
        }

        boolean match(String token) {
            if (s.regionMatches(i, token, 0, token.length())) {
                i += token.length();
                return true;
            }
            return false;
        }

        boolean peek(char c) {
            return !isEof() && s.charAt(i) == c;
        }

        void expect(char c) {
            if (isEof() || s.charAt(i) != c) throw error("Expected '" + c + "'");
            i++;
        }
    }
}

