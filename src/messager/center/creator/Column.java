package messager.center.creator;

class Column
{
    /** integer */
    public final static int INTEGER = 1;

    /** long */
    public final static int LONG = 2;

    /** string [varchar or char] */
    public final static int STRING = 3;

    /** char */
    public final static int CHARACTER = 4;

    /**
     * string [varchar or char] -> integer <br>
     * 정의된 코드를 읽어올때 사용
     */
    public final static int STRING_INTEGER = 5;

    public String name;

    public int type;

    public String query;

    Column(String name, int type) {
        this.name = name;
        this.query = name;
        this.type = type;
    }

    Column(String name, String query, int type) {
        this.name = name;
        this.query = query;
        this.type = type;
    }
}