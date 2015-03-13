package storybook.test.ui;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import storybook.model.hbn.entity.Person;
import NER.PopulationFromFile;

public class ImportTest {

	
	static Vector<Person> p;
	
	@BeforeClass
	static public void Steve()
	{
		try {
			
		
		PopulationFromFile pff = new PopulationFromFile("sample.txt");
		
		p =  pff.getPopulation();
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	}
	
	@Test
	public void test() {
		try
		{
			
			
			Assert.assertFalse(p.get(0).getFullName().equals("Lehman Brothers"));
			//Assert.assertFalse(p.get(0).getGender().isMale());
			
			
			//fail("Not yet implemented");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	@Test
	public void test2() {
		try
		{
			
			
			
			Assert.assertTrue(p.get(0).getFullName().equals("Timothy Geithner"));
			Assert.assertTrue(p.get(0).getGender().isMale());
			
			
			//fail("Not yet implemented");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	@Test
	public void test3() {
		try
		{
			
			
			
			Assert.assertTrue(p.get(1).getFullName().equals("Henry Paulson"));
			Assert.assertTrue(p.get(1).getGender().isMale());
			
			
			//fail("Not yet implemented");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	@Test
	public void test4() {
		try
		{
			
			
			Assert.assertTrue(p.get(2).getFullName().equals("Michelle Parker"));
			Assert.assertTrue(p.get(2).getGender().isFemale());
			
			//fail("Not yet implemented");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
