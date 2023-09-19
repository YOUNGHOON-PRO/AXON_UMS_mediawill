package messager.common;

import java.util.ArrayList;

/** 
 * 컨텐츠들을 관리 저장한다.
 * 객체 직렬화를 통해 파일에 저장되거나 Center와 Generator의 통신을 통해 전달된다.
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class Contents2 implements java.io.Serializable {
	/** Main  컨텐츠 */
	private TemplateContent2 main2;

	/** 머지가 필요한 첨부파일 리스트 */
	private ArrayList templateList2;

	/** 머지가 필요하지 않은 첨부 파일 리스트 */
	private ArrayList fileList2;

	/**
	 * Contents 객체를 생성한다.
	 * 
	 * @param main  
	 * @param templateList 첨부파일중 머지가 필요한 컨텐츠의 리스트
	 * @param fileList 첨부 파일중 머지가 필요하지 않은 컨텐츠의 리스트
	 */
	public Contents2(TemplateContent2 main2, ArrayList templateList,
			ArrayList fileList) {
		this.main2 = main2;
		this.templateList2 = templateList;
		this.fileList2 = fileList;
	}

	/**
	 * 컨텐츠를 리턴한다.
	 * 
	 * @return 컨텐츠를 나타내는 TemplateContent
	 */
	public TemplateContent2 getMainContent() {
		return main2;
	}
	
	/**
	 * 컨텐츠를 리턴한다.
	 * 
	 * @return 컨텐츠를 나타내는 TemplateContent
	 */
	public TemplateContent2 getMainContent2() {
		return main2;
	}

	/**
	 * 머지가 필요하지 않은 일반 첨부파일들의 리스트를 얻는다.
	 * 첨부파일은 FileContent 객체의 형태로 base64로 인코딩이 되어진 상태이다.
	 * 
	 * @return FileContent 객체들의 리스트
	 */
	public ArrayList getFileList() {
		return fileList2;
	}

	/**
	 * 첨부 파일 리스트중 머지가 필요한 컨텐츠들을 나타내는 <br>
	 * TemplateContent 객체의 리스트를 얻는다.
	 * 
	 * @return TemplateContent 객체들을 포함하는 ArrayList
	 */
	public ArrayList getTemplateList() {
		return templateList2;
	}

	/**
	 * 컨텐츠에 첨부파일이 포함되었나 확인한다.
	 * 
	 * @return 첨부 파일이 포함 되었으면 true
	 */
	public boolean isMultipart() {
		boolean isMultiPart = false;
		if ((fileList2 != null && fileList2.size() > 0)
				|| (templateList2 != null && templateList2.size() > 0)) {
			isMultiPart = true;
		}
		return isMultiPart;
	}
}

