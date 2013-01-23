/**
 * This file is part of Niowire.
 *
 * Niowire is free software: you can redistribute it and/or modify it under the
 * terms of the Lesser GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Niowire is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more
 * details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Niowire. If not, see <http://www.gnu.org/licenses/>.
 */
package io.niowire.entities;

import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link NioObjectFactory}
 *
 * @author Trent Houliston
 */
public class InjectorTest
{

	/**
	 * This test ensures that the configuration and injection works as expected
	 * at the various access levels (checking that they can all be injected
	 * into)
	 */
	@Test(timeout = 1000)
	public void testBuildApplyAccessLevels()
	{
		//Create an object to test with
		TestClass obj = new TestClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(4);
		testData.put("superPrivate", "TEST_SUPER_PRIVATE");
		testData.put("superProtected", "TEST_SUPER_PROTECTED");
		testData.put("superDefault", "TEST_SUPER_DEFAULT");
		testData.put("superPublic", "TEST_SUPER_PUBLIC");

		//Build a configuration and configure the object
		Injector<TestClass> injector = new Injector<TestClass>(TestClass.class, testData);
		injector.inject(obj);

		//Check that all the fields were injected properly
		assertEquals("The private injection did not have the expected result", "TEST_SUPER_PRIVATE", obj.superPrivate);
		assertEquals("The protected injection did not have the expected result", "TEST_SUPER_PROTECTED", obj.superProtected);
		assertEquals("The default injection did not have the expected result", "TEST_SUPER_DEFAULT", obj.superDefault);
		assertEquals("The public injection did not have the expected result", "TEST_SUPER_PUBLIC", obj.superPublic);
	}

	/**
	 * Tests that injection follows the inheritance chain, injecting as it goes
	 * up
	 */
	@Test(timeout = 1000)
	public void testBuildApplySuperClass()
	{
		//Create an object to test with
		SubClass obj = new SubClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(4);
		testData.put("superPrivate", "TEST_SUPER_PRIVATE");
		testData.put("superProtected", "TEST_SUPER_PROTECTED");
		testData.put("superDefault", "TEST_SUPER_DEFAULT");
		testData.put("superPublic", "TEST_SUPER_PUBLIC");
		testData.put("subClassValue", "TEST_SUB_CLASS_VALUE");

		//Build a configuration and configure the object
		Injector<SubClass> injector = new Injector<SubClass>(SubClass.class, testData);
		injector.inject(obj);

		//Check that all the fields were injected properly (we have to cast to the super object to access the private member)
		assertEquals("The private injection did not have the expected result", "TEST_SUPER_PRIVATE", ((TestClass) obj).superPrivate);
		assertEquals("The protected injection did not have the expected result", "TEST_SUPER_PROTECTED", obj.superProtected);
		assertEquals("The default injection did not have the expected result", "TEST_SUPER_DEFAULT", obj.superDefault);
		assertEquals("The public injection did not have the expected result", "TEST_SUPER_PUBLIC", obj.superPublic);
		assertEquals("The subclass injection did not have the expected result", "TEST_SUB_CLASS_VALUE", obj.subClassValue);
	}

	/**
	 * Tests that if a configuration is missing, the value is left unchanged
	 * (uses the default)
	 */
	@Test(timeout = 1000)
	public void testIgnoreMissingConfigs()
	{
		//Create an object to test with
		TestClass obj = new TestClass();

		//Create some data with protected and public missing
		HashMap<String, String> testData = new HashMap<String, String>(4);
		testData.put("superPrivate", "TEST_SUPER_PRIVATE");
		testData.put("superDefault", "TEST_SUPER_DEFAULT");

		//Build a configuration and configure the object
		Injector<TestClass> injector = new Injector<TestClass>(TestClass.class, testData);
		injector.inject(obj);

		//Check that all the fields were injected properly and that public and protected were left unchanged
		assertEquals("The private injection did not have the expected result", "TEST_SUPER_PRIVATE", obj.superPrivate);
		assertEquals("The protected field was injected when it should not have been", "DEFAULT_SUPER_PROTECTED", obj.superProtected);
		assertEquals("The default injection did not have the expected result", "TEST_SUPER_DEFAULT", obj.superDefault);
		assertEquals("The public field was injected when it should not have been", "DEFAULT_SUPER_PUBLIC", obj.superPublic);
	}

	/**
	 * Tests that only fields which have the annotation are injected. (And
	 * included in this is that any configuration elements without an element to
	 * go into will be ignored)
	 */
	@Test(timeout = 1000)
	public void testAnnotationCheckConfigs()
	{
		//Create an object to test with
		TestClass obj = new TestClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(4);
		testData.put("ignoreMe", "TEST_IGNORE_ME");

		//Build a configuration and configure the object
		Injector<TestClass> injector = new Injector<TestClass>(TestClass.class, testData);
		injector.inject(obj);

		//Check that all the fields were injected properly
		assertEquals("The field without an annotation was injected into", "DEFAULT_IGNORE_ME", obj.ignoreMe);
	}

	/**
	 * Tests that parameters which use the {@link Named} annotation use that
	 * name for their source instead of the field name.
	 */
	@Test(timeout = 1000)
	public void testNamedParameters()
	{
		//Create an object to test with
		TestClass obj = new TestClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(4);
		testData.put("namedValue", "TEST_NAMED_VALUE");

		//Build a configuration and configure the object
		Injector<TestClass> injector = new Injector<TestClass>(TestClass.class, testData);
		injector.inject(obj);

		//Check that all the fields were injected properly
		assertEquals("The named field was not injected into", "TEST_NAMED_VALUE", obj.notNamedValue);
	}

	/**
	 * Tests that named parameters will not be set through their field name
	 */
	@Test(timeout = 1000)
	public void testNotNamedParameters()
	{
		//Create an object to test with
		TestClass obj = new TestClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(4);
		testData.put("notNamedValue", "TEST_NOT_NAMED_VALUE");

		//Build a configuration and configure the object
		Injector<TestClass> injector = new Injector<TestClass>(TestClass.class, testData);
		injector.inject(obj);

		//Check that all the fields were injected properly
		assertEquals("The named field was not injected into", "DEFAULT_NOT_NAMED_VALUE", obj.notNamedValue);
	}

	/**
	 * Test that methods with the {@link Initialize} annotation are run. Also
	 * tests that the injection operation is performed before the Init methods
	 */
	@Test(timeout = 1000)
	public void testInitMethods()
	{
		//Create an object to test with
		TestClass obj = new TestClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(1);
		testData.put("namedValue", "TEST_NOT_NAMED_VALUE");

		//Build a configuration and configure the object
		Injector<TestClass> injector = new Injector<TestClass>(TestClass.class, testData);
		injector.inject(obj);

		//Check that the init method ran properly
		assertEquals("The init method did not run", "TEST_NOT_NAMED_VALUE", obj.testInit);
	}

	/**
	 * Tests that private init methods are run (to test access permissions
	 */
	/**
	 * Test that methods with the {@link Initialize} annotation are run. Also
	 * tests that the injection operation is performed before the Init methods
	 */
	@Test(timeout = 1000)
	public void testPrivateInitMethods()
	{
		//Create an object to test with
		TestClass obj = new TestClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(1);
		testData.put("namedValue", "TEST_NOT_NAMED_VALUE");

		//Build a configuration and configure the object
		Injector<TestClass> injector = new Injector<TestClass>(TestClass.class, testData);
		injector.inject(obj);

		//Check that the init method ran properly
		assertEquals("The init method did not run", "TEST_NOT_NAMED_VALUE", obj.testPrivateInit);
	}

	/**
	 * Tests that when the sub class has another init method that both are run
	 * (also tests that multiple init methods will be run)
	 */
	@Test(timeout = 1000)
	public void testSuperClassInitMethods()
	{
		//Create an object to test with
		SubClass obj = new SubClass();

		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(1);
		testData.put("namedValue", "TEST_NOT_NAMED_VALUE");

		//Build a configuration and configure the object
		Injector<SubClass> injector = new Injector<SubClass>(SubClass.class, testData);
		injector.inject(obj);

		//Check that the init method ran properly
		assertEquals("The superclass init method did not run", "TEST_NOT_NAMED_VALUE", obj.testInit);
		assertEquals("The subclass init method did not run", "TEST_NOT_NAMED_VALUE", obj.subClassInit);
	}

	/**
	 * This is a test object which has annotations in it to facilitate the
	 * injection tests.
	 */
	public static class TestClass
	{

		@Inject
		private String superPrivate = "DEFAULT_SUPER_PRIVATE";
		@Inject
		protected String superProtected = "DEFAULT_SUPER_PROTECTED";
		@Inject
		String superDefault = "DEFAULT_SUPER_DEFAULT";
		@Inject
		public String superPublic = "DEFAULT_SUPER_PUBLIC";
		@Inject
		@Named(value = "namedValue")
		public String notNamedValue = "DEFAULT_NOT_NAMED_VALUE";
		public String ignoreMe = "DEFAULT_IGNORE_ME";
		public String testInit = "DEFAULT_TEST_INIT";
		private String testPrivateInit;

		@Initialize
		public void testInit()
		{
			this.testInit = this.notNamedValue;
		}

		@Initialize
		private void testPrivateInit()
		{
			this.testPrivateInit = this.notNamedValue;
		}
	}

	/**
	 * This is a subclass of the above class which is used to test the cases
	 * when subclasses are involved
	 */
	public static class SubClass extends TestClass
	{

		@Inject
		public String subClassValue = "DEFAULT_SUB_CLASS_VALUE";
		private String subClassInit = "DEFAULT_SUB_CLASS_INIT";

		@Initialize
		public void subClassInit()
		{
			this.subClassInit = this.notNamedValue;
		}
	}
}
