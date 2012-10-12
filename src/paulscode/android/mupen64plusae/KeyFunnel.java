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
 * Authors: littleguy77
 */
package paulscode.android.mupen64plusae;

import android.view.KeyEvent;
import android.view.View;

public class KeyFunnel extends InputFunnel implements View.OnKeyListener
{
    public enum ImeFormula
    {
        DEFAULT, USB_BT_JOYSTICK_CENTER, BT_CONTROLLER, EXAMPLE_IME
    }
    
    private ImeFormula mImeFormula;
    
    public KeyFunnel()
    {
        mImeFormula = ImeFormula.DEFAULT;
    }
    
    public void setImeFormula( ImeFormula formula )
    {
        mImeFormula = formula;
    }
    
    public boolean onKey( View v, int keyCode, KeyEvent event )
    {
        // Ignore cancellations via back key
        if( keyCode == KeyEvent.KEYCODE_BACK )
            return false;
        
        // Translate the input code and analog strength (ranges between 0.0 and
        // 1.0)
        int inputCode;
        float strength;
        if( keyCode < 0xFF )
        {
            // Ordinary key/button changed state
            inputCode = keyCode;
            strength = 1;
        }
        else
        {
            // Analog axis changed state, decode using a formula specific to the
            // IME
            switch( mImeFormula )
            {
                case DEFAULT:
                case USB_BT_JOYSTICK_CENTER:
                case BT_CONTROLLER:
                default:
                    // Formula defined between paulscode and poke64738
                    inputCode = keyCode / 100;
                    strength = ( (float) keyCode % 100 ) / 64f;
                    break;
                case EXAMPLE_IME:
                    // High byte stores input code, low byte stores strength
                    inputCode = keyCode >> 8;
                    strength = ( keyCode & 0xFF ) / 0xFF;
                    break;
            }
        }
        
        // Strength is zero when the button/axis is released
        if( event.getAction() == KeyEvent.ACTION_UP )
            strength = 0;
        
        // Notify listeners of input data
        notifyListeners( inputCode, strength );
        
        return true;
    }
}
