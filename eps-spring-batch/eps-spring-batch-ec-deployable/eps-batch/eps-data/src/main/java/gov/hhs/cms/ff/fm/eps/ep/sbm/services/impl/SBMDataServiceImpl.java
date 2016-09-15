package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmPolicyMemberVersionMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmPolicyVersionMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMDataService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmPolicyMatchService;

/**
 * @author j.radziewski
 *
 */
public class SBMDataServiceImpl implements SBMDataService {

	private final static Logger LOG = LoggerFactory.getLogger(SBMDataServiceImpl.class);

	protected JdbcTemplate jdbcTemplate;

	private SbmPolicyMatchService policyMatchService;
	
	private SbmPolicyVersionMapper policyVersionMapper;
	private SbmPolicyMemberVersionMapper pmvMapper;
	private SbmPolicyMemberVersionDao pmvDao;
	
	private String verifyQhpIdExists;
	private String getMetalLevelByQhpid;
	private String getCsrMultiplierByVariantAndMetal;
	private String getCsrCsrMultiplierByVariant;


	@Override
	public SBMPolicyDTO performPolicyMatch(SBMPolicyDTO inboundPolicyDTO) {

		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();

		PolicyVersionPO polVerPO = policyMatchService.findLatestPolicy(inboundPolicyDTO);

		if (polVerPO != null) {

			String stateCd = SbmDataUtil.getStateCd(inboundPolicyDTO.getFileInfo());

			Long policyVersionId = polVerPO.getPolicyVersionId();

			PolicyType policy = policyVersionMapper.mapEpsToSbm(polVerPO);

			List<SbmPolicyMemberVersionPO> pmvPOList = pmvDao.getPolicyMemberVersionsForPolicyMatch(stateCd, policyVersionId);
			List<PolicyMemberType> memberList = pmvMapper.mapEpsToSbm(pmvPOList);
			policy.getMemberInformation().addAll(memberList);

			epsPolicyDTO.setPolicyVersionId(policyVersionId);
			epsPolicyDTO.setPolicy(policy);	
		}

		return epsPolicyDTO;
	}


	@Override
	public boolean checkQhpIdExistsForPolicyYear(String qhpId, String planYear) {
		//Fetch QHP Id from INSRNCPLAN where
		//		hiosStandardCmptId  = qhpId 
		//		MarketYear = planYear
		//		InsrncMarketCoverageTypeCd = ‘1’

		return (jdbcTemplate.queryForObject(verifyQhpIdExists,
				new Object[] {qhpId, planYear}, Integer.class) > 0);
	}

	@Override
	public String getMetalLevelByQhpid(String qhpId, String planYear) {

		String metalLevel = null;
		try {
			metalLevel = jdbcTemplate.queryForObject(getMetalLevelByQhpid,
					new Object[] {qhpId, planYear}, String.class);

		} catch(EmptyResultDataAccessException e) {
			LOG.info("Metal Level not found for qhp {}, {}", qhpId, planYear);
		}
		return metalLevel;
	}

	@Override
	public BigDecimal getCsrMultiplierByVariantAndMetal(String variantID, String metal, String year) {
		BigDecimal multiplier = null;
		
		try {
			multiplier = jdbcTemplate.queryForObject(
					getCsrMultiplierByVariantAndMetal, new Object[] {variantID, metal, year}, BigDecimal.class);
		} catch(EmptyResultDataAccessException e) {
			LOG.info("Csr Multiplier not found for variant {}, metal level {}, year {}", variantID, metal, year);
		}
		return multiplier;
	}

	@Override
	public BigDecimal getCsrMultiplierByVariant(String variantID, String year) {
		BigDecimal multiplier = null;
		
		try {
			multiplier = jdbcTemplate.queryForObject(
				getCsrCsrMultiplierByVariant, new Object[] {variantID, year}, BigDecimal.class);
		
		} catch(EmptyResultDataAccessException e) {
			LOG.info("Csr Multiplier not found for variant {}, year {}", variantID, year);
		}
		return multiplier;
	}


	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param policyMatchService the policyMatchService to set
	 */
	public void setPolicyMatchService(SbmPolicyMatchService policyMatchService) {
		this.policyMatchService = policyMatchService;
	}

	/**
	 * @param policyVersionMapper the policyVersionMapper to set
	 */
	public void setPolicyVersionMapper(SbmPolicyVersionMapper policyVersionMapper) {
		this.policyVersionMapper = policyVersionMapper;
	}

	/**
	 * @param pmvMapper the pmvMapper to set
	 */
	public void setPmvMapper(SbmPolicyMemberVersionMapper pmvMapper) {
		this.pmvMapper = pmvMapper;
	}

	/**
	 * @param pmvDao the pmvDao to set
	 */
	public void setPmvDao(SbmPolicyMemberVersionDao pmvDao) {
		this.pmvDao = pmvDao;
	}

	/**
	 * @param verifyQhpIdExists the verifyQhpIdExists to set
	 */
	public void setVerifyQhpIdExists(String verifyQhpIdExists) {
		this.verifyQhpIdExists = verifyQhpIdExists;
	}

	/**
	 * @param getMetalLevelByQhpid the getMetalLevelByQhpid to set
	 */
	public void setGetMetalLevelByQhpid(String getMetalLevelByQhpid) {
		this.getMetalLevelByQhpid = getMetalLevelByQhpid;
	}

	/**
	 * @param getCsrMultiplierByVariantAndMetal the getCsrMultiplierByVariantAndMetal to set
	 */
	public void setGetCsrMultiplierByVariantAndMetal(String getCsrMultiplierByVariantAndMetal) {
		this.getCsrMultiplierByVariantAndMetal = getCsrMultiplierByVariantAndMetal;
	}

	/**
	 * @param getCsrCsrMultiplierByVariant the getCsrCsrMultiplierByVariant to set
	 */
	public void setGetCsrCsrMultiplierByVariant(String getCsrCsrMultiplierByVariant) {
		this.getCsrCsrMultiplierByVariant = getCsrCsrMultiplierByVariant;
	}

}
