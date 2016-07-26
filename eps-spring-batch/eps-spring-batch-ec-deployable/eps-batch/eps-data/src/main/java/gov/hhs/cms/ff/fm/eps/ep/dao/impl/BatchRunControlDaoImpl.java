package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.BatchRunControlDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * This class is the DAO implementation for the Data Access methods for BatchRunControlDao
 * 
 * @author girish.padmanabhan
 */
@SuppressWarnings("rawtypes")
public class BatchRunControlDaoImpl extends GenericEpsDao implements BatchRunControlDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(BatchRunControlDaoImpl.class);

    private String selectPreAuditIngestStatus;
    
	/**
	 * 
	 */
    @Override
    public String getPreAuditExtractStatus() {
        
        LOG.debug("Sql selectPreAuditIngestStatus: "+ selectPreAuditIngestStatus);
         
        String preAuditExtractCompletionInd = null;
        
        try {
        	preAuditExtractCompletionInd = jdbcTemplate.queryForObject(selectPreAuditIngestStatus, String.class);
        	
        } catch (EmptyResultDataAccessException e) {
        	LOG.debug("No record found in batchRunControl table when checking for Last PreAudit Extract Indicator");
        }
        
        return preAuditExtractCompletionInd;
    }

    /**
	 * @param selectPreAuditIngestStatus the selectPreAuditIngestStatus to set
	 */
	public void setSelectPreAuditIngestStatus(String selectPreAuditIngestStatus) {
		this.selectPreAuditIngestStatus = selectPreAuditIngestStatus;
	}

    
}
