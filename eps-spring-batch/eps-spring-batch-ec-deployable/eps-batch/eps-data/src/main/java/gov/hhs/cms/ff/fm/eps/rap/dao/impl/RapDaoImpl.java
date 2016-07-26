package gov.hhs.cms.ff.fm.eps.rap.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.rap.dao.RapDao;
import gov.hhs.cms.ff.fm.eps.rap.dao.mappers.PolicyPaymentsRowMapper;
import gov.hhs.cms.ff.fm.eps.rap.dao.mappers.PolicyPremiumRowMapper;
import gov.hhs.cms.ff.fm.eps.rap.dao.mappers.UserFeeRateRowMapper;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDetailDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the DAO implementation for the Data Access methods for RAP
 * @author prasad.ghanta
 *
 */
@SuppressWarnings("rawtypes")
public class RapDaoImpl extends GenericEpsDao implements RapDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(RapConstants.RAP_LOGGER);
     
    private String selectPolicyPremiums;
    private String selectPolicyPmtTrans;
    private String selectIssuerRates;
	
	/**
	 * Returns next sequence value of PolicyPaymentTransSeq
	 * @return Long PolicyPaymentTransSeq
	 */
	public Long getPolicyPaymentTransNextSeq() {
		return sequenceHelper.nextSequenceId("POLICYPAYMENTTRANSSEQ");
	}
    
    /**
     * Returns list of available payments for the policy version
     * @param policyVersionId
     * @return List<PolicyPremium>
     */
    public List<PolicyPremium> retrievePolicyPremiums(Long policyVersionId) {
        
        LOG.debug("Sql selectPolicyPremiums: "+ selectPolicyPremiums);
        Object[] args = {policyVersionId};
         
        List<PolicyPremium> list = jdbcTemplate.query(selectPolicyPremiums, args, new PolicyPremiumRowMapper());
        
        return list;
    }
    
    /**
     * returns list of available payments for the policy version
     * @param policyVersion
     * @return List<PolicyPaymentTransDTO>
     */
    public List<PolicyPaymentTransDTO> retrievePmtTransactions(PolicyDataDTO policyVersion) {
        
        LOG.debug("Sql selectPolicyPmtTrans: "+ selectPolicyPmtTrans);
        Object[] args = {policyVersion.getSubscriberStateCd(), policyVersion.getExchangePolicyId(), policyVersion.getIssuerHiosId()};
         
        List<PolicyPaymentTransDTO> list = jdbcTemplate.query(selectPolicyPmtTrans, args, new PolicyPaymentsRowMapper());
        
        return list;
    }

    /**
     * Method to retrieve policy payment data for the given policy version
     * 
     * @param policyVersion
     * @return policyDetail
     */
	@Override
	public PolicyDetailDTO retrievePolicyPaymentData(PolicyDataDTO policyVersion) {
		LOG.info("ENTER retrievePolicyPaymentData ", policyVersion);
		
		PolicyDetailDTO policyDetail = new PolicyDetailDTO();
		policyDetail.setPolicyPremiums(retrievePolicyPremiums(policyVersion.getPolicyVersionId()));
		policyDetail.setPolicyPayments(retrievePmtTransactions(policyVersion));
		
		LOG.info("EXIT retrievePolicyPaymentData ", policyDetail);
		return policyDetail;
	}

	/**
	 * Get user fee rate for all states for the given year and asOfDate
	 * @param asOfDate
	 * @param year
	 * @return issuerUserFeeRates
	 */
	public List<IssuerUserFeeRate> getUserFeeRateForAllStates(DateTime asOfDate, String year) {
		LOG.debug("Sql selectIssuerRates: "+ selectIssuerRates);
		
		Object[] args = {year, DataCommonUtil.convertToDate(asOfDate)};
		List<IssuerUserFeeRate> issuerUserFeeRates = jdbcTemplate.query(selectIssuerRates, args, new UserFeeRateRowMapper());
		
		return issuerUserFeeRates;
	}
	
	/**
	 * @param selectPolicyPmtTrans the selectPolicyPmtTrans to set
	 */
	public void setSelectPolicyPmtTrans(String selectPolicyPmtTrans) {
		this.selectPolicyPmtTrans = selectPolicyPmtTrans;
	}

	/**
	 * @param selectPolicyPremiums the selectPolicyPremiums to set
	 */
	public void setSelectPolicyPremiums(String selectPolicyPremiums) {
		this.selectPolicyPremiums = selectPolicyPremiums;
	}

	/**
	 * @param selectIssuerRates the selectIssuerRates to set
	 */
	public void setSelectIssuerRates(String selectIssuerRates) {
		this.selectIssuerRates = selectIssuerRates;
	}
    
}
