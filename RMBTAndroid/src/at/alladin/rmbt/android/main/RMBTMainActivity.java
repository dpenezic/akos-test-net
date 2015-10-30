/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/*******************************************************************************
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.android.main;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import at.alladin.rmbt.android.about.RMBTAboutFragment;
import at.alladin.rmbt.android.adapter.result.OnCompleteListener;
import at.alladin.rmbt.android.fragments.LogFragment;
import at.alladin.rmbt.android.fragments.NetstatFragment;
import at.alladin.rmbt.android.fragments.history.RMBTFilterFragment;
import at.alladin.rmbt.android.fragments.history.RMBTHistoryFragment;
import at.alladin.rmbt.android.fragments.result.QoSCategoryPagerFragment;
import at.alladin.rmbt.android.fragments.result.QoSTestDetailPagerFragment;
import at.alladin.rmbt.android.fragments.result.RMBTResultPagerFragment;
import at.alladin.rmbt.android.fragments.result.RMBTTestResultDetailFragment;
import at.alladin.rmbt.android.help.RMBTHelpFragment;
import at.alladin.rmbt.android.map.MapListEntry;
import at.alladin.rmbt.android.map.MapListSection;
import at.alladin.rmbt.android.map.MapProperties;
import at.alladin.rmbt.android.map.RMBTMapFragment;
import at.alladin.rmbt.android.preferences.RMBTPreferenceActivity;
import at.alladin.rmbt.android.sync.RMBTSyncFragment;
import at.alladin.rmbt.android.terms.RMBTCheckFragment;
import at.alladin.rmbt.android.terms.RMBTCheckFragment.CheckType;
import at.alladin.rmbt.android.terms.RMBTTermsCheckFragment;
import at.alladin.rmbt.android.test.RMBTLoopService;
import at.alladin.rmbt.android.test.RMBTService;
import at.alladin.rmbt.android.test.RMBTTestFragment;
import at.alladin.rmbt.android.util.CheckHistoryTask;
import at.alladin.rmbt.android.util.CheckNewsTask;
import at.alladin.rmbt.android.util.CheckSettingsTask;
import at.alladin.rmbt.android.util.Config;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.DebugPrintStream;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.GeoLocation;
import at.alladin.rmbt.android.util.GetMapOptionsInfoTask;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.LogTask;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult.DetailType;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultCollection;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultDesc;

import com.google.android.gms.maps.model.LatLng;
import at.alladin.openrmbt.android.R;

/**
 * 
 * @author
 * 
 */
public class RMBTMainActivity extends Activity implements MapProperties
{
	
	private final static boolean SHOW_NDT_CHECK = false;
	private final static boolean SHOW_INFORMATION_COMMISSIONER_CHECK = true;
	
	/**
	 * 
	 */
	private final static boolean VIEW_HIERARCHY_SERVER_ENABLED = false; 
	
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "RMBTMainActivity";
    
    /**
	 * 
	 */
    private FragmentManager fm;
    
    /**
	 * 
	 */
    private GeoLocation geoLocation;
        
    /**
	 * 
	 */
    private CheckNewsTask newsTask;
    
    /**
	 * 
	 */
    private CheckSettingsTask settingsTask;
    
    /**
     * 
     */
    private GetMapOptionsInfoTask getMapOptionsInfoTask;
    
    /**
     * 
     */
    private CheckHistoryTask historyTask;
    
    /**
     * 
     */
    private String historyFilterDevices[];
    
    /**
	 * 
	 */
    private String historyFilterNetworks[];
    
    /**
	 * 
	 */
    private ArrayList<String> historyFilterDevicesFilter;
    
    /**
	 * 
	 */
    private ArrayList<String> historyFilterNetworksFilter;
    
    /**
	 * 
	 */
    private final ArrayList<Map<String, String>> historyItemList = new ArrayList<Map<String, String>>();
    
    /**
	 * 
	 */
    private final ArrayList<Map<String, String>> historyStorageList = new ArrayList<Map<String, String>>();
    
    /**
     * 
     */
    private int historyResultLimit;
    
    /**
	 * 
	 */
    private final HashMap<String, String> currentMapOptions = new HashMap<String, String>();
    
    /**
	 * 
	 */
    private HashMap<String, String> currentMapOptionTitles = new HashMap<String, String>();
    
    /**
	 * 
	 */
    private ArrayList<MapListSection> mapTypeListSectionList;
    
    /**
	 * 
	 */
    private HashMap<String,List<MapListSection>> mapFilterListSectionListMap;
    
    private MapListEntry currentMapType;
    
    // /
    
    /**
	 * 
	 */
    private IntentFilter mNetworkStateChangedFilter;
    
    /**
	 * 
	 */
    private BroadcastReceiver mNetworkStateIntentReceiver;
    
    // /
    
    private boolean mapTypeSatellite;
    
    /**
	 * 
	 */
    private boolean historyDirty = true;
    
    /**
     * 
     */
    private MapOverlay mapOverlayType = MapOverlay.AUTO;
    
    /**
     * 
     */
    private boolean mapFirstRun = true;
    
    private ProgressDialog loadingDialog; 
    
    private DrawerLayout drawerLayout;
    
    private ListView drawerList;
    
	private ActionBarDrawerToggle drawerToggle;
	
	private boolean exitAfterDrawerClose = false;
	
	private Menu actionBarMenu;
	
	private String title;
	
	private NetworkInfoCollector networkInfoCollector;
	
    /**
     * 
     */
    private void preferencesUpdate()
    {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //remove control server version on start
        ConfigHelper.setControlServerVersion(this, null);
        
        final Context context = getApplicationContext();
        final PackageInfo pInfo;
        final int clientVersion;
        try
        {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientVersion = pInfo.versionCode;
            
            final int lastVersion = preferences.getInt("LAST_VERSION_CODE", -1);
            if (lastVersion == -1 || lastVersion <= 17)
            {
                preferences.edit().clear().commit();
                Log.d(DEBUG_TAG, "preferences cleared");
            }
            
            if (lastVersion != clientVersion)
                preferences.edit().putInt("LAST_VERSION_CODE", clientVersion).commit();
        }
        catch (final NameNotFoundException e)
        {
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
    }
    
    /**
	 * 
	 */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
    	//Log.i("MAIN ACTIVITY", "onCreate");
        restoreInstance(savedInstanceState);
        super.onCreate(savedInstanceState);
        NetworkInfoCollector.init(this);
        networkInfoCollector = NetworkInfoCollector.getInstance();
        
        preferencesUpdate();
        setContentView(R.layout.main_with_navigation_drawer);
        
        if (VIEW_HIERARCHY_SERVER_ENABLED) {
        	ViewServer.get(this).addWindow(this);
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayUseLogoEnabled(true);
                
        // initialize the navigation drawer with the main menu list adapter:
        String[] mainTitles = getResources().getStringArray(R.array.navigation_main_titles);
        int[] navIcons = new int[] {R.drawable.ic_action_home, R.drawable.ic_action_history, 
        		R.drawable.ic_action_map, R.drawable.ic_action_stat, R.drawable.ic_action_help, 
        		R.drawable.ic_action_about, R.drawable.ic_action_settings, R.drawable.ic_action_about};

        MainMenuListAdapter mainMenuAdapter = new MainMenuListAdapter(this, mainTitles, navIcons);
        
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerLayout.setBackgroundResource(R.drawable.ic_drawer);
        
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.page_title_title_page, R.string.page_title_title_page) {
        	
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //refreshActionBar(null);
				exitAfterDrawerClose = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setOnKeyListener(new OnKeyListener() {		
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_BACK == event.getKeyCode() && exitAfterDrawerClose) {
					onBackPressed();
					return true;
				}				
				return false;
			}
		});
        drawerLayout.setDrawerListener(drawerToggle);
        drawerList.setAdapter(mainMenuAdapter);
        drawerList.setOnItemClickListener(new OnItemClickListener() {
            final int[] menuIds = new int[] {R.id.action_title_page, R.id.action_history, R.id.action_map, 
            		R.id.action_stats, R.id.action_help, R.id.action_info, R.id.action_settings, R.id.action_netstat, R.id.action_log};

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectMenuItem(menuIds[position]);
				drawerLayout.closeDrawers();
			}
		});
        
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Do something against banding effect in gradients
        // Dither flag might mess up on certain devices??
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        
        // Setzt Default-Werte, wenn noch keine Werte vorhanden
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        
        fm = getFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.fragment_content);
        if (! ConfigHelper.isTCAccepted(this))
        {
            if (fragment != null && fm.getBackStackEntryCount() >= 1)
                // clear fragment back stack
                fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            
            getActionBar().hide();
            setLockNavigationDrawer(true);
            
            showTermsCheck();
        }
        else
        {
            currentMapOptions.put("highlight", uuid);
            if (fragment == null)
            {
                if (false) // deactivated for si // ! ConfigHelper.isNDTDecisionMade(this))
                {
                    showTermsCheck();
                    showNdtCheck();
                }
                else
                    initApp(true);
            }
        }
        
        geoLocation = new MainGeoLocation(getApplicationContext());
        
        mNetworkStateChangedFilter = new IntentFilter();
        mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        
        mNetworkStateIntentReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(final Context context, final Intent intent)
            {
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
                {
                	final boolean connected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                	final boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
                	
                	                    
                	if (connected) {
                    	if (networkInfoCollector != null) {
                    		networkInfoCollector.setHasConnectionFromAndroidApi(true);
                    	}                		
                	}
                	else {              		
                    	if (networkInfoCollector != null) {
                    		networkInfoCollector.setHasConnectionFromAndroidApi(false);
                    	}                     	
                	}
                	
            		Log.i(DEBUG_TAG, "CONNECTED: " + connected + " FAILOVER: " + isFailover);            		
                }
            }
        };                
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	ViewServer.get(this).removeWindow(this);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    	drawerToggle.syncState();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        this.actionBarMenu = menu;
        
        title = getTitle(getCurrentFragmentName());
        refreshActionBar(getCurrentFragmentName());
    	return true;
        //return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	//show/hide navigation drawer if home button (on the actionbar) was pressed
            if (drawerLayout.isDrawerOpen(drawerList)) {
                drawerLayout.closeDrawer(drawerList);
            } else {
                drawerLayout.openDrawer(drawerList);
            }
        }
        else {
        	selectMenuItem(item.getItemId());
        }
    	return true;
    }
    
    /**
     * 
     * @param id
     */
    public void selectMenuItem(int id) {
    	if (id != R.id.action_settings && id != R.id.action_title_page && id != R.id.action_info) {
    		if (networkInfoCollector != null) {
    			if (!networkInfoCollector.hasConnectionFromAndroidApi()) {
    				showNoNetworkConnectionToast();
    				return;
    			}
    		}
    	}
    	
    	switch (id) {
    	case R.id.action_title_page:
    		popBackStackFull();
    		break;
    	case R.id.action_help:
    		showHelp(true);
    		break;
    	case R.id.action_history:
    		showHistory(false);
    		break;
    	case R.id.action_info:
    		showAbout();
    		break;
    	case R.id.action_map:
    		showMap(true);
    		break;
    	case R.id.action_settings:
    		showSettings();
    		break;
    	case R.id.action_stats:
    		showStatistics();
    		break;
    	case R.id.action_menu_filter:
    		showFilter();
    		break;
    	case R.id.action_menu_sync:
    		showSync();
    		break;
    	case R.id.action_menu_help:
    		showHelp(false);
    		break;
    	case R.id.action_menu_share:
    		showShareResultsIntent();
    		break;
    	case R.id.action_menu_rtr:
    		showRtrWebPage();
    		break;
    	case R.id.action_menu_map:
    		showMapFromPager();
    		break;
    	case R.id.action_netstat:
    		showNetstatFragment();
    		break;
    	case R.id.action_log:
    		showLogFragment();
    		break;
    	}
    }

	@SuppressWarnings("unchecked")
    protected void restoreInstance(Bundle b)
    {
        if (b == null)
            return;
        historyFilterDevices = (String[]) b.getSerializable("historyFilterDevices");
        historyFilterNetworks = (String[]) b.getSerializable("historyFilterNetworks");
        historyFilterDevicesFilter = (ArrayList<String>) b.getSerializable("historyFilterDevicesFilter");
        historyFilterNetworksFilter = (ArrayList<String>) b.getSerializable("historyFilterNetworksFilter");
        historyItemList.clear();
        historyItemList.addAll((ArrayList<Map<String, String>>) b.getSerializable("historyItemList"));
        historyStorageList.clear();
        historyStorageList.addAll((ArrayList<Map<String, String>>) b.getSerializable("historyStorageList"));
        historyResultLimit = b.getInt("historyResultLimit");
        currentMapOptions.clear();
        currentMapOptions.putAll((HashMap<String, String>) b.getSerializable("currentMapOptions"));
        currentMapOptionTitles = (HashMap<String, String>) b.getSerializable("currentMapOptionTitles");
        mapTypeListSectionList = (ArrayList<MapListSection>) b.getSerializable("mapTypeListSectionList");
        mapFilterListSectionListMap = (HashMap<String,List<MapListSection>>) b.getSerializable("mapFilterListSectionListMap");
        currentMapType = (MapListEntry) b.getSerializable("currentMapType");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle b)
    {
        super.onSaveInstanceState(b);
        b.putSerializable("historyFilterDevices", historyFilterDevices);
        b.putSerializable("historyFilterNetworks", historyFilterNetworks);
        b.putSerializable("historyFilterDevicesFilter", historyFilterDevicesFilter);
        b.putSerializable("historyFilterNetworksFilter", historyFilterNetworksFilter);
        b.putSerializable("historyItemList", historyItemList);
        b.putSerializable("historyStorageList", historyStorageList);
        b.putInt("historyResultLimit", historyResultLimit);
        b.putSerializable("currentMapOptions", currentMapOptions);
        b.putSerializable("currentMapOptionTitles", currentMapOptionTitles);
        b.putSerializable("mapTypeListSectionList", mapTypeListSectionList);
        b.putSerializable("mapFilterListSectionListMap", mapFilterListSectionListMap);
        b.putSerializable("currentMapType", currentMapType);

    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	ViewServer.get(this).setFocusedWindow(this);
    }
    
    /**
	 * 
	 */
    @Override
    public void onStart()
    {
    	Log.i(DEBUG_TAG, "onStart");
        super.onStart();
        
        registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
        // init location Manager
        
        if (ConfigHelper.isTCAccepted(this) && ConfigHelper.isNDTDecisionMade(this)) {
            geoLocation.start();
        }
        
        title = getTitle(getCurrentFragmentName());
        refreshActionBar(getCurrentFragmentName());
    }
    
    /**
	 * 
	 */
    @Override
    public void onStop()
    {
    	Log.i(DEBUG_TAG, "onStop");
        super.onStop();
        stopBackgroundProcesses();
        unregisterReceiver(mNetworkStateIntentReceiver);
    }
        
    public void setOverlayVisibility(boolean isVisible) {
        final LinearLayout overlay = (LinearLayout) findViewById(R.id.overlay);

        if (isVisible) {
        	overlay.setVisibility(View.VISIBLE);
        	overlay.setClickable(true);
        	overlay.bringToFront();
        }
        else {
        	overlay.setVisibility(View.GONE);
        }
    }
    
    /**
     * 
     * @param context
     */
    private void checkNews(final Context context)
    {
        newsTask = new CheckNewsTask(this);
        newsTask.execute();
        // newsTask.setEndTaskListener(this);
    }
    
    /**
     * 
     * @param context
     */
    private void checkLogs(final Context context, final OnCompleteListener listener) {
    	if (ConfigHelper.DEFAULT_SEND_LOG_TO_CONTROL_SERVER) {
	        final LogTask logTask = new LogTask(this, listener);
	        logTask.execute();
    	}
    }
    
    public boolean haveUuid()
    {
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        return (uuid != null && uuid.length() > 0);
    }
    
    public boolean haveHistoryFilters()
    {
        return (historyFilterDevices != null && historyFilterNetworks != null);
    }
    
    /**
     * 
     */
    public void checkSettings(boolean force, final EndTaskListener endTaskListener)
    {
        if (settingsTask != null && settingsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        
        if (! force && haveUuid() && haveHistoryFilters())
            return;
        
        settingsTask = new CheckSettingsTask(this);
        settingsTask.setEndTaskListener(new EndTaskListener()
        {
            @Override
            public void taskEnded(JSONArray result)
            {
                if (loadingDialog != null)
                    loadingDialog.dismiss();
                if (endTaskListener != null)
                    endTaskListener.taskEnded(result);
            }
        });
        
        settingsTask.execute();
    }
    
    public void waitForSettings(boolean waitForUUID, boolean waitForHistoryFilters, boolean forceWait)
    {
        final boolean haveUuid = haveUuid();
        if (forceWait || (waitForUUID && ! haveUuid) || (waitForHistoryFilters && ! haveHistoryFilters()))
        {
            if (loadingDialog != null)
                loadingDialog.dismiss();
            if (settingsTask != null && settingsTask.getStatus() == AsyncTask.Status.RUNNING)
            {
                final CharSequence title = getResources().getText(! haveUuid ? R.string.main_dialog_registration_title : R.string.main_dialog_reload_title);
                final CharSequence text = getResources().getText(! haveUuid ? R.string.main_dialog_registration_text : R.string.main_dialog_reload_text);
                loadingDialog = ProgressDialog.show(this, title, text, true, true);
                loadingDialog.setOnCancelListener(new ProgressDialog.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        onBackPressed();
                    }
                });
            }
        }
    }
    
    /**
     * 
     */
    public void fetchMapOptions()
    {
        if (getMapOptionsInfoTask != null && getMapOptionsInfoTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        
        getMapOptionsInfoTask = new GetMapOptionsInfoTask(this);
        getMapOptionsInfoTask.execute();
    }
    
    /**
     * 
     * @param popStack
     */
    public void startTest(final boolean popStack)
    {
		if (networkInfoCollector != null) {
			if (!networkInfoCollector.hasConnectionFromAndroidApi()) {
				showNoNetworkConnectionToast();
				return;
			}
		}

    	
        final boolean loopMode = ConfigHelper.isLoopMode(this);
        if (loopMode)
        {
            startService(new Intent(this, RMBTLoopService.class));
        }
        else
        {
            FragmentTransaction ft;
            ft = fm.beginTransaction();
            final RMBTTestFragment rmbtTestFragment = new RMBTTestFragment();
            ft.replace(R.id.fragment_content, rmbtTestFragment, AppConstants.PAGE_TITLE_TEST);
            ft.addToBackStack(AppConstants.PAGE_TITLE_TEST);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (popStack)
                fm.popBackStack();
            ft.commit();
            
            final Intent service = new Intent(RMBTService.ACTION_START_TEST, null, this, RMBTService.class);
            startService(service);
        }
    }
    
    private void showShareResultsIntent() {
    	Fragment f = getCurrentFragment();
    	if (f != null) {
    		((RMBTResultPagerFragment) f).getPagerAdapter().startShareResultsIntent();
    	}
	}
    
    public void showTermsCheck()
    {
    	popBackStackFull();
    	
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, RMBTTermsCheckFragment.getInstance(null), AppConstants.PAGE_TITLE_TERMS_CHECK);
        ft.commit();
    }
    
    public void showRtrWebPage() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.menu_rtr_web_link))));
    }
    
    
    public boolean showChecksIfNecessary() {
    	boolean icDecisionMade = true; 
    	if (SHOW_INFORMATION_COMMISSIONER_CHECK) {
    		icDecisionMade = ConfigHelper.isICDecisionMade(this);
    		if (!icDecisionMade) {
    			showIcCheck();
    		}
    	}
    	
    	boolean ndtDecisionMade = true;
    	if (SHOW_NDT_CHECK) {
	    	ndtDecisionMade = ConfigHelper.isNDTDecisionMade(this);
	    	if (!ndtDecisionMade) {
	    		showNdtCheck();
	    	}
    	}
    	
    	return !ndtDecisionMade || !icDecisionMade;
    }
        
    /**
     * information commissioner check
     */
    public void showIcCheck() {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, RMBTCheckFragment.newInstance(CheckType.INFORMATION_COMMISSIONER), AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.commit();    	
    }
    
    public void showNdtCheck()
    {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, RMBTCheckFragment.newInstance(CheckType.NDT), AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.addToBackStack(AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.commit();
    }
    
    public void showResultsAfterTest(String testUuid) {
    	popBackStackFull();
    	
        final RMBTResultPagerFragment fragment = new RMBTResultPagerFragment();
        final Bundle args = new Bundle();
        args.putString(RMBTResultPagerFragment.ARG_TEST_UUID, testUuid);
        fragment.setArguments(args);
        
        
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_HISTORY_PAGER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY_PAGER);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_PAGER);
    }
    
    public void initApp(boolean duringCreate)
    {
    	//check log directory and send log files to control server if available
        checkLogs(getApplicationContext(), new OnCompleteListener() {
			
			@Override
			public void onComplete(int flag, Object object) {
		        //after log check: redirect system output to file if option is set
		        redirectSystemOutput(ConfigHelper.isSystemOutputRedirectedToFile(RMBTMainActivity.this));
			}
		});
        
    	popBackStackFull();
    	
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new RMBTMainMenuFragment(), AppConstants.PAGE_TITLE_MAIN);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        checkNews(getApplicationContext());
        checkSettings(false, null);
        //checkIp();
        waitForSettings(true, false, false);
        fetchMapOptions();
        historyResultLimit = Config.HISTORY_RESULTLIMIT_DEFAULT;
        
        if (! duringCreate && geoLocation != null)
            geoLocation.start();        
    }
    
    /**
     * 
     */
    public void showNoNetworkConnectionToast() {
    	Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 
     * @param popStack
     */
    public void showHistory(final boolean popStack)
    {
    	popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();
        
        ft.replace(R.id.fragment_content, new RMBTHistoryFragment(), AppConstants.PAGE_TITLE_HISTORY);
        ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (popStack) {
        	fm.popBackStack();
        }
        
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY);
    }
    
    /**
     * 
     * @param itemList
     * @param pos
     */
    public void showHistoryPager(final int pos)
    {
        if (historyStorageList != null)
        {
//            final RMBTHistoryPagerFragment fragment = new RMBTHistoryPagerFragment();
            
            final Bundle args = new Bundle();
            
            final RMBTResultPagerFragment fragment = new RMBTResultPagerFragment();
            String testUuid = historyStorageList.get(pos).get("test_uuid");
            //testUuid = "842356d7-a863-48f9-8220-678125fb3a76";
            //testUuid = "0d765559-ab16-4fa1-b776-4040e18bf134";
            //testUuid = "dbf47f08-711f-4cfa-9fd9-78f06a7a7df3";
            args.putString(RMBTResultPagerFragment.ARG_TEST_UUID, testUuid);
            fragment.setArguments(args);
            
            final FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_HISTORY_PAGER);
            ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY_PAGER);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            
            refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_PAGER);
        }
    }
    
    /**
     * 
     * @param uid
     */
    public void showResultDetail(final String testUUid)
    {    	
        FragmentTransaction ft;
        
        final Fragment fragment = new RMBTTestResultDetailFragment();
        
        final Bundle args = new Bundle();
        
        args.putString(RMBTTestResultDetailFragment.ARG_UID, testUUid);
        fragment.setArguments(args);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_RESULT_DETAIL);
        ft.addToBackStack(AppConstants.PAGE_TITLE_RESULT_DETAIL);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_RESULT_DETAIL);
    }
    
    public void showAbout() {
    	popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();
        
        ft.replace(R.id.fragment_content, new RMBTAboutFragment(), AppConstants.PAGE_TITLE_ABOUT);
        ft.addToBackStack(AppConstants.PAGE_TITLE_ABOUT);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);        	
        
        ft.commit();
        refreshActionBar(AppConstants.PAGE_TITLE_ABOUT);
    }
    
    /**
     * 
     * @param uid
     */
    public void showExpandedResultDetail(QoSServerResultCollection testResultArray, DetailType detailType, int position)
    {
        FragmentTransaction ft;
        
        //final RMBTResultDetailPagerFragment fragment = new RMBTResultDetailPagerFragment();
        final QoSCategoryPagerFragment fragment = new QoSCategoryPagerFragment();
        
        fragment.setQoSResult(testResultArray);
        fragment.setDetailType(detailType);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_RESULT_QOS);
        ft.addToBackStack("result_detail_expanded");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        fragment.setCurrentPosition(position);
        refreshActionBar(AppConstants.PAGE_TITLE_RESULT_QOS);
    }
    
    /**
     * 
     * @param uid
     */
    public void showQoSTestDetails(List<QoSServerResult> resultList, List<QoSServerResultDesc> descList, int index)
    {
        FragmentTransaction ft;
        
        //final RMBTResultDetailPagerFragment fragment = new RMBTResultDetailPagerFragment();
        final QoSTestDetailPagerFragment fragment = new QoSTestDetailPagerFragment();

        fragment.setQoSResultList(resultList);
        fragment.setQoSDescList(descList);
        fragment.setInitPosition(index);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
        ft.addToBackStack(AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
    }

    public void showMapFromPager() {
    	try {
    		Fragment f = getCurrentFragment();
    		if (f != null) {
    			((RMBTResultPagerFragment) f).getPagerAdapter().showMap();
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void showMap(boolean popBackStack) {
    	if (popBackStack) {
    		popBackStackFull();
    	}
    	
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        Fragment f = new RMBTMapFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_ALL_GESTURES, true);
        bundle.putBoolean(RMBTMapFragment.OPTION_SHOW_INFO_TOAST, true);
        bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_CONTROL_BUTTONS, true);
        f.setArguments(bundle);
        ft.replace(R.id.fragment_content, f, AppConstants.PAGE_TITLE_MAP);
        ft.addToBackStack(AppConstants.PAGE_TITLE_MAP);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

        refreshActionBar(AppConstants.PAGE_TITLE_MAP);
    }
    
    public RMBTMapFragment showMap(String mapType, LatLng initialCenter, boolean clearFilter, boolean popBackStack) {
    	return showMap(mapType, initialCenter, clearFilter, -1, popBackStack);
    }
    
    /**
     * @param testPoint 
     * @param mapType 
     * 
     */
    public RMBTMapFragment showMap(String mapType, LatLng initialCenter, boolean clearFilter, int viewId, boolean popBackStack)
    {
    	if (popBackStack) {
    		popBackStackFull();
    	}
    	
        FragmentTransaction ft;
        
        setCurrentMapType(mapType);
        
        if (clearFilter)
        {
            final List<MapListSection> mapFilterListSelectionList = getMapFilterListSelectionList();
            if (mapFilterListSelectionList != null)
            {
                for (final MapListSection section : mapFilterListSelectionList)
                {
                    for (final MapListEntry entry : section.getMapListEntryList())
                        entry.setChecked(entry.isDefault());
                }
                updateMapFilter();
            }
        }
        
        final RMBTMapFragment fragment = new RMBTMapFragment();
        
        final Bundle bundle = new Bundle();
        bundle.putParcelable("initialCenter", initialCenter);
        
        if (viewId >= 0) {
        	bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_ALL_GESTURES, false);
        	bundle.putBoolean(RMBTMapFragment.OPTION_SHOW_INFO_TOAST, false);
        	bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_CONTROL_BUTTONS, false);
        	bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_OVERLAY, false);
            fragment.setArguments(bundle);
            ft = fm.beginTransaction();
        	//replace the given viewgroup, but do not add to backstack
            ft.replace(viewId, fragment, AppConstants.PAGE_TITLE_MINI_MAP);
            //ft.addToBackStack(AppConstants.PAGE_TITLE_MINI_MAP);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit(); 	
        }
        else {
        	System.out.println("SHOW MAP");
            fragment.setArguments(bundle);
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_MAP);
            ft.addToBackStack(AppConstants.PAGE_TITLE_MAP);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            refreshActionBar(AppConstants.PAGE_TITLE_MAP);
        }

        return fragment;
    }
    
    /**
     * 
     * @param path
     */
    
    public void showHelp(final int resource, boolean popBackStack)
    {
        showHelp(getResources().getString(resource), popBackStack, AppConstants.PAGE_TITLE_HELP);
    }
    
    public void showHelp(boolean popBackStack) {
    	showHelp("", popBackStack, AppConstants.PAGE_TITLE_HELP);
    }
    
    public void showHelp(final String url, boolean popBackStack, String titleId)
    {
    	if (popBackStack) {
    		popBackStackFull();
    	}
        
    	FragmentTransaction ft;
    	
        
        ft = fm.beginTransaction();
        
        final Fragment fragment = new RMBTHelpFragment();        
        final Bundle args = new Bundle();
            
        args.putString(RMBTHelpFragment.ARG_URL, url);
        fragment.setArguments(args);
        ft.replace(R.id.fragment_content, fragment, titleId);
        ft.addToBackStack(titleId);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.commit();
        refreshActionBar(titleId);
    }
    
    /**
     * 
     */
    public void showSync()
    {
        FragmentTransaction ft;        
        ft = fm.beginTransaction();
        
        final Fragment fragment = new RMBTSyncFragment();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_SYNC);
        ft.addToBackStack(AppConstants.PAGE_TITLE_SYNC);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);        	
        
        ft.commit();
        refreshActionBar(AppConstants.PAGE_TITLE_SYNC);
    }
    
    public void showFilter() {
        final FragmentManager fm = getFragmentManager();
        FragmentTransaction ft;
        
        final Fragment fragment = new RMBTFilterFragment();
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_HISTORY_FILTER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY_FILTER);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_FILTER);
    }

    /**
     * 
     */
    public void showSettings() {
        startActivity(new Intent(this, RMBTPreferenceActivity.class));
    }
    
    /**
     * 
     */
    public void showStatistics() {
        String urlStatistic = ConfigHelper.getVolatileSetting("url_statistics");
        if (urlStatistic == null || urlStatistic.length() == 0) {
        	if ((urlStatistic = ConfigHelper.getCachedStatisticsUrl(getApplicationContext())) == null) {
        		return;
        	}
        }
        showHelp(urlStatistic, true, AppConstants.PAGE_TITLE_STATISTICS);
    }
    
    public void showNetstatFragment()
    {
        FragmentTransaction ft;        
        ft = fm.beginTransaction();
        
        final Fragment fragment = new NetstatFragment();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_NETSTAT);
        ft.addToBackStack(AppConstants.PAGE_TITLE_NETSTAT);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);        	
        
        ft.commit();
        refreshActionBar(AppConstants.PAGE_TITLE_NETSTAT);
    }
    
    public void showLogFragment()
    {
        FragmentTransaction ft;        
        ft = fm.beginTransaction();
        
        final Fragment fragment = new LogFragment();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_LOG);
        ft.addToBackStack(AppConstants.PAGE_TITLE_LOG);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);        	
        
        ft.commit();
        refreshActionBar(AppConstants.PAGE_TITLE_LOG);
    }


    
    /**
     * 
     */
    private void stopBackgroundProcesses()
    {
        geoLocation.stop();
        if (newsTask != null)
        {
            newsTask.cancel(true);
            newsTask = null;
        }
        if (settingsTask != null)
        {
            settingsTask.cancel(true);
            settingsTask = null;
        }
        if (loadingDialog != null)
        {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
        
        if (getMapOptionsInfoTask != null)
        {
            getMapOptionsInfoTask.cancel(true);
            getMapOptionsInfoTask = null;
        }
        if (historyTask != null)
        {
            historyTask.cancel(true);
            historyTask = null;
        }
    }
    
    /**
     * 
     * @param history_filter_devices
     * @param history_filter_networks
     */
    public void setSettings(final String[] history_filter_devices, final String[] history_filter_networks)
    {
        historyFilterDevices = history_filter_devices;
        historyFilterNetworks = history_filter_networks;
        
        historyFilterDevicesFilter = new ArrayList<String>();
        if (history_filter_devices != null)
            for (final String history_filter_device : history_filter_devices)
                historyFilterDevicesFilter.add(history_filter_device);
        
        historyFilterNetworksFilter = new ArrayList<String>();
        if (history_filter_networks != null)
            for (final String history_filter_network : history_filter_networks)
                historyFilterNetworksFilter.add(history_filter_network);
    }
    
    /**
     * 
     * @return
     */
    public String[] getHistoryFilterDevices()
    {
        return historyFilterDevices;
    }
    
    /**
     * 
     * @return
     */
    public String[] getHistoryFilterNetworks()
    {
        return historyFilterNetworks;
    }
    
    /**
     * 
     * @param history_filter_devices_filter
     */
    public void setHistoryFilterDevicesFilter(final ArrayList<String> historyFilterDevicesFilter)
    {
        this.historyFilterDevicesFilter = historyFilterDevicesFilter;
        historyDirty = true;
    }
    
    /**
     * 
     * @param history_filter_networks_filter
     */
    public void setHistoryFilterNetworksFilter(final ArrayList<String> historyFilterNetworksFilter)
    {
        this.historyFilterNetworksFilter = historyFilterNetworksFilter;
        historyDirty = true;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<String> getHistoryFilterDevicesFilter()
    {
        return historyFilterDevicesFilter;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<String> getHistoryFilterNetworksFilter()
    {
        return historyFilterNetworksFilter;
    }
    
    /**
     * 
     * @return
     */
    public List<Map<String, String>> getHistoryItemList()
    {
        return historyItemList;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<Map<String, String>> getHistoryStorageList()
    {
        return historyStorageList;
    }
    
    /**
     * 
     */
    @Override
    public Map<String, String> getCurrentMapOptions()
    {
        return currentMapOptions;
    }
    
    /**
     * 
     */
    public Map<String, String> getCurrentMapOptionTitles()
    {
        return currentMapOptionTitles;
    }
    
    // /
    
    public void setCurrentMapType(MapListEntry currentMapType)
    {
        this.currentMapType = currentMapType;
        
     // set the filter options in activity
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        currentMapOptions.clear();
        currentMapOptionTitles.clear();
        currentMapOptions.put("highlight", uuid);
        currentMapOptions.put(currentMapType.getKey(), currentMapType.getValue());
        currentMapOptions.put("overlay_type", currentMapType.getOverlayType());
        currentMapOptionTitles.put(currentMapType.getKey(),
                currentMapType.getSection().getTitle() + ": " + currentMapType.getTitle());
        
        updateMapFilter();
    }
    
    public void setCurrentMapType(String mapType)
    {
        if (mapTypeListSectionList == null || mapType == null)
            return;
        for (final MapListSection section : mapTypeListSectionList)
        {
            for (MapListEntry entry : section.getMapListEntryList())
            {
                if (entry.getValue().equals(mapType))
                {
                    setCurrentMapType(entry);
                    return;
                }
            }
        }
    }
    
    public MapListEntry getCurrentMapType()
    {
        return currentMapType;
    }
    
    public String getCurrentMainMapType()
    {
        final String mapTypeString = currentMapType == null ? null : currentMapType.getValue();
        String part = null;
        if (mapTypeString != null)
        {
            final String[] parts = mapTypeString.split("/");
            part = parts[0];
        }
        return part;
    }
    
    /**
     * 
     * @return
     */
    public List<MapListSection> getMapTypeListSectionList()
    {
        return mapTypeListSectionList;
    }
    
    /**
     * 
     * @param mapTypeListSectionList
     */
    public void setMapTypeListSectionList(final ArrayList<MapListSection> mapTypeListSectionList)
    {
        this.mapTypeListSectionList = mapTypeListSectionList;
    }
    
    /**
     * 
     * @return
     */
    public Map<String,List<MapListSection>> getMapFilterListSectionListMap()
    {
        return mapFilterListSectionListMap;
    }
    
    public List<MapListSection> getMapFilterListSelectionList()
    {
        
        final Map<String, List<MapListSection>> mapFilterListSectionListMap = getMapFilterListSectionListMap();
        if (mapFilterListSectionListMap == null)
            return null;
        return mapFilterListSectionListMap.get(getCurrentMainMapType());
    }
    
    /**
     * 
     * @param mapFilterListSectionList
     */
    public void setMapFilterListSectionListMap(final HashMap<String,List<MapListSection>> mapFilterListSectionList)
    {
        this.mapFilterListSectionListMap = mapFilterListSectionList;
        updateMapFilter();
    }
    
    public void updateMapFilter()
    {
        final List<MapListSection> mapFilterListSelectionList = getMapFilterListSelectionList();
        if (mapFilterListSelectionList == null)
            return;
        for (final MapListSection section : mapFilterListSelectionList)
        {
            final MapListEntry entry = section.getCheckedMapListEntry();
            
            if (entry != null && entry.getKey() != null && entry.getValue() != null)
            {
                getCurrentMapOptions().put(entry.getKey(), entry.getValue());
                getCurrentMapOptionTitles().put(entry.getKey(),
                        entry.getSection().getTitle() + ": " + entry.getTitle());
            }
        }
    }
    
    
    
    /**
     * 
     * @author
     * 
     */
    private class MainGeoLocation extends GeoLocation
    {
        /**
         * 
         * @param context
         */
        public MainGeoLocation(final Context context)
        {
            super(context, ConfigHelper.isGPS(context));
        }
        
        /**
		 * 
		 */
        @Override
        public void onLocationChanged(final Location curLocation)
        {
        }
    }
    
    
    /**
     * 
     * @return
     */
    public boolean isMapFirstRun()
    {
        return mapFirstRun;
    }
    
    /**
     * 
     * @param mapFirstRun
     */
    public void setMapFirstRun(final boolean mapFirstRun)
    {
        this.mapFirstRun = mapFirstRun;
    }
    
    /**
	 * 
	 */
    @Override
    public void onBackPressed()
    {
//        final RMBTNDTCheckFragment ndtCheckFragment = (RMBTNDTCheckFragment) getSupportFragmentManager().findFragmentByTag("ndt_check");
//        if (ndtCheckFragment != null)
//            if (ndtCheckFragment.onBackPressed())
//                return;
        

        final RMBTTermsCheckFragment tcFragment = (RMBTTermsCheckFragment) getFragmentManager().findFragmentByTag("terms_check");
        if (tcFragment != null && tcFragment.isResumed()) {
            if (tcFragment.onBackPressed())
                return;
        }
        
        final RMBTTestFragment testFragment = (RMBTTestFragment) getFragmentManager().findFragmentByTag("test");
        if (testFragment != null && testFragment.isResumed()) {
            if (testFragment.onBackPressed())
                return;
        }


        final RMBTSyncFragment syncCodeFragment = (RMBTSyncFragment) getFragmentManager()
                .findFragmentByTag("sync");
        if (syncCodeFragment != null && syncCodeFragment.isResumed()) {
            if (syncCodeFragment.onBackPressed())
                return;
        } 

        final RMBTMainMenuFragment mainMenuCodeFragment = (RMBTMainMenuFragment) getFragmentManager()
                .findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
        if (mainMenuCodeFragment != null && mainMenuCodeFragment.isResumed()) {
            if (mainMenuCodeFragment.onBackPressed())
                return;
        } 
        
        refreshActionBarAndTitle();
        
        if (getFragmentManager().getBackStackEntryCount() > 0 || exitAfterDrawerClose) {
            super.onBackPressed();            
        }
        else {
        	System.out.println(getCurrentFragment());
        	if (ConfigHelper.isDontShowMainMenuOnClose(this)) {
        		super.onBackPressed();
        	}
        	else {
        		exitAfterDrawerClose = true;
        		drawerLayout.openDrawer(drawerList);
        	}
        }
    }
    
    private void refreshActionBarAndTitle() {
        title = getTitle(getPreviousFragmentName());
        refreshActionBar(getPreviousFragmentName());
	}

	/**
     * 
     * @return
     */
    public boolean isHistoryDirty()
    {
        return historyDirty;
    }
    
    /**
     * 
     * @param historyDirty
     */
    public void setHistoryDirty(final boolean historyDirty)
    {
        this.historyDirty = historyDirty;
    }
    
    /**
     * 
     * @author bp
     * 
     */
    public interface HistoryUpdatedCallback
    {
    	public final static int SUCCESSFUL = 0;
    	public final static int LIST_EMPTY = 1;
    	public final static int ERROR = 2;
    	
        /**
         * 
         * @param success
         */
        public void historyUpdated(int status);
    }
    
    /**
     * 
     * @param callback
     */
    public void updateHistory(final HistoryUpdatedCallback callback)
    {
        if (historyDirty
                && (historyTask == null || historyTask.isCancelled() || historyTask.getStatus() == AsyncTask.Status.FINISHED))
        {
            historyTask = new CheckHistoryTask(this, historyFilterDevicesFilter, historyFilterNetworksFilter);
            
            historyTask.setEndTaskListener(new EndTaskListener()
            {
                @Override
                public void taskEnded(final JSONArray resultList)
                {
                    if (resultList != null && resultList.length() > 0 && !historyTask.hasError())
                    {
                        historyStorageList.clear();
                        historyItemList.clear();
                        
                        final Date tmpDate = new Date();
                        final DateFormat dateFormat = Helperfunctions.getDateFormat(false);
                        
                        for (int i = 0; i < resultList.length(); i++)
                        {
                            
                            JSONObject resultListItem;
                            try
                            {
                                resultListItem = resultList.getJSONObject(i);
                                
                                final HashMap<String, String> storageItem = new HashMap<String, String>();
                                storageItem.put("test_uuid", resultListItem.optString("test_uuid", null));
                                storageItem.put("time", String.valueOf(resultListItem.optLong("time", 0)));
                                storageItem.put("timezone", resultListItem.optString("timezone", null));
                                historyStorageList.add(storageItem);
                                
                                final HashMap<String, String> viewItem = new HashMap<String, String>();
                                // viewIitem.put( "device",
                                // resultListItem.optString("plattform","none"));
                                viewItem.put("device", resultListItem.optString("model", "-"));
                                
                                viewItem.put("type", resultListItem.optString("network_type"));
                                
                                final String timeString = Helperfunctions.formatTimestampWithTimezone(tmpDate,
                                        dateFormat, resultListItem.optLong("time", 0),
                                        resultListItem.optString("timezone", null));
                                
                                viewItem.put("date", timeString == null ? "-" : timeString);
                                
                                viewItem.put("down", resultListItem.optString("speed_download", "-"));
                                viewItem.put("up", resultListItem.optString("speed_upload", "-"));
                                viewItem.put("ping", resultListItem.optString("ping", "-"));
                                historyItemList.add(viewItem);
                            }
                            catch (final JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        historyDirty = false;
                        if (callback != null)
                            callback.historyUpdated(HistoryUpdatedCallback.SUCCESSFUL);
                    }
                    else if (callback != null) {
                        callback.historyUpdated(historyTask.hasError() ? HistoryUpdatedCallback.ERROR : HistoryUpdatedCallback.LIST_EMPTY);
                    }
                }
            });
            historyTask.execute();
        }
        else if (callback != null)
            callback.historyUpdated(!(historyStorageList.isEmpty() && historyStorageList.isEmpty()) ? HistoryUpdatedCallback.SUCCESSFUL : HistoryUpdatedCallback.LIST_EMPTY);
    }

    public void setMapOverlayType(final MapOverlay mapOverlayType)
    {
        this.mapOverlayType = mapOverlayType;
    }
    
    public MapOverlay getMapOverlayType()
    {
        return mapOverlayType;
    }
    
    /**
     * 
     * @return
     */
    public int getHistoryResultLimit()
    {
        return historyResultLimit;
    }
    
    /**
     * 
     * @param limit
     */
    public void setHistoryResultLimit(final int limit)
    {
        historyResultLimit = limit;
    }

    public void setMapTypeSatellite(boolean mapTypeSatellite)
    {
        this.mapTypeSatellite = mapTypeSatellite;
    }

    public boolean getMapTypeSatellite()
    {
        return mapTypeSatellite;
    }
    
    public void popBackStackFull() {
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
		    	if (fm.getBackStackEntryCount() > 0) {
		        	fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);	
		    	}
		    	
		    	refreshActionBarAndTitle();				
			}
		});
    }
    
    /**
     * 
     * @param toFile
     */
    public void redirectSystemOutput(boolean toFile) {
        try {
        	if (toFile) {
        		Log.i(DEBUG_TAG,"redirecting sysout to file");
        		//Redirecting console output and runtime exceptions to file (System.out.println)
        		File f = new File(Environment.getExternalStorageDirectory(), "qosdebug");
        		if (!f.exists()) {
        			f.mkdir();
        		}
            
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.GERMAN);
        		//PrintStream fileStream = new PrintStream(new File(f, sdf.format(new Date()) + ".txt"));
        		PrintStream fileStream = new DebugPrintStream(new File(f, sdf.format(new Date()) + ".txt"));
             
        		//System.setOut(fileStream);
                System.setErr(fileStream);
        	}
        	else {        		
        		//Redirecting console output and runtime exceptions to default output stream
                //System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                //System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
                Log.i(DEBUG_TAG,"redirecting sysout to default");
        	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public Fragment getCurrentFragment()
    {
    	final int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 0)
        {
    		try
            {
                final BackStackEntry backStackEntryAt = getFragmentManager().getBackStackEntryAt(backStackEntryCount - 1);
                String fragmentTag = backStackEntryAt.getName();
                Fragment currentFragment = getFragmentManager().findFragmentByTag(fragmentTag);
                return currentFragment;
            }
            catch (Exception e)
            {
                // fix possible race condition:
                // when called in background thread - back stack could be different between call of
                // getBackStackEntryCount() and getBackStackEntryAt()
                e.printStackTrace();
            }
    	}
    	
    	return getFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
    }

    /**
     * 
     * @return
     */
    public String getCurrentFragmentName(){
    	if (getFragmentManager().getBackStackEntryCount() > 0) {
    		String fragmentTag = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName();
            return fragmentTag;
    	}

    	Fragment f = getFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
    	return f != null ? AppConstants.PAGE_TITLE_MAIN : null;
    }

    /**
     * 
     * @return
     */
    protected String getPreviousFragmentName(){
    	if (getFragmentManager().getBackStackEntryCount() > 1) {
    		String fragmentTag = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 2).getName();
            return fragmentTag;
    	}

    	return null;
    }

    /**
     * 
     * @return
     */
    protected String getTitle(String fragmentName) {
    	String name = fragmentName; // (fragmentName != null ? fragmentName : getCurrentFragmentName());
    	Integer id = null;
    	if (name != null)
    	    id = AppConstants.TITLE_MAP.get(name);
    	
    	if (id == null)
    	    id = R.string.page_title_title_page;
    	    
        title = getResources().getString(id);

    	return title;
    }
    
    /**
     * 
     * @param isEnabled
     */
    public void setLockNavigationDrawer(boolean isLocked) {
    	if (isLocked) {
    		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    	}
    	else {
    		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    	}
    }
    
    public void refreshActionBar(String name) {
    	if (name == null && title == null) {
    		getActionBar().setTitle(getTitle(getCurrentFragmentName()));
    	}
    	else {
    		getActionBar().setTitle((name != null || title == null) ? getTitle(name) : title);
    	}
    	
    	if (actionBarMenu != null) {
    		if (AppConstants.PAGE_TITLE_HISTORY.equals(name)) {
        		setVisibleMenuItems(R.id.action_menu_filter, R.id.action_menu_sync);
        	}
    		else if (AppConstants.PAGE_TITLE_ABOUT.equals(name)) {
    			setVisibleMenuItems(R.id.action_menu_rtr);
    		}
    		else if (AppConstants.PAGE_TITLE_HISTORY_PAGER.equals(name)) {
    			Fragment f = getCurrentFragment();
    			if (f != null && f instanceof RMBTResultPagerFragment) {
    				((RMBTResultPagerFragment) f).setActionBarItems();
    			}
    		}
        	else {
        		setVisibleMenuItems();
        	}    	
    	}
    }
    
    /**
     * 
     * @param id
     */
    public void setVisibleMenuItems(Integer...id) {
    	if (actionBarMenu != null) {
    		if (id != null && id.length > 0) {
        		Set<Integer> idSet = new HashSet<Integer>(); 
        		Collections.addAll(idSet, id);
        		for (int i = 0; i < actionBarMenu.size(); i++) {
        			MenuItem item = actionBarMenu.getItem(i);
        			if (idSet.contains(item.getItemId())) {
        				item.setVisible(true);
        			}
        			else {
        				item.setVisible(false);
        			}
        		}
    		}
    		else {
        		for (int i = 0; i < actionBarMenu.size(); i++) {
        			MenuItem item = actionBarMenu.getItem(i);
        			item.setVisible(false);
        		}
    		}
    	}
    }
    
    /**
     * 
     * @return
     */
    public NetworkInfoCollector getNetworkInfoCollector() {
    	return this.networkInfoCollector;
    }
    
    /*
     */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
        boolean isMobile = false, isWifi = false;

        try {
            NetworkInfo[] infoAvailableNetworks = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getAllNetworkInfo();

            if (infoAvailableNetworks != null) {
                for (NetworkInfo network : infoAvailableNetworks) {

                    if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (network.isConnected() && network.isAvailable())
                            isWifi = true;
                    }
                    if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                        if (network.isConnected() && network.isAvailable())
                            isMobile = true;
                    }
                }
            }

            return isMobile || isWifi;
        }
        catch (Exception e) {
        	return false;
        }
    }
}