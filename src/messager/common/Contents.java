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
public final class Contents implements java.io.Serializable {
	/** Main  컨텐츠 */
	private TemplateContent main;

	/** 머지가 필요한 첨부파일 리스트 */
	private ArrayList templateList;

	/** 머지가 필요하지 않은 첨부 파일 리스트 */
	private ArrayList fileList;

	/**
	 * Contents 객체를 생성한다.
	 * 
	 * @param main  
	 * @param templateList 첨부파일중 머지가 필요한 컨텐츠의 리스트
	 * @param fileList 첨부 파일중 머지가 필요하지 않은 컨텐츠의 리스트
	 */
	public Contents(TemplateContent main, ArrayList templateList,
			ArrayList fileList) {
		this.main = main;
		this.templateList = templateList;
		this.fileList = fileList;
	}

	/**
	 * 컨텐츠를 리턴한다.
	 * 
	 * @return 컨텐츠를 나타내는 TemplateContent
	 */
	public TemplateContent getMainContent() {
		return main;
	}

	/**
	 * 머지가 필요하지 않은 일반 첨부파일들의 리스트를 얻는다.
	 * 첨부파일은 FileContent 객체의 형태로 base64로 인코딩이 되어진 상태이다.
	 * 
	 * @return FileContent 객체들의 리스트
	 */
	public ArrayList getFileList() {
		return fileList;
	}

	/**
	 * 첨부 파일 리스트중 머지가 필요한 컨텐츠들을 나타내는 <br>
	 * TemplateContent 객체의 리스트를 얻는다.
	 * 
	 * @return TemplateContent 객체들을 포함하는 ArrayList
	 */
	public ArrayList getTemplateList() {
		return templateList;
	}

	/**
	 * 컨텐츠에 첨부파일이 포함되었나 확인한다.
	 * 
	 * @return 첨부 파일이 포함 되었으면 true
	 */
	public boolean isMultipart() {
		boolean isMultiPart = false;
		if ((fileList != null && fileList.size() > 0)
				|| (templateList != null && templateList.size() > 0)) {
			isMultiPart = true;
		}
		return isMultiPart;
	}
}

