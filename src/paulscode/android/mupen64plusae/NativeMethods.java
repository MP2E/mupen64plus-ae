/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2012 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with Mupen64PlusAE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: paulscode
 */
package paulscode.android.mupen64plusae;


/**
 * A class containing all bridging methods between Java and JNI, and nothing else.
 * 
 * @see CoreInterface
 */
public class NativeMethods
{
    // *************************************************
    // *************************************************
    // *************************************************
    // Call-outs made TO the native code
    // See jni/front-end/src/main.c
    // jni/input-sdl/src/plugin.c
    // jni/SDL/src/core/android/SDL_android.cpp
    // jni/SDL/src/main/android/SDL_android_main.cpp
    // *************************************************
    // *************************************************
    // *************************************************
    
    // TODO: These should all have javadoc comments. 
    // It would better document calls going in/out of native code.
    
    public static native void fileLoadEmulator( String filename );
    
    public static native void fileSaveEmulator( String filename );
    
    public static native void init();
    
    public static native String getHeaderCRC( String filename );
    
    public static native String getHeaderName( String filename );
    
    public static native void onAccel( float x, float y, float z );
    
    public static native void onResize( int x, int y, int format );
    
    public static native void pauseEmulator();
    
    public static native void quit();
    
    public static native void resetEmulator();
    
    public static native void resumeEmulator();
    
    public static native void runAudioThread();
    
    public static native int stateEmulator();
    
    public static native void stateLoadEmulator();
    
    public static native void stateSaveEmulator();
    
    public static native void stateSetSlotEmulator( int slotID );
    
    public static native void stopEmulator();
    
    public static native void updateVirtualGamePadStates( int controllerNum, boolean[] buttons,
            int axisX, int axisY );

    public static native void stateSetSpeed( int percent );
    // ********************************************
    // ********************************************
    // ********************************************
    // Call-ins made FROM the native code
    // See jni/SDL/src/core/android/SDL_android.cpp
    // ********************************************
    // ********************************************
    // ********************************************
    
    public static Object audioInit( int sampleRate, boolean is16Bit, boolean isStereo,
            int desiredFrames )
    {
        return CoreInterface.audioInit( sampleRate, is16Bit, isStereo, desiredFrames );
    }
    
    public static void audioQuit()
    {
        CoreInterface.audioQuit();
    }
    
    public static void audioWriteByteBuffer( byte[] buffer )
    {
        CoreInterface.audioWriteByteBuffer( buffer );
    }
    
    public static void audioWriteShortBuffer( short[] buffer )
    {
        CoreInterface.audioWriteShortBuffer( buffer );
    }
    
    public static boolean createGLContext( int majorVersion, int minorVersion )
    {
        return CoreInterface.initEGL( majorVersion, minorVersion );
    }
    
    public static void flipBuffers()
    {
        CoreInterface.flipEGL();
    }
    
    public static boolean getAutoFrameSkip()
    {
        return CoreInterface.getAutoFrameSkip();
    }
    
    public static Object getDataDir()
    {
        return CoreInterface.getDataDir();
    }
    
    public static Object getExtraArgs()
    {
        return CoreInterface.getExtraArgs();
    }
    
    public static int getHardwareType()
    {
        return CoreInterface.getHardwareType();
    }
    
    public static int getMaxFrameSkip()
    {
        return CoreInterface.getMaxFrameSkip();
    }
    
    public static Object getROMPath()
    {
        return CoreInterface.getRomPath();
    }
    public static void emuStateCallback( int newState )
    {
        CoreInterface.emuStateCallback( newState );
    }    
    public static boolean getScreenStretch()
    {
        return CoreInterface.getScreenStretch();
    }
    
    public static void setActivityTitle( String title )
    {
        CoreInterface.setActivityTitle( title );
    }
    
    public static void showToast( String message )
    {
        CoreInterface.showToast( message );
    }
    
    public static boolean useRGBA8888()
    {
        return CoreInterface.useRGBA8888();
    }
    
    public static void vibrate( boolean active )
    {
        CoreInterface.vibrate( active );
    }
}
