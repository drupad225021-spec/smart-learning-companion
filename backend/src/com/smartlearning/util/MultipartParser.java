package com.smartlearning.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultipartParser {

    public static class Part {
        private String name;
        private String filename;
        private String contentType;
        private byte[] data;

        public String getName() { return name; }
        public String getFilename() { return filename; }
        public String getContentType() { return contentType; }
        public byte[] getData() { return data; }
        public String getStringValue() {
            return data != null ? new String(data, StandardCharsets.UTF_8).trim() : "";
        }
    }

    public static List<Part> parse(InputStream is, String contentTypeHeader) throws IOException {
        List<Part> parts = new ArrayList<>();
        if (contentTypeHeader == null || !contentTypeHeader.contains("multipart/form-data")) {
            return parts;
        }

        String boundaryMarker = null;
        for (String param : contentTypeHeader.split(";")) {
            param = param.trim();
            if (param.startsWith("boundary=")) {
                boundaryMarker = param.substring(9).trim();
                if (boundaryMarker.startsWith("\"") && boundaryMarker.endsWith("\"")) {
                    boundaryMarker = boundaryMarker.substring(1, boundaryMarker.length() - 1);
                }
                break;
            }
        }

        if (boundaryMarker == null) return parts;

        // Read all stream bytes
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[8192];
        int read;
        while ((read = is.read(tmp)) != -1) {
            buffer.write(tmp, 0, read);
        }
        byte[] bodyBytes = buffer.toByteArray();

        byte[] delimiter = ("--" + boundaryMarker).getBytes(StandardCharsets.ISO_8859_1);
        List<Integer> positions = findOccurrences(bodyBytes, delimiter);

        for (int i = 0; i < positions.size() - 1; i++) {
            int start = positions.get(i) + delimiter.length;
            int end = positions.get(i + 1);

            // Skip CRLF immediately after delimiter
            if (start + 2 <= bodyBytes.length && bodyBytes[start] == '\r' && bodyBytes[start + 1] == '\n') {
                start += 2;
            }

            // Find header/body separator: CRLF CRLF
            byte[] headerSep = "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);
            int headerEnd = findFirst(bodyBytes, headerSep, start, end);
            if (headerEnd == -1) continue;

            String headersStr = new String(bodyBytes, start, headerEnd - start, StandardCharsets.UTF_8);
            int dataStart = headerEnd + 4;
            int dataEnd = end;
            // Trim trailing CRLF before next boundary
            if (dataEnd >= 2 && bodyBytes[dataEnd - 2] == '\r' && bodyBytes[dataEnd - 1] == '\n') {
                dataEnd -= 2;
            }

            if (dataEnd < dataStart) dataEnd = dataStart;
            byte[] partData = Arrays.copyOfRange(bodyBytes, dataStart, dataEnd);

            Part part = new Part();
            part.data = partData;

            // Parse headers
            for (String line : headersStr.split("\r\n")) {
                if (line.toLowerCase().startsWith("content-disposition:")) {
                    String[] tokens = line.substring(20).split(";");
                    for (String tok : tokens) {
                        tok = tok.trim();
                        if (tok.startsWith("name=")) {
                            part.name = stripQuotes(tok.substring(5).trim());
                        } else if (tok.startsWith("filename=")) {
                            part.filename = stripQuotes(tok.substring(9).trim());
                        }
                    }
                } else if (line.toLowerCase().startsWith("content-type:")) {
                    part.contentType = line.substring(13).trim();
                }
            }

            if (part.name != null) {
                parts.add(part);
            }
        }

        return parts;
    }

    private static String stripQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static List<Integer> findOccurrences(byte[] source, byte[] target) {
        List<Integer> list = new ArrayList<>();
        outer:
        for (int i = 0; i <= source.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    continue outer;
                }
            }
            list.add(i);
        }
        return list;
    }

    private static int findFirst(byte[] source, byte[] target, int from, int to) {
        outer:
        for (int i = from; i <= to - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
