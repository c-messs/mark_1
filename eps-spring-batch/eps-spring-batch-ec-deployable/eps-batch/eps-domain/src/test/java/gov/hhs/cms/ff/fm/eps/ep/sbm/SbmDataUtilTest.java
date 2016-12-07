package gov.hhs.cms.ff.fm.eps.ep.sbm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;

public class SbmDataUtilTest {
	
	@Test
	public void test_hasWarningFromEPSChange_false_empty() {
		
		boolean expected = false;
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		boolean actual = SbmDataUtil.hasWarningFromEPSChange(policyDTO);
		assertEquals("hasWarnings", expected, actual);
	}
	
	@Test
	public void test_hasWarningFromEPSChange_false() {
		
		boolean expected = false;
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		
		SbmErrWarningLogDTO errWarnDTO1 = new SbmErrWarningLogDTO();
		errWarnDTO1.setErrorWarningTypeCd(SBMErrorWarningCode.ER_001.getCode());
		SbmErrWarningLogDTO errWarnDTO2 = new SbmErrWarningLogDTO();
		errWarnDTO1.setErrorWarningTypeCd(SBMErrorWarningCode.WR_001.getCode());
		SbmErrWarningLogDTO errWarnDTO3 = new SbmErrWarningLogDTO();
		errWarnDTO1.setErrorWarningTypeCd(SBMErrorWarningCode.WR_002.getCode());
		SbmErrWarningLogDTO errWarnDTO4 = new SbmErrWarningLogDTO();
		errWarnDTO1.setErrorWarningTypeCd(SBMErrorWarningCode.WR_003.getCode());
		
		policyDTO.getErrorList().add(errWarnDTO1);
		policyDTO.getErrorList().add(errWarnDTO2);
		policyDTO.getErrorList().add(errWarnDTO3);
		policyDTO.getErrorList().add(errWarnDTO4);
		
		boolean actual = SbmDataUtil.hasWarningFromEPSChange(policyDTO);
		assertEquals("hasWarnings", expected, actual);
	}
	
	@Test
	public void test_hasWarningFromEPSChange_true() {
		
		boolean expected = true;
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		
		SbmErrWarningLogDTO errWarnDTO1 = new SbmErrWarningLogDTO();
		errWarnDTO1.setErrorWarningTypeCd(SBMErrorWarningCode.ER_001.getCode());
		
		SbmErrWarningLogDTO errWarnDTO2 = new SbmErrWarningLogDTO();
		errWarnDTO2.setErrorWarningTypeCd(SBMErrorWarningCode.WR_007.getCode());
		
		policyDTO.getErrorList().add(errWarnDTO1);
		policyDTO.getErrorList().add(errWarnDTO2);
		
		boolean actual = SbmDataUtil.hasWarningFromEPSChange(policyDTO);
		assertEquals("hasWarnings", expected, actual);
	}
	

}
