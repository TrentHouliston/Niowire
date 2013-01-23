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
public class NioObjectFactoryTest
{

	/**
	 * Tests that creating objects works as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testCreate() throws Exception
	{
		//Create some data
		HashMap<String, String> testData = new HashMap<String, String>(4);
		testData.put("superPrivate", "TEST_SUPER_PRIVATE");
		testData.put("superProtected", "TEST_SUPER_PROTECTED");
		testData.put("superDefault", "TEST_SUPER_DEFAULT");
		testData.put("superPublic", "TEST_SUPER_PUBLIC");
		testData.put("subClassValue", "TEST_SUB_CLASS_VALUE");
		testData.put("namedValue", "TEST_NOT_NAMED_VALUE");

		//Build a factory1 and create an object
		NioObjectFactory<SubClass> factory = new NioObjectFactory<SubClass>(SubClass.class, testData);
		SubClass obj = factory.create();

		//Check that all the fields were injected properly (we have to cast to the super object to access the private member)
		assertEquals("The private injection did not have the expected result", "TEST_SUPER_PRIVATE", ((TestClass) obj).superPrivate);
		assertEquals("The protected injection did not have the expected result", "TEST_SUPER_PROTECTED", obj.superProtected);
		assertEquals("The default injection did not have the expected result", "TEST_SUPER_DEFAULT", obj.superDefault);
		assertEquals("The public injection did not have the expected result", "TEST_SUPER_PUBLIC", obj.superPublic);
		assertEquals("The subclass injection did not have the expected result", "TEST_SUB_CLASS_VALUE", obj.subClassValue);

		//Check that the init method ran properly
		assertEquals("The superclass init method did not run", "TEST_NOT_NAMED_VALUE", obj.testInit);
		assertEquals("The subclass init method did not run", "TEST_NOT_NAMED_VALUE", obj.subClassInit);
	}

	/**
	 * Tests that the isInstance method works as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testIsInstance() throws Exception
	{
		//Create some test data
		HashMap<String, String> testData1 = new HashMap<String, String>(6);
		testData1.put("superPrivate", "TEST_SUPER_PRIVATE");
		testData1.put("superProtected", "TEST_SUPER_PROTECTED");
		testData1.put("superDefault", "TEST_SUPER_DEFAULT");
		testData1.put("superPublic", "TEST_SUPER_PUBLIC");
		testData1.put("subClassValue", "TEST_SUB_CLASS_VALUE");
		testData1.put("namedValue", "TEST_NOT_NAMED_VALUE");

		HashMap<String, String> testData2 = new HashMap<String, String>(6);
		testData2.put("superPrivate", "TEST_SUPER_PRIVATE_2");
		testData2.put("superProtected", "TEST_SUPER_PROTECTED_2");
		testData2.put("superDefault", "TEST_SUPER_DEFAULT_2");
		testData2.put("superPublic", "TEST_SUPER_PUBLIC_2");
		testData2.put("subClassValue", "TEST_SUB_CLASS_VALUE_2");
		testData2.put("namedValue", "TEST_NOT_NAMED_VALUE_2");

		//Build a factory using the superclass and data 1
		NioObjectFactory<TestClass> factory1 = new NioObjectFactory<TestClass>(TestClass.class, testData1);
		TestClass obj1 = factory1.create();

		//Build a factory using the superclass and data 2
		NioObjectFactory<TestClass> factory2 = new NioObjectFactory<TestClass>(TestClass.class, testData2);
		TestClass obj2 = factory2.create();

		//Build a factory using the subclass and data 1
		NioObjectFactory<SubClass> factory3 = new NioObjectFactory<SubClass>(SubClass.class, testData1);
		TestClass obj3 = factory3.create();

		//Build a factory using the subclass and data 2
		NioObjectFactory<SubClass> factory4 = new NioObjectFactory<SubClass>(SubClass.class, testData2);
		TestClass obj4 = factory4.create();

		//Hand build a superclass using data 1
		TestClass obj5 = new TestClass();
		Injector<TestClass> injector5 = new Injector<TestClass>(TestClass.class, testData1);
		injector5.inject(obj5);

		//Hand build a superclass using data 2
		TestClass obj6 = new TestClass();
		Injector<TestClass> injector6 = new Injector<TestClass>(TestClass.class, testData2);
		injector6.inject(obj6);

		//Hand build a subclass using data 1
		SubClass obj7 = new SubClass();
		Injector<SubClass> injector7 = new Injector<SubClass>(SubClass.class, testData1);
		injector7.inject(obj7);

		//Hand build a subclass using data 2
		SubClass obj8 = new SubClass();
		Injector<SubClass> injector8 = new Injector<SubClass>(SubClass.class, testData2);
		injector8.inject(obj8);

		//Test the results from the 1st factory
		assertTrue("The object the factory created should be an instance", factory1.isInstance(obj1));
		assertFalse("This object has a different configuration, should be false", factory1.isInstance(obj2));
		assertFalse("This object has is a subclass, should be false", factory1.isInstance(obj3));
		assertFalse("This object has a different configuration and is a subclass, should be false", factory1.isInstance(obj4));
		assertTrue("This object is identical apart from not being made by the factory, should be true", factory1.isInstance(obj5));
		assertFalse("This object has a different configuration, should be false", factory1.isInstance(obj6));
		assertFalse("This object has is a subclass, should be false", factory1.isInstance(obj7));
		assertFalse("This object has a different configuration and is a subclass, should be false", factory1.isInstance(obj8));

		//Test the results from the 2nd factory
		assertFalse("This object has a different configuration, should be false", factory2.isInstance(obj1));
		assertTrue("The object the factory created should be an instance", factory2.isInstance(obj2));
		assertFalse("This object has a different configuration and is a subclass, should be false", factory2.isInstance(obj3));
		assertFalse("This object has is a subclass, should be false", factory2.isInstance(obj4));
		assertFalse("This object has a different configuration, should be false", factory2.isInstance(obj5));
		assertTrue("This object is identical apart from not being made by the factory, should be true", factory2.isInstance(obj6));
		assertFalse("This object has a different configuration and is a subclass, should be false", factory2.isInstance(obj7));
		assertFalse("This object has is a subclass, should be false", factory2.isInstance(obj8));

		//Test the results from the 3rd factory
		assertFalse("This object has is a superclass, should be false", factory3.isInstance(obj1));
		assertFalse("This object has a different configuration and is a superclass, should be false", factory3.isInstance(obj2));
		assertTrue("The object the factory created should be an instance", factory3.isInstance(obj3));
		assertFalse("This object has a different configuration, should be false", factory3.isInstance(obj4));
		assertFalse("This object has is a superclass, should be false", factory3.isInstance(obj5));
		assertFalse("This object has a different configuration and is a superclass, should be false", factory3.isInstance(obj6));
		assertTrue("This object is identical apart from not being made by the factory, should be true", factory3.isInstance(obj7));
		assertFalse("This object has a different configuration, should be false", factory3.isInstance(obj8));

		//Test the results from the 4th factory
		assertFalse("This object has a different configuration and is a subclass, should be false", factory4.isInstance(obj1));
		assertFalse("This object has is a subclass, should be false", factory4.isInstance(obj2));
		assertFalse("This object has a different configuration, should be false", factory4.isInstance(obj3));
		assertTrue("The object the factory created should be an instance", factory4.isInstance(obj4));
		assertFalse("This object has a different configuration and is a subclass, should be false", factory4.isInstance(obj5));
		assertFalse("This object has is a subclass, should be false", factory4.isInstance(obj6));
		assertFalse("This object has a different configuration, should be false", factory4.isInstance(obj7));
		assertTrue("This object is identical apart from not being made by the factory, should be true", factory4.isInstance(obj8));
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
