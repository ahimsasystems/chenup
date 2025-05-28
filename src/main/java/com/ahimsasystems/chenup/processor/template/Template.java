package com.ahimsasystems.chenup.processor.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Template {
    private List<TemplateToken> fragments;

    public void compile(String template) {
        // Internally uses tokenizer (could be inline or delegated)
        fragments = tokenize(template);
    }

    public String render(Map<String, String> values) {
        StringBuilder sb = new StringBuilder();
        for (TemplateToken token : fragments) {
            switch (token) {
                case TextToken(String text) -> sb.append(text);
                case PlaceholderToken(String key) -> sb.append(values.getOrDefault(key, ""));
            }
        }
        return sb.toString();
    }

    // Internal tokenizer — package-private or private static
    private static List<TemplateToken> tokenize(String template) {
        List<TemplateToken> tokens = new ArrayList<>();
        int i = 0;
        int len = template.length();
        StringBuilder buf = new StringBuilder();

        while (i < len) {
            char c = template.charAt(i);
            if (c == '$' && i + 1 < len && template.charAt(i + 1) == '(') {
                if (buf.length() > 0) {
                    tokens.add(new TextToken(buf.toString()));
                    buf.setLength(0);
                }
                i += 2;
                int start = i;
                while (i < len && template.charAt(i) != ')') i++;
                if (i >= len) throw new IllegalArgumentException("Unclosed placeholder");
                String key = template.substring(start, i).trim();
                tokens.add(new PlaceholderToken(key));
            } else {
                buf.append(c);
            }
            i++; // skip ')'
        }
        if (buf.length() > 0) {
            tokens.add(new TextToken(buf.toString()));
        }
        return tokens;
    }
}
