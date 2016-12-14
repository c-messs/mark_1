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

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;

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
	 * This method creates a list of files in the source directory
	 * and filters out by Environment Code only if in PROD or PROD-R. 
	 * @param dir
	 * @param envCd
	 * @return
	 */
	public static List<File> getFilesFromDir(File dir, String envCd) {

		List<File> files = new ArrayList<File>();

		if (dir.isDirectory()) {
			//Create a Filename filter to identify filter files
			File[] dirFiles = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {

					boolean isFile = file.isFile();
					boolean isEnv = true;
					// Only filter if the envCd is passed in.
					if (envCd != null) {
						// Only filter files for PROD and PROD-R
						if (SBMConstants.FILE_ENV_CD_PROD.equals(envCd) || SBMConstants.FILE_ENV_CD_PROD_R.equals(envCd)) {
							// Typical fileName: SBMI.FEP0106ID.D161114.T124239968.R
							String[] tkns = StringUtils.split(file.getName(), ".");
							String envComponent = null;
							if (tkns.length >= 5) {
								envComponent = tkns[4];
							}
							if (envComponent != null) {
								isEnv = (envComponent.length() == 1 && envComponent.indexOf(envCd) == 0);
							}
						}
					}
					return isFile && isEnv;
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
	 * Get file with no filtering.
	 * @param dir
	 * @return
	 */
	public static List<File> getFilesFromDir(File dir) {

		return getFilesFromDir(dir, null);
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
