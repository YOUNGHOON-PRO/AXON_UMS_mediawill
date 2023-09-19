package messager.center.repository;

import java.io.*;

public class ObjectStream
{
    /**
     * byte배열을 Object로 변환한다.
     * @param bytes 객체로 변환할 바이트 스트림
     * @return 변환된 객체
     * @exception Exception 변환오류 발생 또는 IO오류 발생
     */
    public static Object toObject(byte[] bytes)
        throws Exception {
        ObjectInput oin = null;
        Exception exception = null;
        Object object = null;
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try {
            oin = new ObjectInputStream(is);
            object = oin.readObject();
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (oin != null) {
                try {
                    oin.close();
                }
                catch (Exception ex) {
                }
                oin = null;
            }
            is = null;
        }

        if (exception != null) {
            throw exception;
        }
        return object;
    }

    /**
     * object를 byte배열로 변환한다.
     * @param object 바이트 배열로 변환할 객체
     * @return  변환된 byte 배열
     * @exception Exception 입출력 오류 발생 또는 객체 변환 오류
     */
    public static byte[] toBytes(Object object)
        throws Exception {
        ObjectOutput oout = null;
        Exception exception = null;
        byte[] bytes = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream(8192);

        try {
            oout = new ObjectOutputStream(os);
            oout.writeObject(object);
            oout.flush();
            bytes = os.toByteArray();
        }
        catch (Exception ex) {
            exception = ex;
        }
        finally {
            if (oout != null) {
                try {
                    oout.close();
                }
                catch (Exception ex) {
                }

                oout = null;
                os = null;
            }
        }
        return bytes;
    }
}
