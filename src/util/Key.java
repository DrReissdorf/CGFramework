/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2012 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */
package util;

import org.lwjgl.input.Keyboard;


/**
 * LWJGL doesn't allow access to the internally stored key states, 
 * and thereby prevents checking for state changes since we cannot 
 * just copy the full old state and test the current state against 
 * it.
 * This class provides easy polling access for "key up" and 
 * "key down" events.
 */
public class Key
{
	private static boolean[] key_was_down = new boolean[Keyboard.KEYBOARD_SIZE];
	private static boolean   any_key      = false;
	
	
	/**
	 * This function should be called at the end of a frame to make 
	 * sure justPressed() / justReleased() behave as expected.
	 */
	public static void updateKeystates()
	{
		any_key = false;
		
		while( Keyboard.next() )
		{
			any_key = true;
			
			int key           = Keyboard.getEventKey();
			key_was_down[key] = Keyboard.getEventKeyState();
		}
	}
	
	
	public static boolean isPressed( int key )
	{
		return Keyboard.isKeyDown( key );
	}
	
	
	public static boolean justPressed( int key )
	{
		return Keyboard.isKeyDown( key ) && !key_was_down[key];
	}
	
	
	public static boolean justReleased( int key )
	{
		return key_was_down[key] && !Keyboard.isKeyDown( key );
	}
	
	
	public static boolean any()
	{
		return any_key;
	}
}
