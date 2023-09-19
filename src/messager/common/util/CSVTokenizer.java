package messager.common.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * 1행의 CSV 형식의 데이터를 해석해, 각각의 항목으로 분해하는 클래스.
 * CSV형식에 대응한 java.util.StringTokenizer 와 같은 것.
 *
 * @version 1.0.8 (2008.02.05)
 * @author 이명재
 */

public class CSVTokenizer implements Enumeration {
	private String source;                    // 대상이 되는 문자열
	private int currentPosition;              // 다음의 읽어들일 위치
	private int maxPosition;


	/**
	 * CSV 형식의 line을 해석하는 CSVTokenizer 의 인스턴스를
	 * 작성한다.
	 *
	 * @param line CSV형식의 문자열. 개행코드를 포함하지 않는다.
	 */
	public CSVTokenizer(String line) {
		source = line;
		currentPosition = 0;
		maxPosition = line.length();
	}

	/**
	 * 다음의 콤마가 있는 위치를 돌려준다.
	 * 콤마가 남아있지 않은 경우는 nextComma() == maxPosition 가 된다.
	 * 또 마지막 항목이 공백인 경우도 nextComma() == maxPosition 가 된다.
	 *
	 * @param ind 검색을 시작할 위치
	 * @return 다음 콤마가 있는 위치. 콤마가 없는 경우 문자열의 길이가 된다.
	 */
	private int nextComma(int ind) {
		boolean inquote = false;
		while (ind < maxPosition) {
			char ch = source.charAt(ind);
			if (!inquote && ch == ',') {
				break;
			} else if ('"' == ch) {
				inquote = !inquote;       // ""처리도 이것으로 OK
			}
			ind ++;
		}
		return ind;
	}

	/**
	 * 포함되어있는 항목의 수를 반환한다.
	 *
	 * @return 포함되어있는 항목의 수
	 */
	public int countTokens() {
		int i = 0;
		int ret = 1;
		while ((i = nextComma(i)) < maxPosition) {
			i ++;
			ret ++;
		}
		return ret;
	}

	/**
	 * 다음의 항목의 문자열을 반환한다.
	 *
	 * @return 다음 항목
	 * @exception NoSuchElementException 항목이 남아있지 않을 때
	 */
	public String nextToken() {
		// ">=" 의 끝의 항목을 정확히 처리할 수 없다.
		// 말미의 항목이 비어있는(콤마로 1행이 끝나는) 경우, 예외가 발생하므로
		if (currentPosition > maxPosition)
			throw new NoSuchElementException(toString()+"#nextToken");

		int st = currentPosition;
		currentPosition = nextComma(currentPosition);

		StringBuffer strb = new StringBuffer();
		while (st < currentPosition) {
			char ch = source.charAt(st++);
			if (ch == '"') {
				// "가 단독으로 나타났을 때에는 아무것도 하지 않는다.
				if ((st < currentPosition) && (source.charAt(st) == '"')) {
					strb.append(ch);
					st ++;
				}
			} else {
				strb.append(ch);
			}
		}
		currentPosition ++;
		return new String(strb);
	}

	/**
	 * <code>nextToken</code> 메소드와 동일하며,
	 * 다음 항목의 문자열을 반환한다.<br>
	 * 다만 반환하는 값이 String이 아닌 Object형이다.<br>
	 * java.util.Enumeration을 구현하기 위해 이 메소드가 있다.
	 *
	 * @return 다음 항목
	 * @exception NoSuchElementException 항목이 남아있지 않을 때
	 * @see java.util.Enumeration
	 * @see jp.ac.wakhok.tomoharu.csv.CSVTokenizer#nextElement()
	 */
	public Object nextElement() {
		return nextToken();
	}

	/**
	 * 아직 항목이 남아있는지 어떤지 조사한다.
	 *
	 * @return 아직 항목이 남아있으면 true
	 */
	public boolean hasMoreTokens() {
		// "<=" 이 아닌 "<" 이면 말미의 항목을 정확하게 처리할 수 없다.
		return (nextComma(currentPosition) <= maxPosition);
	}

	/**
	 * <code>hasMoreTokens</code> 메소드와 동일하며 항목이 남아있는지 어떤지 조사한다.<br>
	 * java.util.Enumeration 을 구현하기 위해 이 메소드가 있다.
	 *
	 * @return 항목이 남아있으면 true
	 * @see java.util.Enumeration
	 * @see jp.ac.wakhok.tomoharu.csv.CSVTokenizer#hasMoreTokens()
	 */
    public boolean hasMoreElements() {
		return hasMoreTokens();
    }

	/**
	 * 인스턴스의 문자열표현을 반환한다.
	 *
	 * @return 인스턴스의 문자열을 표현
	 */
	public String toString() {
		return "CSVTokenizer(\""+source+"\")";
	}
}
