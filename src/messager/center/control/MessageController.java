package messager.center.control;

import java.io.*;
import java.net.*;

public class MessageController
    extends Thread
{
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public MessageController(Socket socket) {
        this.socket = socket;
    }

    private InputStream getInputStream()
        throws Exception {
        if (in == null) {
            in = new BufferedInputStream(socket.getInputStream());
        }
        return in;
    }

    private OutputStream getOutputStream()
        throws Exception {
        if (out == null) {
            out = new BufferedOutputStream(socket.getOutputStream());
        }
        return out;
    }

    private void closeSocket() {
        if (in != null) {
            try {
                in.close();
            }
            catch (Exception ex) {
            }
            in = null;
        }

        if (out != null) {
            try {
                out.close();
            }
            catch (Exception ex) {
            }
            out = null;
        }

        if (socket != null) {
            try {
                socket.close();
            }
            catch (Exception ex) {
            }
            socket = null;
        }
    }

    public void run() {
        try {
            ControlCommand controlCmd = new ControlCommand();
            ControlResponse response = new ControlResponse();
            InputStream is = getInputStream();
            boolean b_run = true;

            while (b_run) {
                controlCmd.readCommand(is);
                byte[] responseData = null;
                switch (controlCmd.command) {
                    case ControlCommand.CMD_LIST:
                        System.out.println("MessageController.run() : CMD_LIST");
                        responseData = response.getList();
                        break;
                    case ControlCommand.CMD_PAUSE:
                        System.out.println("MessageController.run() : CMD_PAUSE");
                        responseData =
                            response.pause(controlCmd.taskNo, controlCmd.subTaskNo);
                        break;
                    case ControlCommand.CMD_STOP:
                        System.out.println("MessageController.run() : CMD_STOP");
                        responseData = response.stop(controlCmd.taskNo,
                            controlCmd.subTaskNo);
                        break;
                    case ControlCommand.CMD_SEND:
                        System.out.println("MessageController.run() : CMD_SEND");
                        responseData = response.start(controlCmd.taskNo,
                            controlCmd.subTaskNo);
                        break;
                    case ControlCommand.CMD_INFO:
                        System.out.println("MessageController.run() : CMD_INFO");
                        responseData = response.getInfo(controlCmd.taskNo,
                            controlCmd.subTaskNo);
                        break;
                    default:
                        System.out.println("MessageController.run() : default");
                        b_run = false;
                        break;
                }
                if (responseData != null) {
                    sendResponse(responseData);
                }
            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            closeSocket();
        }
    }

    private void sendResponse(byte[] responseData)
        throws Exception {
        OutputStream out = getOutputStream();
        out.write(responseData, 0, responseData.length);
        out.flush();
    }
}
