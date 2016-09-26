package com.minecolonies.permissions;

import org.junit.Before;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Runs syntax tests.
 * 
 * TODO
 * Simulate:
 * - block break
 * - block interact
 * - block place
 * - item drop
 * - item use
 * - entity interact
 * 
 * @author Isfirs
 */
public class PermissionTest {
	
	public static final String[] TEST_TRUE = new String[] {
		"block.place.minecraft:",
		"block.place.minecraft:dirt",
		"block.place.minecraft:*",
	};
	
	public static final String[] TEST_FALSE = new String[] {
		"block.place.minecraft: dirt",
		"block.place.*:*",
		"block.place.*:grass",
	};
	
	/**
	 * 
	 */
	@Before
	public void before()
	{
		//
	}
	
	/**
	 * 
	 */
	@Test
	public void testSyntaxCorrect()
	{
		// Test correct syntax
		for (String string: TEST_TRUE)
		{
			try {
				new PermissionKey(string);
			} catch (IllegalArgumentException ex) {
				System.out.println(String.format("Error at `%s`", string));
			}
		}
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testSyntaxIncorrect()
	{
		IllegalArgumentException iae;
		// Test false syntax
		for (String string: TEST_FALSE)
		{
			iae = null;
			try {
				new PermissionKey(string);
			} catch(IllegalArgumentException ex)
			{
				iae = ex;
			}
			
			assertNotNull(String.format("No IllegalArgumentException at `%s`", IllegalArgumentException.class.getSimpleName(), string), iae);
		}
	}
	
}
