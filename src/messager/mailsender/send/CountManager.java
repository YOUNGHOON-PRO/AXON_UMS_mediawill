package messager.mailsender.send;

/**
 * 실시간으로 발송 현황을 보관하는 객체
 * @value sCount : 성공 카운트
 * @value totlaCount : 대상자 리스트의 전체 카운트
 * @value endCount :  현재 유닛의 발송된 카운트
 */
public class CountManager
{
    private int sCount;
    private int totalCount;
    private int endCount;

    public void increaseSCount(int count) {
        sCount += count;
    }

    public void setTotalCount(int count) {
        totalCount = count;
    }

    public void increaseEndCount(int count) {
        endCount += count;
    }

    public int getSCount() {
        return sCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getEndCount() {
        return endCount;
    }
}
