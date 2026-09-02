package com.github.eclipsethemes.eclipse.adapters.ui;

import java.util.Map;
import java.util.stream.Collectors;

/** Temporary debug helper for NDJSON instrumentation. */
public final class DbgNdjson {
	private DbgNdjson() {
	}

	public static String line(String hypothesisId, String location, String message, Map<String, ?> data) {
		String dataJson = data.entrySet().stream()
				.map(e -> "\"" + esc(e.getKey()) + "\":" + jsonVal(e.getValue()))
				.collect(Collectors.joining(",", "{", "}"));
		return "{\"hypothesisId\":\"" + esc(hypothesisId) + "\",\"location\":\"" + esc(location)
				+ "\",\"message\":\"" + esc(message) + "\",\"data\":" + dataJson + ",\"timestamp\":"
				+ System.currentTimeMillis() + "}\n";
	}

	private static String jsonVal(Object v) {
		if (v == null) {
			return "null";
		}
		if (v instanceof Number || v instanceof Boolean) {
			return String.valueOf(v);
		}
		return "\"" + esc(String.valueOf(v)) + "\"";
	}

	private static String esc(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
