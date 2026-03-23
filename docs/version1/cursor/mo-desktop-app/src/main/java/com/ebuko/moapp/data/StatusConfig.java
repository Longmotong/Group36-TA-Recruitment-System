package com.ebuko.moapp.data;

import com.ebuko.moapp.json.MiniJson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class StatusConfig {
  public static class Entry {
    public final String value;
    public final String label;
    public final String color;

    public Entry(String value, String label, String color) {
      this.value = value;
      this.label = label;
      this.color = color;
    }
  }

  private final Map<String, Entry> byValue = new HashMap<>();

  private StatusConfig() {}

  public static StatusConfig load(Path dataRoot) throws IOException {
    Path p = dataRoot.resolve("system").resolve("application_status_config.json");
    String text = Files.readString(p, StandardCharsets.UTF_8);
    @SuppressWarnings("unchecked")
    Map<String, Object> root = (Map<String, Object>) MiniJson.parse(text);

    StatusConfig cfg = new StatusConfig();
    Object statusesObj = root.get("statuses");
    if (statusesObj instanceof List<?> statuses) {
      for (Object stObj : statuses) {
        if (!(stObj instanceof Map<?, ?> st)) continue;
        String value = asString(st.get("value"));
        if (value.isEmpty()) continue;
        cfg.byValue.put(value, new Entry(value, asString(st.get("label")), asString(st.get("color"))));
      }
    }
    return cfg;
  }

  public Entry get(String value) {
    return byValue.get(value);
  }

  public String labelFor(String value) {
    Entry e = byValue.get(value);
    return e == null ? value : e.label;
  }

  private static String asString(Object v) {
    return v == null ? "" : String.valueOf(v);
  }
}

