package utils;

import java.io.FileInputStream;
import java.io.IOException;


import org.apache.commons.io.IOUtils;


public class Utils {
	public static String readFile(String filePath) throws IOException {
		FileInputStream fs = new FileInputStream(filePath);
		
		//InputStream inStream = new InputStre
		
		return IOUtils.toString(fs);
	}
	
	
	/*
	 *  //java 7
	 * 	//http://stackoverflow.com/a/326440
	public static String readFile(String path, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	 */
}
