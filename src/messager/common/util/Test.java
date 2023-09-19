package messager.common.util;

public class Test {

	

	public static void main(String args[]) throws Exception  {
		
		String kor_str = "�븳湲��뀒�뒪�듃";

		String[] ary = {"euc-kr","utf-8","iso-8859-1","ksc5601","x-windows-949"};

		for( int i =0 ; i < ary.length; i++){

		for(int j=0; j < ary.length ; j++){

		if( i != j){

		System.out.println( ary[i]+"=>"+ ary[j]+ " \r\n ==> " +new String(kor_str.getBytes(ary[i]),ary[j]));

		}

		}

		}



		

				
	}
}
