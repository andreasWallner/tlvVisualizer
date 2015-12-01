package at.innovative_solutions.tlv.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TLVTest.class,
	UtilsTest.class,
	IDTest.class,
	SimpleFormatterTest.class})
public final class AllTests {}
