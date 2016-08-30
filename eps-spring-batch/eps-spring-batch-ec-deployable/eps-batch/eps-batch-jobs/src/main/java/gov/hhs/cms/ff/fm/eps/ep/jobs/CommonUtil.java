package gov.hhs.cms.ff.fm.eps.ep.jobs;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * @author eps
 *
 */
public class CommonUtil {

	/**
	 * This method returns list of ids as comma seperated string. for ex : If
	 * the list has strings '[test1,test2,test3,test4,test5]"; then it returns
	 * as 'test1','test2','test3','test4','test5' for other types :If list has
	 * "[72, 99, 58, 48, 60, 100]"; it returns as "72, 99, 58, 48, 60, 100";
	 * (replaces '[' ']')
	 * 
	 * @param issuerLevelTransactionIDList
	 * @return
	 */
	public static String buildListToString(List issuerLevelTransactionIDList) {
		String listAsString = "";

		if ((issuerLevelTransactionIDList != null)
				&& (!issuerLevelTransactionIDList.isEmpty())) {
			if (issuerLevelTransactionIDList.get(0).getClass().getSimpleName()
					.startsWith("String")) {
				listAsString = issuerLevelTransactionIDList.toString().replace(
						"[", "'");
				listAsString = listAsString.replace("]", "'");
				listAsString = listAsString.replaceAll("\\s*,\\s*", "','");
			} else {
				listAsString = issuerLevelTransactionIDList.toString().replace(
						"[", " ");
				listAsString = listAsString.replace("]", " ");
			}
		}
		return listAsString;
	}
	
	/**
	 * This method creates a list of files in the source directory.
	 *
	 * @param dir the dir
	 * @return List<File> - sorted by last modified date
	 */
	public static List<File> getFilesFromDir(File dir) {
		List<File> files = new ArrayList<File>();


		if (dir.isDirectory()) {
			//Create a Filename filter to identify filter files
			File[] dirFiles = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});
			if(ArrayUtils.isNotEmpty(dirFiles)) {
				Collections.addAll(files, dirFiles);
			}
		} else {
			throw new EnvironmentException("E9004: Service Access Failure, unexpected file type: Directory=" + dir);
		}
		
		//sort by last modified date
		if(CollectionUtils.isNotEmpty(files)) {
			files.sort(LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
		}
		
		return files;
	}
	
	/**
	 * Checks if originalStr is found in list of values (Note: Not case
	 * sensitive).
	 *
	 * @param originalStr String
	 * @param values String
	 * @return boolean
	 */	
	public static boolean isStringMatched(String originalStr, String... values) {

		if (StringUtils.isNotBlank(originalStr) && values != null) {
			for (String str : values) {
				if (originalStr.equalsIgnoreCase(str)) {
					return true;
				}
			}
		}

		return false;
	}

}
