package messager.generator.content;

import java.text.*;
import java.util.*;

/**
 * Header에 Date 필드에 들어갈 날짜의 포맷을 생성한다.
 */
public final class MailDateFormat
    extends SimpleDateFormat
{
    //TimeZone의 Offset
    //private static String timeZoneOffset;
    //Mail Header의 날짜 포맷
    private static String dateFormat;

    static {
        //TimeZone의 Offset
        String timeZoneOffset = initTimeZoneOffset();
        //Date 필드를 표현할 포맷을 설정
        StringBuffer formatBuffer = new StringBuffer(
            "EEE, d MMM yyyy HH:mm:ss ").append(timeZoneOffset);
        dateFormat = formatBuffer.toString();
    }

    /**
     * TimeZone의 Offset를 얻는다.
     */
    private static String initTimeZoneOffset() {
        char[] timeZoneOffsetChars = new char[5];
        Calendar calendar = Calendar.getInstance();
        int timeZoneOffset = calendar.get(Calendar.ZONE_OFFSET)
            + calendar.get(Calendar.DST_OFFSET);
        if (timeZoneOffset < 0) {
            timeZoneOffsetChars[0] = '-';
            timeZoneOffset = -timeZoneOffset;
        }
        else {
            timeZoneOffsetChars[0] = '+';
        }

        int offsetMinutes = timeZoneOffset / 60 / 1000;
        int hours = offsetMinutes / 60;
        int minutes = offsetMinutes % 60;
        timeZoneOffsetChars[1] = Character.forDigit(hours / 10, 10);
        timeZoneOffsetChars[2] = Character.forDigit(hours % 10, 10);
        timeZoneOffsetChars[3] = Character.forDigit(minutes / 10, 10);
        timeZoneOffsetChars[4] = Character.forDigit(minutes % 10, 10);
        return new String(timeZoneOffsetChars);
    }

    /**
     * MailDateFormat 객체를 생성한다.
     */
    public MailDateFormat() {
        super(dateFormat, Locale.US);
    }

    /**
     * 지정된 Date 를 일자/시각 캐릭터 라인에 포맷 해, 지정된 StringBuffer 에 결과를 추가합니다.
     */
    public StringBuffer format(Date date, StringBuffer strbuffer,
                               FieldPosition fieldposition) {
        super.format(date, strbuffer, fieldposition);
        return strbuffer;
    }
}
