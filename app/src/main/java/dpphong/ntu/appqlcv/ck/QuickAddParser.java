package dpphong.ntu.appqlcv.ck;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bộ phân tích câu thêm nhanh bằng ngôn ngữ tự nhiên (tiếng Việt).
 * Ví dụ: "Họp nhóm 9h mai !cao #Công việc".
 */
public final class QuickAddParser {

    private QuickAddParser() {
    }

    public static class Result {
        public String title = "";
        public String date;
        public String time;
        public String priority = "Vừa";
        public String listName;
        public String summary = "";
    }

    private static final Pattern LIST_PATTERN =
            Pattern.compile("#([\\p{L}\\p{N}_]+)");
    private static final Pattern TIME_PATTERN =
            Pattern.compile("(?i)(\\d{1,2})\\s*(?:h|giờ|:)\\s*(\\d{2})?\\b");
    private static final Pattern EXPLICIT_DATE_PATTERN =
            Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?\\b");

    public static Result parse(String raw) {
        Result result = new Result();
        if (raw == null) raw = "";

        String text = " " + raw.trim() + " ";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 1. Danh sách: #Tên
        Matcher listMatcher = LIST_PATTERN.matcher(text);
        if (listMatcher.find()) {
            result.listName = listMatcher.group(1);
            text = text.replace(listMatcher.group(0), " ");
        }

        // 2. Ưu tiên: !cao, !thấp, !vừa
        String lower = text.toLowerCase(Locale.getDefault());
        if (lower.contains("!cao") || lower.contains("!high")) {
            result.priority = "Cao";
            text = text.replaceAll("(?i)!cao|!high", " ");
        } else if (lower.contains("!thấp") || lower.contains("!thap") || lower.contains("!low")) {
            result.priority = "Thấp";
            text = text.replaceAll("(?i)!thấp|!thap|!low", " ");
        } else if (lower.contains("!vừa") || lower.contains("!vua") || lower.contains("!medium")) {
            result.priority = "Vừa";
            text = text.replaceAll("(?i)!vừa|!vua|!medium", " ");
        }

        // 3. Ngày
        boolean hasDate = false;
        lower = text.toLowerCase(Locale.getDefault());
        if (lower.matches(".*\\b(hôm nay|hom nay|hnay)\\b.*")) {
            hasDate = true;
            text = text.replaceAll("(?i)\\b(hôm nay|hom nay|hnay)\\b", " ");
        } else if (lower.matches(".*\\b(ngày mai|ngay mai|mai)\\b.*")) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            hasDate = true;
            text = text.replaceAll("(?i)\\b(ngày mai|ngay mai|mai)\\b", " ");
        } else if (lower.matches(".*\\b(mốt|ngày mốt)\\b.*")) {
            calendar.add(Calendar.DAY_OF_MONTH, 2);
            hasDate = true;
            text = text.replaceAll("(?i)\\b(ngày mốt|mốt)\\b", " ");
        } else if (lower.matches(".*\\b(tuần sau|tuan sau)\\b.*")) {
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            hasDate = true;
            text = text.replaceAll("(?i)\\b(tuần sau|tuan sau)\\b", " ");
        } else {
            Matcher weekdayMatcher = Pattern.compile("(?i)\\b(?:thứ|thu)\\s*([2-7]|cn|chủ nhật|chu nhat)\\b")
                    .matcher(text);
            if (weekdayMatcher.find()) {
                String token = weekdayMatcher.group(1).toLowerCase(Locale.getDefault());
                int target;
                switch (token) {
                    case "3": target = Calendar.TUESDAY; break;
                    case "4": target = Calendar.WEDNESDAY; break;
                    case "5": target = Calendar.THURSDAY; break;
                    case "6": target = Calendar.FRIDAY; break;
                    case "7": target = Calendar.SATURDAY; break;
                    default: target = Calendar.SUNDAY; break;
                }
                int current = calendar.get(Calendar.DAY_OF_WEEK);
                int delta = (target - current + 7) % 7;
                calendar.add(Calendar.DAY_OF_MONTH, delta);
                hasDate = true;
                text = text.replace(weekdayMatcher.group(0), " ");
            } else {
                Matcher explicitDate = EXPLICIT_DATE_PATTERN.matcher(text);
                if (explicitDate.find()) {
                    try {
                        int day = Integer.parseInt(explicitDate.group(1));
                        int month = Integer.parseInt(explicitDate.group(2));
                        int year = calendar.get(Calendar.YEAR);
                        if (explicitDate.group(3) != null) {
                            year = Integer.parseInt(explicitDate.group(3));
                            if (year < 100) year += 2000;
                        }
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        hasDate = true;
                        text = text.replace(explicitDate.group(0), " ");
                    } catch (Exception ignored) {
                        hasDate = false;
                    }
                }
            }
        }

        if (!hasDate) {
            // Mặc định: hôm nay
        }
        result.date = dbFormat.format(calendar.getTime());

        // 4. Giờ
        Matcher timeMatcher = TIME_PATTERN.matcher(text);
        if (timeMatcher.find()) {
            try {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = timeMatcher.group(2) != null ? Integer.parseInt(timeMatcher.group(2)) : 0;

                String lowerText = text.toLowerCase(Locale.getDefault());
                if ((lowerText.contains("chiều") || lowerText.contains("tối")
                        || lowerText.contains("pm")) && hour < 12) {
                    hour += 12;
                }
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    result.time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    text = text.replace(timeMatcher.group(0), " ");
                    text = text.replaceAll("(?i)\\b(chiều|tối|sáng|pm|am)\\b", " ");
                }
            } catch (Exception ignored) {
            }
        }

        // 5. Làm sạch tiêu đề
        String title = text.replaceAll("\\s+", " ").trim();
        if (title.isEmpty()) title = "Công việc mới";
        result.title = title;

        StringBuilder summary = new StringBuilder();
        summary.append("📅 ").append(result.date);
        if (result.time != null) summary.append(" • ⏰ ").append(result.time);
        summary.append(" • ⚡ ").append(result.priority);
        if (result.listName != null) summary.append(" • 🗂️ #").append(result.listName);
        result.summary = summary.toString();

        return result;
    }
}
