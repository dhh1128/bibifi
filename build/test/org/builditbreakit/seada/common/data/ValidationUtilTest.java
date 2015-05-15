package org.builditbreakit.seada.common.data;

import org.builditbreakit.seada.common.data.ValidationUtil;

import junit.framework.TestCase;

public class ValidationUtilTest extends TestCase {

	public void test_isAlpha() {
		for (char c = (char)0; c <= (char)1000; ++c) {
			boolean expected = (c < (char)128) ? Character.isAlphabetic(c) : false;
			assertEquals(expected, ValidationUtil.isAlpha(c));
		}
	}

	public void test_isDigit() {
		for (char c = (char)0; c <= (char)1000; ++c) {
			boolean expected = (c < (char)128) ? Character.isDigit(c) : false;
			assertEquals(expected, ValidationUtil.isDigit(c));
		}
	}

	public void test_assertValidUINT32() {
		ValidationUtil.assertValidUINT32(0, "x");
		ValidationUtil.assertValidUINT32(100, "x");
		ValidationUtil.assertValidUINT32(1024*1024, "x");
		
		try {
			ValidationUtil.assertValidUINT32(-1, "x");
			fail("Expected assertion to fail");
		} catch (IllegalArgumentException e) {
		}
		
		int n = 1024 * 1024 * 1024;
		ValidationUtil.assertValidUINT32(n - 1, "x");
		try {
			ValidationUtil.assertValidUINT32(n, "x");
			fail("Expected assertion to fail");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testLeadingZeros() {
		int n = Integer.parseInt("0000000000000000000000000000000000001024");
		assertEquals(1024L, n);
	}
	
	public void test_isSwitch() {
		assertTrue(ValidationUtil.isSwitch("-A"));
		assertTrue(ValidationUtil.isSwitch("-Z"));
		assertFalse(ValidationUtil.isSwitch("-0"));
		assertFalse(ValidationUtil.isSwitch("-n"));
		assertFalse(ValidationUtil.isSwitch("--A"));
		assertFalse(ValidationUtil.isSwitch("-Apple"));
		assertFalse(ValidationUtil.isSwitch("-APPLE"));
	}
	
}
