package com.pdf.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

public class HtmlToPdf {
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final String encKey = "htmltopdf$%^&*()";
	private static Logger logger = null;
	private String wkthmltopdf =  "";
	private String orientation = "Portrait";
	private String licensePath = "";
	private int marginTop = 0;
	private int marginBottom = 0;
	private int marginLeft = 0;
	private int marginRight = 0;
	
	public HtmlToPdf(String configPath){
		chHtmlToPdfConfig wkConfig = null;
		
		try{
			wkConfig = new chHtmlToPdfConfig(configPath);
			this.wkthmltopdf = wkConfig.get("chhtmltopdf.file.path");
			this.licensePath = wkConfig.get("chhtmltopdf.license.path");
			
			logger = new Logger();
			logger.setEnable(wkConfig.getBool("chhtmltopdf.log.enable"));
			logger.setLogDir(wkConfig.get("chhtmltopdf.log.dir"));
			logger.setLogName(wkConfig.get("chhtmltopdf.log.name"));
			
			
		}catch(Exception e){
			System.out.println("chHtmlToPdf init Error : "+e.getMessage());
		}
		

		wkConfig = null;
	}
	
	public void setOrientation(String orientation ){
		this.orientation = orientation;
	}
	public void setMarginTop(int marginTop) {
		this.marginTop = marginTop;
	}
	public void setMarginBottom(int marginBottom) {
		this.marginBottom = marginBottom;
	}
	public void setMarginLeft(int marginLeft) {
		this.marginLeft = marginLeft;
	}
	public void setMarginRight(int marginRight) {
		this.marginRight = marginRight;
	}

	public boolean htmlFileToPdf(String htmlFile,String pdfFile){
		boolean result = false;
		
		Pdf pdf = null;
		try{
			pdf = new Pdf();

			pdf.addPageFromFile(htmlFile);
			pdf.setWkhtmltopdfCommand(this.wkthmltopdf);
			
			pdf.addParam(new Param("--zoom","1.2"));
			pdf.addParam(new Param("--orientation",orientation));
			pdf.addParam(new Param("--margin-top",Integer.toString(marginTop)));
			pdf.addParam(new Param("--margin-bottom",Integer.toString(marginBottom)));
			pdf.addParam(new Param("--margin-left",Integer.toString(marginLeft)));
			pdf.addParam(new Param("--margin-right",Integer.toString(marginRight)));
			
			pdf.saveAs(pdfFile);
			
			result = true;
			logger.info("htmlFileToPdf", "htmlFile convert Success (%s -> %s)", htmlFile, pdfFile);
		}catch(IOException e){
			logger.error("htmlFileToPdf", "htmlFile Convert Pdf I/O Error : %s",  e.getMessage());
			result = false;
		}catch(Exception e){
			logger.error("htmlFileToPdf", "htmlFile Convert Pdf Error : %s",  e.getMessage());
			result = false;
		}finally{
			pdf = null;
		}
		
		return result;
	}

	public boolean htmlContentToPdf(String content,String pdfFile){
		boolean result = false;
		
		Pdf pdf = null;
		try{
			pdf = new Pdf();

			pdf.addPageFromString(content);
			pdf.setWkhtmltopdfCommand(this.wkthmltopdf);
			
			pdf.addParam(new Param("--zoom","1.2"));
			pdf.addParam(new Param("--orientation",orientation));
			pdf.addParam(new Param("--margin-top",Integer.toString(marginTop)));
			pdf.addParam(new Param("--margin-bottom",Integer.toString(marginBottom)));
			pdf.addParam(new Param("--margin-left",Integer.toString(marginLeft)));
			pdf.addParam(new Param("--margin-right",Integer.toString(marginRight)));
			
			pdf.saveAs(pdfFile);
			
			result = true;
			logger.info("htmlContentToPdf", "html Content convert Success  %s)", pdfFile);
		}catch(IOException e){
			logger.error("htmlContentToPdf", "htmlContentToPdf Convert Pdf I/O Error : %s",  e.getMessage());
			result = false;
		}catch(Exception e){
			logger.error("htmlContentToPdf", "htmlContentToPdf Convert Pdf Error : %s",  e.getMessage());
			result = false;
		}finally{
			pdf = null;
		}
		
		return result;
	}

	public boolean encryptPdf(String pdfFile, String encKey){
		boolean result = false;
		
		
		PDDocument doc = null;
		StandardProtectionPolicy spp = null;
		AccessPermission ap = null;
		try
		{
			doc = PDDocument.load(new File(pdfFile));

			int keyLength = 128;

			ap = new AccessPermission();
			ap.setCanPrint(true);

			spp = new StandardProtectionPolicy(encKey, encKey, ap);
			spp.setEncryptionKeyLength(keyLength);
			spp.setPermissions(ap);
			doc.protect(spp);

			doc.save(pdfFile);
			doc.close();
			
			result = true;
			logger.info("encryptPdf","pdfFile Encrypt Success(%s)", pdfFile);
			
		}catch (InvalidPasswordException e) { 
			logger.error("encryptPdf","already PDF file is encrypted  : "+e.getMessage()); 
		}catch(IOException e){
			logger.error("encryptPdf", "pdfFile encrypt I/O Error(%s) : %s", pdfFile, e.getMessage());			
			result = false;
		}catch(Exception e){
			logger.error("encryptPdf", "pdfFile encrypt Error(%s) : %s", pdfFile, e.getMessage());
			result = false;
		}
		finally
		{
			if(doc != null){try {doc.close();} catch (IOException e) {}}
		}
		
		return result;
	}

	public boolean htmlFileToEncryptPdf(String htmlFile, String pdfFile, String encKey){
		boolean result = false;
		
		PDDocument doc = null;
		StandardProtectionPolicy spp = null;
		AccessPermission ap = null;
		
		Pdf pdf = null;
		try{
			pdf = new Pdf();

			pdf.addPageFromFile(htmlFile);
			pdf.setWkhtmltopdfCommand(this.wkthmltopdf);
			
			pdf.addParam(new Param("--zoom","1.2"));
			pdf.addParam(new Param("--orientation",orientation));
			pdf.addParam(new Param("--margin-top",Integer.toString(marginTop)));
			pdf.addParam(new Param("--margin-bottom",Integer.toString(marginBottom)));
			pdf.addParam(new Param("--margin-left",Integer.toString(marginLeft)));
			pdf.addParam(new Param("--margin-right",Integer.toString(marginRight)));
			
			pdf.saveAs(pdfFile);
			
			doc = PDDocument.load(new File(pdfFile));

			int keyLength = 128;

			ap = new AccessPermission();
			ap.setCanPrint(true);

			spp = new StandardProtectionPolicy(encKey, encKey, ap);
			spp.setEncryptionKeyLength(keyLength);
			spp.setPermissions(ap);
			doc.protect(spp);

			doc.save(pdfFile);
			doc.close();
						
			result = true;
			logger.info("htmlFileToEncryptPdf", "htmlFile Encrypt convert Success (%s -> %s)", htmlFile, pdfFile);
		}catch (InvalidPasswordException e) { 
			logger.error("htmlFileToEncryptPdf","already PDF file is encrypted  : "+e.getMessage()); 
		}catch(IOException e){
			logger.error("htmlFileToEncryptPdf", "htmlFile Encrypt Convert Pdf I/O Error : %s",  e.getMessage());
			result = false;
		}catch(Exception e){
			logger.error("htmlFileToEncryptPdf", "htmlFile Encrypt Convert Pdf Error : %s",  e.getMessage());
			result = false;
		}finally{
			pdf = null;
		}
		
		return result;
	}
	
	public boolean htmlContentToEncryptPdf(String content, String pdfFile, String encKey){
		boolean result = false;
		
		PDDocument doc = null;
		StandardProtectionPolicy spp = null;
		AccessPermission ap = null;
		
		Pdf pdf = null;
		try{
			pdf = new Pdf();

			pdf.addPageFromString(content);
			pdf.setWkhtmltopdfCommand(this.wkthmltopdf);
			
			pdf.addParam(new Param("--zoom","1.2"));
			pdf.addParam(new Param("--orientation",orientation));
			pdf.addParam(new Param("--margin-top",Integer.toString(marginTop)));
			pdf.addParam(new Param("--margin-bottom",Integer.toString(marginBottom)));
			pdf.addParam(new Param("--margin-left",Integer.toString(marginLeft)));
			pdf.addParam(new Param("--margin-right",Integer.toString(marginRight)));
			
			pdf.saveAs(pdfFile);
			
			doc = PDDocument.load(new File(pdfFile));

			int keyLength = 128;

			ap = new AccessPermission();
			ap.setCanPrint(true);

			spp = new StandardProtectionPolicy(encKey, encKey, ap);
			spp.setEncryptionKeyLength(keyLength);
			spp.setPermissions(ap);
			doc.protect(spp);

			doc.save(pdfFile);
			doc.close();
						
			result = true;
			logger.info("htmlContentToEncryptPdf", "html Content Encrypt convert Success (%s)", pdfFile);
		}catch (InvalidPasswordException e) { 
			logger.error("htmlContentToEncryptPdf","already PDF file is encrypted  : "+e.getMessage()); 
		}catch(IOException e){
			logger.error("htmlContentToEncryptPdf", "htmlFile Encrypt Convert Pdf I/O Error : %s",  e.getMessage());
			result = false;
		}catch(Exception e){
			logger.error("htmlContentToEncryptPdf", "htmlFile Encrypt Convert Pdf Error : %s",  e.getMessage());
			result = false;
		}finally{
			pdf = null;
		}
		
		return result;
	}
	
	public static void main(String[] args) throws InterruptedException 
	{
		HtmlToPdf pdfConvert = new HtmlToPdf("D:\\Develop\\프로젝트\\htmltopdf\\산출물\\properties\\chhtmltopdf.properties");
		
		String content = "<html><body>TEST123</body></html>";
		String htmlFile = "D:\\Develop\\프로젝트\\htmltopdf\\산출물\\sample\\input\\index.html";
		String pdfFile = "D:\\Develop\\프로젝트\\htmltopdf\\산출물\\sample\\output\\index.pdf";
		String key = "111";

		pdfConvert.setMarginTop(10);
		pdfConvert.setMarginBottom(10);
		pdfConvert.setMarginLeft(10);
		pdfConvert.setMarginRight(10);
		
		pdfConvert.htmlContentToPdf(content, pdfFile);
		
	}
	
}

