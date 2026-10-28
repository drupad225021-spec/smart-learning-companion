package com.smartlearning.util;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON parser and serializer for Java.
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Object[]) {
            return toJson(Arrays.asList((Object[]) obj));
        }
        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
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
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static Object parse(String json) {
        if (json == null) return null;
        json = json.trim();
        if (json.isEmpty()) return null;
        int[] index = new int[]{0};
        skipWhitespace(json, index);
        return parseValue(json, index);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object res = parse(json);
        if (res instanceof Map) {
            return (Map<String, Object>) res;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object res = parse(json);
        if (res instanceof List) {
            return (List<Object>) res;
        }
        return new ArrayList<>();
    }

    private static Object parseValue(String json, int[] idx) {
        skipWhitespace(json, idx);
        if (idx[0] >= json.length()) return null;

        char c = json.charAt(idx[0]);
        if (c == '{') {
            return parseObjectInternal(json, idx);
        } else if (c == '[') {
            return parseArrayInternal(json, idx);
        } else if (c == '"') {
            return parseString(json, idx);
        } else if (c == 't' || c == 'f') {
            return parseBoolean(json, idx);
        } else if (c == 'n') {
            return parseNull(json, idx);
        } else if (c == '-' || Character.isDigit(c)) {
            return parseNumber(json, idx);
        }
        return null;
    }

    private static Map<String, Object> parseObjectInternal(String json, int[] idx) {
        Map<String, Object> map = new LinkedHashMap<>();
        idx[0]++; // skip '{'
        skipWhitespace(json, idx);

        if (idx[0] < json.length() && json.charAt(idx[0]) == '}') {
            idx[0]++;
            return map;
        }

        while (idx[0] < json.length()) {
            skipWhitespace(json, idx);
            if (idx[0] >= json.length()) break;
            if (json.charAt(idx[0]) != '"') {
                break;
            }
            String key = parseString(json, idx);
            skipWhitespace(json, idx);
            if (idx[0] < json.length() && json.charAt(idx[0]) == ':') {
                idx[0]++; // skip ':'
            }
            skipWhitespace(json, idx);
            Object value = parseValue(json, idx);
            map.put(key, value);

            skipWhitespace(json, idx);
            if (idx[0] < json.length() && json.charAt(idx[0]) == ',') {
                idx[0]++;
                skipWhitespace(json, idx);
            } else if (idx[0] < json.length() && json.charAt(idx[0]) == '}') {
                idx[0]++;
                break;
            }
        }
        return map;
    }

    private static List<Object> parseArrayInternal(String json, int[] idx) {
        List<Object> list = new ArrayList<>();
        idx[0]++; // skip '['
        skipWhitespace(json, idx);

        if (idx[0] < json.length() && json.charAt(idx[0]) == ']') {
            idx[0]++;
            return list;
        }

        while (idx[0] < json.length()) {
            skipWhitespace(json, idx);
            if (idx[0] >= json.length()) break;
            Object val = parseValue(json, idx);
            list.add(val);
            skipWhitespace(json, idx);
            if (idx[0] < json.length() && json.charAt(idx[0]) == ',') {
                idx[0]++;
                skipWhitespace(json, idx);
            } else if (idx[0] < json.length() && json.charAt(idx[0]) == ']') {
                idx[0]++;
                break;
            }
        }
        return list;
    }

    private static String parseString(String json, int[] idx) {
        idx[0]++; // skip opening '"'
        StringBuilder sb = new StringBuilder();
        while (idx[0] < json.length()) {
            char c = json.charAt(idx[0]);
            if (c == '"') {
                idx[0]++; // skip closing '"'
                return sb.toString();
            } else if (c == '\\') {
                idx[0]++;
                if (idx[0] < json.length()) {
                    char esc = json.charAt(idx[0]);
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
                            if (idx[0] + 4 < json.length()) {
                                String hex = json.substring(idx[0] + 1, idx[0] + 5);
                                sb.append((char) Integer.parseInt(hex, 16));
                                idx[0] += 4;
                            }
                            break;
                        default: sb.append(esc); break;
                    }
                }
            } else {
                sb.append(c);
            }
            idx[0]++;
        }
        return sb.toString();
    }

    private static Boolean parseBoolean(String json, int[] idx) {
        if (json.startsWith("true", idx[0])) {
            idx[0] += 4;
            return Boolean.TRUE;
        }
        if (json.startsWith("false", idx[0])) {
            idx[0] += 5;
            return Boolean.FALSE;
        }
        return null;
    }

    private static Object parseNull(String json, int[] idx) {
        if (json.startsWith("null", idx[0])) {
            idx[0] += 4;
        }
        return null;
    }

    private static Number parseNumber(String json, int[] idx) {
        int start = idx[0];
        boolean isFloating = false;
        while (idx[0] < json.length()) {
            char c = json.charAt(idx[0]);
            if (c == '.' || c == 'e' || c == 'E') {
                isFloating = true;
                idx[0]++;
            } else if (c == '-' || c == '+' || Character.isDigit(c)) {
                idx[0]++;
            } else {
                break;
            }
        }
        String numStr = json.substring(start, idx[0]);
        try {
            if (isFloating) {
                return Double.parseDouble(numStr);
            } else {
                long val = Long.parseLong(numStr);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private static void skipWhitespace(String json, int[] idx) {
        while (idx[0] < json.length() && Character.isWhitespace(json.charAt(idx[0]))) {
            idx[0]++;
        }
    }
}
