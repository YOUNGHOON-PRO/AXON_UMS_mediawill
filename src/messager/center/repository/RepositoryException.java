package messager.center.repository;

/**
 * UnitInfo객체의 저장이나 로드시 에러 발생하면 Throw된다.
 */
public class RepositoryException
    extends Exception
{
    public RepositoryException() {
        super();
    }

    public RepositoryException(String str) {
        super(str);
    }
}
