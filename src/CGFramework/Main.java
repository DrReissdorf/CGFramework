package CGFramework; 

/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2012 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */
import java.io.File;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import static org.lwjgl.opengl.GL11.*;


public class Main
{
    private static boolean exit              = false;
    private static boolean fullscreen        = false;
    private static boolean fullscreenChanged = false;
    private static int     width             = 1280;
    private static int     height            = 720;

    /* Variablen fuer drawAndUpdateFps */
    private static long oldTime;
    private static long currentTime;
    private static int frames = 0;


    public static void toggleFullscreen()
    {
        try
        {
            fullscreen = !fullscreen;
            fullscreenChanged = true;

            if( fullscreen )
            {
                DisplayMode displayMode = Display.getDesktopDisplayMode();
                Display.setDisplayModeAndFullscreen( displayMode );
            }
            else
                Display.setDisplayMode( new DisplayMode(width, height) );
        }
        catch( LWJGLException e )
        {
            e.printStackTrace();
        }
    }


    public static void exit()
    {
        exit = true;
    }

    private static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private static void drawAndUpdateFps() {
        if ((getTime() - oldTime) > 1000) {
            Display.setTitle("FPS: " + frames + " FOV: " + Sandbox.fov + "   " + Sandbox.specularModel);
            frames = 0;
            oldTime += 1000;
        }
        frames++;
    }

    public static void main( String[] args )
    {
        System.setProperty("org.lwjgl.librarypath", new File("frameworks/lwjgl-2.9.0/native").getAbsolutePath());
        try
        {
            if( LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_MACOSX )
                Display.create( new PixelFormat(0,16,0,0), new ContextAttribs(3, 2).withProfileCore(true) );
            else
                Display.create( new PixelFormat(0,16,0,0), new ContextAttribs(3, 1) );

            Display.setDisplayMode( new DisplayMode(width, height) );
            Display.setVSyncEnabled( true );

            Display.setTitle( "CG FHK" );
            Display.setResizable( true );

            glViewport( 0, 0, width, height );
        }
        catch( LWJGLException e )
        {
            e.printStackTrace();
        }

        Sandbox sandbox   = new Sandbox( width, height );
        float   deltaTime = 0.0f;
        long    lastTime  = 0;
        oldTime = getTime();

        while( !Display.isCloseRequested() && !Main.exit ) {
            Display.sync(120);
            drawAndUpdateFps();
            long time = System.nanoTime();
            deltaTime = (float)(time - lastTime) * 1e-9f;
            lastTime  = time;


            sandbox.update( deltaTime );
            sandbox.draw();

            Display.update();

            if( Display.wasResized() || fullscreenChanged )
            {
                // =========================================
                // workaround since LWJGL doesn't recognize
                // the fullscreen toggle as a display resize
                // =========================================

                if( !fullscreenChanged )
                {
                    width  = Display.getWidth();
                    height = Display.getHeight();
                }

                sandbox.onResize( Display.getWidth(), Display.getHeight() );
            }
        }

        Display.destroy();
    }
}
