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

import java.io.File;

import paulscode.android.mupen64plusae.persistent.AppData;
import paulscode.android.mupen64plusae.persistent.UserPrefs;
import paulscode.android.mupen64plusae.util.CrashTester;
import paulscode.android.mupen64plusae.util.ErrorLogger;
import paulscode.android.mupen64plusae.util.FileUtil;
import paulscode.android.mupen64plusae.util.Notifier;
import paulscode.android.mupen64plusae.util.Prompt;
import paulscode.android.mupen64plusae.util.TaskHandler;
import paulscode.android.mupen64plusae.util.Utility;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class MenuActivity extends PreferenceActivity implements OnPreferenceClickListener,
        OnSharedPreferenceChangeListener
{
    // These constants must match the keys used in res/xml/preferences.xml
    
    private static final String ACTION_DEVICE_INFO = "actionDeviceInfo";
    private static final String ACTION_CONTROLLER_INFO = "actionControllerInfo";
    private static final String ACTION_MIGRATE_SLOT_SAVES = "actionMigrateSlotSaves";
    private static final String ACTION_CRASH_TEST = "actionCrashTest";
    private static final String ACTION_RELOAD_ASSETS = "actionReloadAssets";
    private static final String ACTION_RESET_USER_PREFS = "actionResetUserPrefs";
    
    private static final String SCREEN_PLAY = "screenPlay";
    private static final String SCREEN_INPUT = "screenInput";
    private static final String SCREEN_TOUCHPAD = "screenTouchpad";
    private static final String SCREEN_VIDEO = "screenVideo";
    private static final String SCREEN_AUDIO = "screenAudio";
    
    private static final String CATEGORY_SINGLE_PLAYER = "categorySinglePlayer";
    private static final String CATEGORY_GLES2_RICE = "categoryGles2Rice";
    private static final String CATEGORY_GLES2_N64 = "categoryGles2N64";
    
    private static final String TOUCHPAD_ENABLED = "touchpadEnabled";
    private static final String TOUCHSCREEN_SIZE = "touchscreenSize";
    private static final String PLUGIN_VIDEO = "videoPlugin";
    
    private static final String PLAYER_MAP = "playerMap";
    private static final String PATH_HI_RES_TEXTURES = "pathHiResTextures";
    private static final String PATH_CUSTOM_TOUCHSCREEN = "pathCustomTouchscreen";
    
    private CheatsMenuHandler mCheatsMenuHandler = null;
    
    // App data and user preferences
    private AppData mAppData = null;
    private UserPrefs mUserPrefs = null;
    
    // Don't need to call these every time the orientation changes
    static
    {
        // Required for reading CRC header
        FileUtil.loadNativeLibName( "SDL" );
        FileUtil.loadNativeLibName( "core" );
        FileUtil.loadNativeLibName( "front-end" );
    }
    
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // Get app data and user preferences
        mAppData = new AppData( this );
        mUserPrefs = new UserPrefs( this );
        
        // Disable the Xperia PLAY plugin as necessary
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        if( !mAppData.hardwareInfo.isXperiaPlay )
            prefs.edit().putBoolean( TOUCHPAD_ENABLED, false ).commit();
        
        // Load user preference menu structure from XML and update view
        addPreferencesFromResource( R.xml.preferences );
        
        // Refresh the preference data wrapper
        mUserPrefs = new UserPrefs( this );
        
        // Instantiate the cheats menu handler
        if( mCheatsMenuHandler == null )
            mCheatsMenuHandler = new CheatsMenuHandler( this, mAppData, mUserPrefs );
        // TODO: Only refresh when the cheats menu is open
        mCheatsMenuHandler.refresh();
        
        // Handle certain menu items that require extra processing or aren't actually preferences
        listenTo( SCREEN_PLAY );
        listenTo( ACTION_DEVICE_INFO );
        listenTo( ACTION_CONTROLLER_INFO );
        listenTo( ACTION_MIGRATE_SLOT_SAVES );
        listenTo( ACTION_RELOAD_ASSETS );
        listenTo( ACTION_RESET_USER_PREFS );
        listenTo( PATH_HI_RES_TEXTURES );
        
        // Handle crash tests in a particular way (see CrashTester for more info)
        findPreference( ACTION_CRASH_TEST ).setOnPreferenceClickListener( new CrashTester( this ) );
        
        // Hide the Xperia PLAY menu items as necessary
        if( !mAppData.hardwareInfo.isXperiaPlay )
        {
            removePreference( CATEGORY_SINGLE_PLAYER, SCREEN_TOUCHPAD );
        }
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        sharedPreferences.unregisterOnSharedPreferenceChangeListener( this );
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        refreshViews( sharedPreferences, mUserPrefs );
        sharedPreferences.registerOnSharedPreferenceChangeListener( this );
    }
    
    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
    {
        if( key.equals( PLUGIN_VIDEO ) || key.equals( TOUCHPAD_ENABLED ) )
        {
            // Sometimes one preference change affects the hierarchy or layout of the views. In this
            // case it's easier just to restart the activity than try to figure out what to fix.
            // Examples:
            // * Restore the preference categories that were removed in refreshViews(...)
            // * Change the input mapping layout when Xperia Play touchpad en/disabled
            finish();
            startActivity( getIntent() );
        }
        else if( key.equals( PATH_HI_RES_TEXTURES ) )
        {
            // TODO: Make this summary persist, rather than the last selected filename
            // We'll need to extend PathPreference in that case, or use a different type of
            // preference
            // findPreference( key ).setSummary( R.string.pathHiResTextures_summary );
            processTexturePak( sharedPreferences.getString( PATH_HI_RES_TEXTURES, "" ) );
        }
        else
        {
            // Just refresh the preference screens in place
            mUserPrefs = new UserPrefs( this );
            refreshViews( sharedPreferences, mUserPrefs );
        }
    }
    
    private void refreshViews( SharedPreferences sharedPreferences, UserPrefs user )
    {
        // Enable the play menu only if the selected game actually exists
        File selectedGame = new File( mUserPrefs.selectedGame );
        boolean isValidGame = selectedGame.exists() && selectedGame.isFile();
        enablePreference( SCREEN_PLAY, isValidGame );
        
        // Enable the multi-player menu only if the player map is enabled
        enablePreference( PLAYER_MAP, user.playerMap.isEnabled() );
        
        // Enable the input menu only if the input plug-in is not a dummy
        enablePreference( SCREEN_INPUT, user.inputPlugin.enabled );
        
        // Enable the audio menu only if the audio plug-in is not a dummy
        enablePreference( SCREEN_AUDIO, user.audioPlugin.enabled );
        
        // Enable the video menu only if the video plug-in is not a dummy
        enablePreference( SCREEN_VIDEO, user.videoPlugin.enabled );
        
        // Hide certain categories altogether if they're not applicable. Normally we just rely on
        // the built-in dependency disabler, but here the categories are so large that hiding them
        // provides a better user experience.
        if( !user.isGles2N64Enabled )
            removePreference( SCREEN_VIDEO, CATEGORY_GLES2_N64 );
        
        if( !user.isGles2RiceEnabled )
            removePreference( SCREEN_VIDEO, CATEGORY_GLES2_RICE );
        
        // Enable the custom touchscreen prefs under certain conditions
        enablePreference( PATH_CUSTOM_TOUCHSCREEN, user.isTouchscreenEnabled
                && user.isTouchscreenCustom );
        enablePreference( TOUCHSCREEN_SIZE, user.isTouchscreenEnabled && !user.isTouchscreenCustom );
        
        // Update the summary text for all relevant preferences
        for( String key : sharedPreferences.getAll().keySet() )
            refreshText( key );
    }
    
    @SuppressWarnings( "deprecation" )
    private void listenTo( String key )
    {
        Preference preference = findPreference( key );
        if( preference != null )
            preference.setOnPreferenceClickListener( this );
    }
    
    @SuppressWarnings( "deprecation" )
    private void refreshText( String key )
    {
        Preference preference = findPreference( key );
        if( preference instanceof ListPreference )
            preference.setSummary( ( (ListPreference) preference ).getEntry() );
    }
    
    @SuppressWarnings( "deprecation" )
    private void enablePreference( String key, boolean enabled )
    {
        Preference preference = findPreference( key );
        if( preference != null )
            preference.setEnabled( enabled );
    }
    
    @SuppressWarnings( "deprecation" )
    private void removePreference( String keyParent, String keyChild )
    {
        Preference parent = findPreference( keyParent );
        Preference child = findPreference( keyChild );
        if( parent instanceof PreferenceGroup && child != null )
            ( (PreferenceGroup) parent ).removePreference( child );
    }
    
    @Override
    public boolean onPreferenceClick( Preference preference )
    {
        // Handle the clicks on certain menu items that aren't actually preferences
        String key = preference.getKey();
        
        if( key.equals( SCREEN_PLAY ) )
        {
            mCheatsMenuHandler.rebuild();
            // Let Android open the play menu, once built
            return false;
        }
        else if( key.equals( ACTION_DEVICE_INFO ) )
            actionDeviceInfo();
        
        else if( key.equals( ACTION_CONTROLLER_INFO ) )
            actionControllerInfo();
        
        else if( key.equals( ACTION_MIGRATE_SLOT_SAVES ) )
            actionMigrateSlotSaves();
        
        else if( key.equals( ACTION_RELOAD_ASSETS ) )
            actionReloadAssets();
        
        else if( key.equals( ACTION_RESET_USER_PREFS ) )
            actionResetUserPrefs();
        
        else
            // Let Android handle all other preference clicks
            return false;
        
        // Tell Android that we handled the click
        return true;
    }
    
    private void actionDeviceInfo()
    {
        String title = getString( R.string.actionDeviceInfo_title );
        String message = Utility.getCpuInfo();
        new Builder( this ).setTitle( title ).setMessage( message ).create().show();
    }
    
    private void actionControllerInfo()
    {
        String title = getString( R.string.actionControllerInfo_title );
        String message = Utility.getPeripheralInfo();
        new Builder( this ).setTitle( title ).setMessage( message ).create().show();
    }
    
    private void actionMigrateSlotSaves()
    {
        final File oldDir = new File( mAppData.oldDataDir + "/data/save/" );
        if( !oldDir.exists() )
        {
            String title = getString( R.string.actionMigrateSlotSaves_title );
            String message = getString( R.string.actionMigrateSlotSaves_messageNotFound );
            new Builder( this ).setTitle( title ).setMessage( message ).create().show();
        }
        else
        {
            String title = getString( R.string.confirm_title );
            String message = getString( R.string.actionMigrateSlotSaves_messageConfirm );
            Prompt.promptConfirm( this, title, message, new OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    if( which == DialogInterface.BUTTON_POSITIVE )
                    {
                        File newDir = new File( mUserPrefs.slotSaveDir );
                        FileUtil.copyFile( oldDir, newDir, true );
                        Notifier.showToast( MenuActivity.this,
                                R.string.actionMigrateSlotSaves_messageSuccess );
                    }
                }
            } );
        }
    }
    
    private void actionReloadAssets()
    {
        mAppData.setAssetVersion( 0 );
        startActivity( new Intent( this, MainActivity.class ) );
        finish();
    }
    
    private void actionResetUserPrefs()
    {
        String title = getString( R.string.confirm_title );
        String message = getString( R.string.actionResetUserPrefs_popupMessage );
        Prompt.promptConfirm( this, title, message, new OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, int which )
            {
                if( which == DialogInterface.BUTTON_POSITIVE )
                {
                    // Don't handle all the changes that are about to be made
                    SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences( MenuActivity.this );
                    sharedPreferences
                            .unregisterOnSharedPreferenceChangeListener( MenuActivity.this );
                    
                    // Reset the user preferences
                    SharedPreferences preferences = PreferenceManager
                            .getDefaultSharedPreferences( MenuActivity.this );
                    preferences.edit().clear().commit();
                    PreferenceManager.setDefaultValues( MenuActivity.this, R.xml.preferences, true );
                    
                    // Rebuild the menu system by restarting the activity
                    finish();
                    startActivity( getIntent() );
                }
            }
        } );
    }
    
    private void processTexturePak( final String filename )
    {
        if( TextUtils.isEmpty( filename ) )
        {
            ErrorLogger.put( "Video", "pathHiResTextures",
                    "Filename not specified in MenuActivity.processTexturePak" );
            Notifier.showToast( this, R.string.pathHiResTexturesTask_errorMessage );
            return;
        }
        
        TaskHandler.Task task = new TaskHandler.Task()
        {
            @Override
            public void run()
            {
                String headerName = Utility.getTexturePackName( filename );
                if( !ErrorLogger.hasError() )
                {
                    if( TextUtils.isEmpty( headerName ) )
                    {
                        ErrorLogger
                                .setLastError( "getTexturePackName returned null in MenuActivity.processTexturePak" );
                        ErrorLogger.putLastError( "Video", "pathHiResTextures" );
                    }
                    else
                    {
                        String outputFolder = mAppData.dataDir + "/data/hires_texture/"
                                + headerName;
                        FileUtil.deleteFolder( new File( outputFolder ) );
                        Utility.unzipAll( new File( filename ), outputFolder );
                    }
                }
            }
            
            @Override
            public void onComplete()
            {
                if( ErrorLogger.hasError() )
                    Notifier.showToast( MenuActivity.this,
                            R.string.pathHiResTexturesTask_errorMessage );
                ErrorLogger.clearLastError();
            }
        };
        
        String title = getString( R.string.pathHiResTexturesTask_title );
        String message = getString( R.string.pathHiResTexturesTask_message );
        TaskHandler.run( this, title, message, task );
    }
}
