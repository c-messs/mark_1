package gov.hhs.cms.ff.fm.eps.ep.jobs;

import java.util.List;

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

}
