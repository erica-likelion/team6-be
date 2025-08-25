package likelion.sajaboys.soboonsoboon.util;

import likelion.sajaboys.soboonsoboon.domain.Post;

public final class TextSummarizer {

    private TextSummarizer() {
    }

    public static String oneLinePost(Post p) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(safe(enumName(p.getType()))).append("] ");
        sb.append(trim(safe(p.getTitle()), 60));
        sb.append(" @").append(trim(safe(p.getPlace()), 30));
        if (p.getPrice() != null) {
            sb.append(" | price=").append(p.getPrice().toPlainString());
        }
        if (p.getTimeStart() != null) {
            sb.append(" | start=").append(p.getTimeStart());
        }
        return sb.toString();
    }

    public static String briefContent(String content, int max) {
        if (content == null || content.isBlank()) return "";
        String s = content.replaceAll("\\s+", " ").trim();
        return trim(s, max);
    }

    private static String enumName(Enum<?> e) {
        return e == null ? "" : e.name();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
