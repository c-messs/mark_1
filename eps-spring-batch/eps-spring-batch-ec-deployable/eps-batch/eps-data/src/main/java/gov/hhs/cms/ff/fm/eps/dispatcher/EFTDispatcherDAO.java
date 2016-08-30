package gov.hhs.cms.ff.fm.eps.dispatcher;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interface for all the dispatcher specific database calls.
 *
 * @author pankaj.samayam
 */
public interface EFTDispatcherDAO {

	/**
	 * Insert physical document.
	 *
	 * @param physicaldocument the physicaldocument
	 * @return the long
	 * @throws SQLException the SQL exception
	 */
	public long insertPhysicalDocument(PhysicalDocument physicaldocument) throws SQLException;
	
	/**
	 * Gets the listof file names.
	 *
	 * @param physicalDocumentTypeCd the physical document type cd
	 * @param serverEnvironmentTypeCd the server environment type cd
	 * @return List<Map<String, Object>> fileNames
	 */
	public List<Map<String, Object>> getListofFileNames(String physicalDocumentTypeCd, String serverEnvironmentTypeCd);
	
	/**
	 * Inserts dispatched documents.
	 *
	 * @param dispatchedPhysicalDocuments the dispatched physical documents
	 * @throws SQLException the SQL exception
	 */
	public void insertsDispatchedDocuments(List<DispatchedPhysicalDocument>dispatchedPhysicalDocuments) throws SQLException;
	
}
