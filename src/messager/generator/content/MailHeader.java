package messager.generator.content;

import java.util.*;

import messager.common.*;
import messager.common.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mail의 header를 생성한다.
 */
public class MailHeader
{
	private static final Logger LOGGER = LogManager.getLogger(MailHeader.class.getName());
	
    private static final String lineSeparator = "\r\n";

    private static String mailerName;

    static {
        mailerName = System.getProperty("X-Mailer");
        if (mailerName == null || mailerName.length() == 0) {
        	mailerName = "AXON_v1.0";  //mailerName = "Postware@MasterPromotion_v3.0";
        }
    }

    private Message message;

    /** 리턴메일 을 위한 필드 */
    private String neoTaskIDField;

    /** 회신 메일 주소 필드 (Reply-To필드) */
    private String replyToField;

    /** 보내는 사람 필드 (From필드) */
    private String fromField;

    /**
     * 컨텐츠의 헤더 필드
     * Content-Type, Content-Transfer-Encoding(Multipart가 아닐 경우) 등이 포함된다.
     */
    private String contentHeader;

    /**
     * 날짜 필드 (Date필드)
     */
    private String dateField;

    /*
     * private int toNameColumn; private int toEmailColumn; private int
     * toUserIDColumn;
     */

    //메일 헤더의 인코딩 처리
    private MailHeaderEncoder headerEncoder;

    /**
     * MultiPart일 경우 Boundary로 사용된 String
     */
    private String boundary;

    /**
     * 객체를 생성한다.
     * type이 0보다 클 경우 parameter는 MultiPart의 Boundary로 사용되고
     * type이 0일 경우 paramter는 컨텐츠의 헤더이다.
     *
     * @param message Message의 발송 정보
     * @param parameter 컨텐츠의 헤더 또는 Boundary로 사용될 String
     * @param type 0이 아닐 경우는 Multipart를 의미 해서 paramter는 Boundary로 사용된다.
     */
    public MailHeader(Message message, String parameter, int type) {
        this.message = message;
        String charsetCode = message.charsetCode;
        String headerEncodingCode = message.headerEncodingCode;

        //String charsetName = CharsetTable.javaCharsetName(charsetCode);
        headerEncoder = new MailHeaderEncoder(charsetCode, headerEncodingCode);
        neoTaskIDField = createNeoTaskID(message); //X-MESSAGE-ID: L0D0Q0CYLS6GWDyOUi72Q0H3L36161
        setContentHeader(parameter, type);
    }

    /**
     * 리턴메일 분석을 위해 message 의 key를 생성한다.
     *
     * @param message
     * @return
     */
    private String createNeoTaskID(Message message) {
        StringBuffer buffer = new StringBuffer(); // 313_1_1_ADMIN_1_003
        buffer.append(Integer.toString(message.taskNo)).append('_')
            .append(Integer.toString(message.subTaskNo)).append('_')
            .append(Integer.toString(message.deptNo)).append('_')
            .append(message.userNo).append('_')
            .append(Integer.toString(message.campaignNo)).append('_')
            .append(message.campaignType);

        String neoTaskID = buffer.toString();
        buffer.setLength(0); //buffer를 비운다.
        buffer.append("X-MESSAGE-ID: ").append(NeoEncoder.encode(neoTaskID));
        return buffer.toString(); //X-MESSAGE-ID: L0D0Q0CYLS6GWDyOUi72Q0H3L36161
    }

    /**
     * int 형 데이타가 저장된 Object에서 int형 데이타를 얻는다.
     *
     * @param obj int형 데이타가 저장된 Integer객체
     * @return int형 데이타
     */
    private int getInt(Object obj) {
        int intValue = -1;

        if (obj != null) {
            Integer integer = (Integer) obj;
            intValue = integer.intValue();
        }
        return intValue;
    }

    /**
     * MultiPart일 경우 ContentType 설정(boundary 지정)
     */
    private void setContentHeader(String parameter, int type) {
        if (type > 0) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Content-Type: multipart/mixed;").append("\r\n")
                .append('\t').append("boundary=\"").append(parameter)
                .append('\"');
            contentHeader = buffer.toString();
        }
        else {
            contentHeader = parameter;
        }
    }

    /**
     * 대상자의 헤더를 생성한다.
     * 대상자의 정보가 필요하지 않은 고정된 필드는 미리 생성하여 추가하고
     * 대상자의 정보가 필요한 필드는 대상자의 정보를 이용하여 생성한다.
     *
     * @param receiver 대상자의 정보를 저장한 객체
     * @param buffer 헤더를 저장할 버퍼
     * @param toUser To필드의 이메일 주소와 대상자의 이름을 포함한다.
     */
    public void create(ReceiverInfo receiver, StringBuffer buffer, SendTo toUser)
        throws GeneratorException {
        if (replyToField == null) {
            replyToField = createReplyToField();
        }
        if (fromField == null) {
            fromField = createFromField();
        }
        if (dateField == null) {
            dateField = createDateField();
        }
        buffer.append(replyToField).append(lineSeparator);
        buffer.append(fromField).append(lineSeparator);
        addToField(receiver, toUser, buffer);
        buffer.append(lineSeparator);
        addSubject(receiver, buffer);
        buffer.append(lineSeparator);
        buffer.append(dateField).append(lineSeparator);
        buffer.append("MIME-Version: 1.0").append(lineSeparator);
        if (contentHeader != null) {
            buffer.append(contentHeader).append(lineSeparator);
        }
        buffer.append(neoTaskIDField).append(lineSeparator);

        buffer.append("X-USER-ID: ");
        if (toUser.id != null) {
            buffer.append(NeoEncoder.encode(toUser.id));
        }
        buffer.append(lineSeparator);

        buffer.append("X-USER-NM: ");
        if (toUser.name != null) {
            buffer.append(NeoEncoder.encode(toUser.name));
        }
        buffer.append(lineSeparator);

        buffer.append("X-Mailer: ").append(mailerName).append(lineSeparator);
    }

    /**
     * Subject 필드를 생성하여 추가한다.
     * Subject는 머지 가능한 필드이므로 Template 객체와 대상자의 정보가 저장된 ReceiverInfo 객체를 이용하여 생성한다.
     *
     * @param receiver 대상자의 정보가 저장된 객체
     * @param buffer 생성된 헤더를 저장할 버퍼
     * @throws GeneratorException 머지를 실패하거나 인코딩을 실패하였을 경우
     */
    private void addSubject(ReceiverInfo receiver, StringBuffer buffer)
        throws GeneratorException {
        buffer.append("Subject: ");
        Template subject = message.subject;
        try {
            buffer.append(headerEncoder.encodeText(subject.create(receiver)));
        }
        catch (MergeException ex) {
        	LOGGER.error(ex);
            throw new GeneratorException(ErrorCode.MERGE_ERROR, "Subject : "
                                         + ex.getMessage());
        }
        catch (java.io.UnsupportedEncodingException ex) {
        	LOGGER.error(ex);
            throw new GeneratorException(ErrorCode.ENCODING_ERROR,
                                         "UnsupportedException: Subject");
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            throw new GeneratorException(ErrorCode.CONTENT_INVALID, ex
                                         .getMessage());
        }
    }

    /**
     * Date 필드를 생성한다.
     *
     * @return Date 필드
     */
    private String createDateField() {
        Date date = new Date();
        MailDateFormat mailDateFormat = new MailDateFormat();
        StringBuffer buffer = new StringBuffer();
        buffer.append("Date: ").append(mailDateFormat.format(date));
        return buffer.toString();
    }

    /**
     * 헤더를 저장할 버퍼에 대상자의 정보로 To 헤더 필드를 완성한다.
     *
     * @param receiver 대상자의 정보가 저장된 ReceiverInfo 객체
     * @param toUser 대상자의 발송 정보가 저장된 SendTo 객체
     * @param buffer 헤더가 저장될 버퍼
     * @throws GeneratorException 인코딩 실패시
     */
    private void addToField(ReceiverInfo receiver, SendTo toUser,
                            StringBuffer buffer)
        throws GeneratorException {
        try {
            /*******************************************
             * 구버전의 TO NAME 헤더 생성
             * 사용 하지 않음
             * writed by 오범석
                String toName = toUser.name;
                String suffix = message.toNameSuffix;
                Template nameTemplate = message.toName;
                if (nameTemplate != null) {
                 //네임 머지 템플릿이 존재하면 템플릿에서 대상자 명을 생성한다.
                 toName = nameTemplate.create(receiver);
                }
                buffer.append("To: ").append(
                  headerEncoder.encodeText(toName + message.toNameSuffix))
                  .append(' ').append('<').append(toUser.email).append('>');
             *********************************************/
            String toName = toUser.name;
            Template nameTemplate = message.toName;
            if (nameTemplate != null) {
                //네임 머지 템플릿이 존재하면 템플릿에서 대상자 명을 생성한다.
                toName = nameTemplate.create(receiver);
            }
            buffer.append("To: ").append(
                headerEncoder.encodeText(toName)).append(' ').append('<').append(toUser.email).append('>');

        }
        catch (java.io.UnsupportedEncodingException ex) {
        	LOGGER.error(ex);
            throw new GeneratorException(ErrorCode.ENCODING_ERROR, ex
                                         .getMessage());
        }
        catch (MergeException ex) {
        	LOGGER.error(ex);
            throw new GeneratorException(ErrorCode.MERGE_ERROR, ex.getMessage());
        }
        catch (Exception ex) {
        	LOGGER.error(ex);
            throw new GeneratorException(ErrorCode.CONTENT_INVALID, ex
                                         .getMessage());
        }
    }

    /**
     * 헤더 필드명이 포함된 보내는 사람에 대한 헤더 필드를 얻는다.
     *
     * @return From 헤더에 대한 필드명이 포함된 헤더 필드
     * @throws GeneratorException 컨텐츠의 문자셋과 Name의 문자셋이 달라서 인코딩이 실패할 경우
     */
    private String createFromField()
        throws GeneratorException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("From: ");
        if (message.fromName != null) {
            try {
                buffer.append(headerEncoder.encodeText(message.fromName));
            }
            catch (java.io.UnsupportedEncodingException ex) {
            	LOGGER.error(ex);
                throw new GeneratorException(ErrorCode.ENCODING_ERROR,
                                             "UnsupportedException: From");
            }
        }
        if (message.fromEmail != null) {
            buffer.append(' ').append('<').append(message.fromEmail)
                .append('>');
        }
        return buffer.toString();
    }

    /**
     * 헤더 필드명이 포함된 회신 메일 필드를 얻는다.
     *
     * @return 헤더에 포함될 회신 메일 필드
     */
    private String createReplyToField() {
        String field = null;
        if (message.replyTo != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Reply-To: ").append(message.replyTo);
            field = buffer.toString();
        }
        return field;
    }
}
