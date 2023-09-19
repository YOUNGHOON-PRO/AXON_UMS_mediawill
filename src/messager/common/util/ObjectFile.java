package messager.common.util;

import java.io.*;

/**
 * 객체를 Serializable를 통해서 File에 write하거나 File에서 객체를 읽는다.
 */
public class ObjectFile
{
    /**
     * file에서 객체를 읽는다.
     *
     * @param objFile
     *            객체가 저장된 파일의 File객체
     * @return 객체 파일에서 읽은 Object
     * @exception IOException
     */
    public static Object readObject(File objFile)
        throws IOException {
        InputStream in = null;
        ObjectInput objin = null;
        IOException exception = null;
        Object object = null;

        try {
            in = new BufferedInputStream(new FileInputStream(objFile));
            objin = new ObjectInputStream(in);
            object = objin.readObject();
        }
        catch (IOException ex) {
            exception = ex;
        }
        catch (Exception ex) {
            exception = new IOException(ex.getMessage());
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {
                }
            }

            if (objin != null) {
                try {
                    objin.close();
                }
                catch (IOException ex) {
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
        return object;
    }

    /**
     * 지정된 파일에 객체를 Write한다.
     *
     * @param file
     *            객체를 저장할 파일에 대한 File 객체
     * @param object
     *            저장할 객체
     */
    public static void writeObject(File file, Object object)
        throws IOException {
        OutputStream out = null;
        ObjectOutput objout = null;
        IOException exception = null;

        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            objout = new ObjectOutputStream(out);
            objout.writeObject(object);
        }
        catch (IOException ex) {
            exception = ex;
        }
        catch (Exception ex) {
            exception = new IOException(ex.getMessage());
        }
        finally {
            if (objout != null) {
                try {
                    objout.close();
                }
                catch (IOException ex) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                }
            }
        }

        if (exception != null) {
            throw exception;
        }
    }
}
