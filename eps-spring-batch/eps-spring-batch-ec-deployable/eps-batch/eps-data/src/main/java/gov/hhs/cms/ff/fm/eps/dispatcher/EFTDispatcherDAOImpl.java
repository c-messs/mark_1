package gov.hhs.cms.ff.fm.eps.dispatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Interface implementation for all the dispatcher specific database calls.
 *
 * @author pankaj.samayam
 */
public class EFTDispatcherDAOImpl implements EFTDispatcherDAO {
	
	/** The logger. */
	private Logger logger = org.slf4j.LoggerFactory
			.getLogger(EFTDispatcherDAOImpl.class);

	/** The jdbc template. */
	private JdbcTemplate jdbcTemplate;
	
	/** The ecm query props. */
	private Properties ecmQueryProps;
	
	/** The conn. */
	private Connection  conn;
	
	/**
	 * Gets the physical document identifier.
	 *
	 * @return the physical document identifier
	 */
	/*
	 * Method Implementation to get the current Value for
	 * PhysicalDocumentIdentifier in PhysicalDocument table from EPS
	 * 
	 * gov.hhs.cms.ff.fm.eps.ecm.dispatcher.EFTDispatcherDAO#
	 * getPhysicalDocumentIdentifier
	 */
	private long getPhysicalDocumentIdentifier() {
		String physicalDocumentIdentifier = String
				.format(ecmQueryProps
						.getProperty("dispatcher.physicalDocument.physcialDocumentIdentifier"));

		long physcialDocID = getJdbcTemplate().queryForObject(
				physicalDocumentIdentifier, Long.class);

		return physcialDocID;
	}

	/**
	 * Method Implementation to insert Records into PhysicalDocument table in
	 * EPS
	 * 
	 * gov.hhs.cms.ff.fm.eps.ecm.dispatcher.EFTDispatcherDAO#
	 * insertPhysicalDocument
	 *
	 * @param physicaldocument the physicaldocument
	 * @return the long
	 */
	@Override
	public long insertPhysicalDocument(PhysicalDocument physicaldocument){
		String insertPhysicalDocument = String.format(ecmQueryProps
				.getProperty("dispatcher.physicalDocument.insert"));

		logger.info("inserting PhyscialDocument record to Database");
		
		long physcialDocID = getPhysicalDocumentIdentifier();
		
		try {
			Timestamp physicalDocDateTime = new Timestamp(physicaldocument.getPhysicalDocumentDateTime().getMillis());
			String postalCode = null;
			if ((postalCode = physicaldocument.getStatePostalCode()) == null) {
				postalCode = " ";
			}
			
			Object[] inputValues = {
					physcialDocID,
					physicalDocDateTime,
					physicaldocument.getPhysicalDocumentByteArray(),
					physicaldocument.getPhysicalDocumentTypeCd(),
					physicaldocument.getServerEnvironmentTypeCd(),
					physicaldocument.getPhysicalDocumentInvalidCndInd(),
					physicaldocument.getIssuerHiosIdentifier(), postalCode,
					physicaldocument.getPhysicalDocumentApprvdInd(),
					physicaldocument.getPhysicalDcmntDsptchTypeCd(),
					physicaldocument.getTargetEFTApplicationTypeCd(),
					physicaldocument.getPhysicalDocumentFileName() 
			};
			
			jdbcTemplate.update(insertPhysicalDocument,	inputValues);
		} catch (Exception e) {
			logger.error("Exceptioin from insertPhysicalDocument--> " + e);
		}
		return physcialDocID;
	}
			

	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.pp.data.dispatcher.EFTDispatcherDAO#insertsDispatchedDocuments(java.util.List)
	 */
	@Override
	public void insertsDispatchedDocuments(List<DispatchedPhysicalDocument> dispatchedPhysicalDocuments) {
		String insertdisPhysicalDoc = String.format(ecmQueryProps
				.getProperty("dispatcher.dispatchedPhysicalDocument.insert"));
		logger.info("inserting DispatchedPhysicalDocument record to Database");

		List<DispatchedPhysicalDocument> dipatchedDocuments = new ArrayList<DispatchedPhysicalDocument>();
		dipatchedDocuments.addAll(dispatchedPhysicalDocuments);
		List<Object[]> allValues = new ArrayList<Object[]>();

		for (DispatchedPhysicalDocument dispatchDoc : dipatchedDocuments) {
			Object[] insertValues = {
					dispatchDoc.getPhysicalDocumentIdentifier(),
					dispatchDoc.getDispachedPhysicalDcmntFileNm(),
					dispatchDoc.getFailedDispatchInd() 
			};
			allValues.add(insertValues);
		}

		jdbcTemplate.batchUpdate(insertdisPhysicalDoc, allValues);
	}

	/**
	 * Method Implementation to retrieve Records from DispatchRoutingMap table
	 * in EPS
	 * 
	 * gov.hhs.cms.ff.fm.eps.ecm.dispatcher.EFTDispatcherDAO#getListofFileNames
	 *
	 * @param physicalDocumentTypeCd the physical document type cd
	 * @param serverEnvironmentTypeCd the server environment type cd
	 * @return the listof file names
	 */
	@Override
	public List<Map<String, Object>> getListofFileNames(
			String physicalDocumentTypeCd, String serverEnvironmentTypeCd) {
		List<Map<String, Object>> fileNamesMap = new ArrayList<Map<String, Object>>();

		String listofFileNames = String.format(ecmQueryProps
				.getProperty("dispatcher.dispatchRoutingMap.select"));
		fileNamesMap.addAll(getJdbcTemplate().queryForList(
				listofFileNames, physicalDocumentTypeCd, serverEnvironmentTypeCd));

		return fileNamesMap;
	}
	
	/**
	 * Gets the ecm query props.
	 *
	 * @return ecmQueryProps
	 */
	public Properties getEcmQueryProps() {
		return ecmQueryProps;
	}

	/**
	 * Sets the ecm query props.
	 *
	 * @param ecmQueryProps            the ecmQueryProps to set
	 */
	public void setEcmQueryProps(Properties ecmQueryProps) {
		this.ecmQueryProps = ecmQueryProps;
	}

	/**
	 * Sets the data source.
	 *
	 * @param dataSource the new data source
	 */
	public void setDataSource(DataSource dataSource) {
		this.setJdbcTemplate(new JdbcTemplate(dataSource));
	}

	/**
	 * Gets the jdbc template.
	 *
	 * @return the jdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {

		return jdbcTemplate;
	}

	/**
	 * Sets the jdbc template.
	 *
	 * @param jdbcTemplate            the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Gets the conn.
	 *
	 * @return the conn
	 */
	public Connection getConn() {
		try {
			conn  = jdbcTemplate.getDataSource().getConnection();
		} catch (SQLException e) {
			logger.info("returning connection");
		}
		return conn;
	}

	/**
	 * Sets the conn.
	 *
	 * @param conn the conn to set
	 */
	public void setConn(Connection conn) {
		this.conn = conn;
	}
}