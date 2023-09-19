package messager.generator.repository;

import java.io.*;

import messager.generator.config.*;

/**
 * 머지가 필요하지 않는 첨부파일을 Unit별로 저장한다.
 * 이 첨부 파일 컨텐츠는 발송시 Unit별로 한번 읽혀져 Unit내에 포함된 모든 대상자에게
 * 발송이 이루어 진다.
 * 대상자 정보로 머지가 이루어진 컨텐츠의 뒤에 붙여서 발송 할 것이므로
 * Multipart의 끝을 알리는 Boundary를 붙인다.
 */
public class UnitAttachFile
{
    /** 저장될 디렉토리 명 */
    private final static String dirName = "attach";

    /** 라인 구분 */
    private final static String lineSeparator = "\r\n";

    /** 디렉토리의 File 객체 */
    private static File dirFile;

    static {

        String workPath = ConfigLoader.getString("work.path", "repository");

        dirFile = new File(workPath, dirName);

    }

    /** UnitName */
    private String unitName;

    /** 파일에 출력할 스트림 객체 */
    private PrintStream out;

    /** 컨텐츠의 구분에 사용될 Boundary 스트링 */
    private String boundary;

    /**
     * 객체 생성
     *
     * @param unitName
     * @param boundary
     */
    public UnitAttachFile(String unitName, String boundary) {
        this.unitName = unitName;
        this.boundary = boundary;
    }

    /**
     * 파일을 생성하고 출력스트림을 생성한다
     *
     * @throws IOException 파일 생성을 실패할 경우
     */
    private void open()
        throws IOException {
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                throw new IOException("Directory Create Fail : " + dirFile);
            }
        }
        File unitFile = new File(dirFile, unitName);
        out = new PrintStream(new BufferedOutputStream(new FileOutputStream(
            unitFile)));
    }

    /** 컨텐츠를 Write한다. */
    public void write(String data)
        throws IOException {
        if (out == null) {
            open();
        }
        out.print("--");
        out.print(boundary);
        out.print(lineSeparator);
        out.print(data);
        out.print(lineSeparator);
        out.print(lineSeparator);
    }

    /**
     * MultiPart의 끝을 알리는 Boundary 스트링을 Write하고
     * 출력 스트림을 닫는다.
     */
    public void close() {
        if (out != null) {
            out.print("--");
            out.print(boundary);
            out.print("--");
            out.print(lineSeparator);
            out.close();
            out = null;
        }
    }
}
