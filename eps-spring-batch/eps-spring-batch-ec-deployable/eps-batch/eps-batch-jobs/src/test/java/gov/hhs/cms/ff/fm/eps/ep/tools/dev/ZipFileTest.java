package gov.hhs.cms.ff.fm.eps.ep.tools.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipFileTest {


	private static String PATH_ZIPPED = "/Prototype834files/FFMToEPS/zipped";
	private static String PATH_UNZIPPED = "/Prototype834files/FFMToEPS/unzipped";


	public static void main(String[] args) throws IOException {
		unzip();
		gunzip();
	}


	private static void unzip() throws IOException {
		System.out.println("zip files..");
    	
    	byte[] buffer = new byte[1024];
    	
    	File dirZipped = new File(PATH_ZIPPED);
    	File[] files = dirZipped.listFiles();
    	
    	for (File file : files) {
    		
    		if(!isValidZip(file)) {
    			System.out.println("Not valid zip file : "+ file.getName());
    		} else {
    		
	    	ZipInputStream zis = 
	        		new ZipInputStream(new FileInputStream(file));
	    	ZipEntry ze; //= zis.getNextEntry();
	    	
	    	System.out.println("file : "+ file.getAbsoluteFile());
	    	
	    	while((ze=zis.getNextEntry()) !=null){
				
	            File newFile = new File(PATH_UNZIPPED + File.separator + file.getName()+"_unzipped.T");
	                 
	            System.out.println("file unzip : "+ newFile.getAbsoluteFile());
	                 
	             //create all non exists folders
	             //else you will hit FileNotFoundException for compressed folder
	             new File(newFile.getParent()).mkdirs();
	               
	             FileOutputStream fos = new FileOutputStream(newFile);             
	
	             int len;
	             while ((len = zis.read(buffer)) > 0) {
	        		fos.write(buffer, 0, len);
	             }
	             fos.close();   
	     	}
	     	
	        zis.closeEntry();
	     	zis.close();
    		}
    	}
        
     	System.out.println("Unzip Done");
	}
	

	
	private static void gunzip() throws IOException {
		
		System.out.println("Gzip..");
		
		byte[] buffer = new byte[1024];
		
		FileInputStream fis = new FileInputStream(PATH_ZIPPED + File.separator + "sorted.new_or_modified_ipps_20160523_104903.T");
        GZIPInputStream gs = new GZIPInputStream(fis);
        
        FileOutputStream fileOutputStream = new FileOutputStream(PATH_UNZIPPED + File.separator + "sorted.new_or_modified_ipps_20160523_104903_unzipped.T");

        int bytesRead;
        while ((bytesRead = gs.read(buffer)) > 0) {
        	fileOutputStream.write(buffer, 0, bytesRead);
        }
        
        gs.close();
        fileOutputStream.close();
        
        System.out.println("GUnzip Done");
		
	}
	
	 static boolean isValidZip(final File file) throws IOException {
		    ZipFile zipfile = null;
		    try {
		        zipfile = new ZipFile(file);
		        return true;
		    } catch (ZipException e) {
		    	System.out.println(e.getLocalizedMessage());
		        return false;
		    } finally {
		        try {
		            if (zipfile != null) {
		                zipfile.close();
		                zipfile = null;
		            }
		        } catch (IOException e) {
		        }
		    }
		}

}
