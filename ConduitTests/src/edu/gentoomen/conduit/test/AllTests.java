package edu.gentoomen.conduit.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Our test suite
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(UseCasePlayMusicGuest.class);
		suite.addTestSuite(UserCasePlayMultipleMusicGuest.class);
		//$JUnit-END$
		return suite;
	}

}
