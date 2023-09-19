package messager.response.util;
/*
* 설명: 날짜의 포멧을 정해주는 클래스이다.
*		대개 날짜와 MID로 합쳐진 파일이름같은것을 명명하는것도 처리해준다.
*		로그 파일 이름이던가 혹은 MID같은 것의 이름을 지어준다.
*/

import java.util.*;
import java.text.*;

/**
 * Log파일의 날짜 표현 클래스
 * @version 1.0
 * @author ymkim
 */
public class LogDateWriter
{
	/**
	 * 현재 날짜를 표현한다.
	 * @version 1.0
	 * @author ymkim
	 * @return String 현재 날짜를 표현한다.
	 */
	public static String getDisplayTime()
	{
		DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT,
				Locale.getDefault());
		return fmt.format(new Date());
	}

	/**
	 * 현재 날짜를 표현한다.
	 * @version 1.0
	 * @author ymkim
	 * @return String 현재 날짜를 표현한다.
	 */
	public static String getLogTime()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		return fmt.format(new Date());
	}

	/**
	 * 현재 날짜를 표현한다.(로그파일용)
	 * @version 1.0
	 * @author ymkim
	 * @return String 현재 날짜를 표현한다.
	 */
	public static String getResultLogTime()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		return fmt.format(new Date());
	}

	/**
	 * 현재 날짜를 표현한다.(날짜 폴더용)
	 * @version 1.0
	 * @author ymkim
	 * @return String 현재 날짜를 표현한다.
	 */
	public static String getLogFolderName()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd", Locale.US);
		return fmt.format(new Date());
	}

	/**
	 * 현재 날짜를 표현한다.(헤더용)
	 * @version 1.0
	 * @author ymkim
	 * @return String 현재 날짜를 표현한다.
	 */
	public static String getMailFormatTime()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);

		StringBuffer sb = new StringBuffer();
		return (sb.append(fmt.format(new Date())).append(' ').append(initTimeZone())).toString();
	}

	/**
	 * TimeZone을 얻는다.
	 * @version 1.0
	 * @author ymkim
	 * @return String TimeZone을 얻는다.
	 */
	private static String initTimeZone()
	{
		DecimalFormat numberFormat = new DecimalFormat("00");
		StringBuffer sb = new StringBuffer();
		TimeZone timeZone = TimeZone.getDefault();
		int zoneOffset = timeZone.getRawOffset();
		String zoneSign = "+";
		if( zoneOffset < 0 )
		{
			zoneOffset = -zoneOffset;
			zoneSign = "-";
		}

		double zoneMinuteOffset = zoneOffset / 60000;
		sb.append(zoneSign);
		sb.append(numberFormat.format(zoneMinuteOffset / 60));
		sb.append(numberFormat.format(zoneMinuteOffset % 60));
		return sb.toString();
	}
}