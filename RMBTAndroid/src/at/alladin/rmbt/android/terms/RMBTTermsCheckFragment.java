/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.android.terms;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import at.alladin.openrmbt.android.R;

import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.terms.RMBTCheckFragment.CheckType;
import at.alladin.rmbt.android.util.ConfigHelper;

public class RMBTTermsCheckFragment extends Fragment
{
//    private static final String DEBUG_TAG = "RMBTTermsCheckFragment";
    
    private boolean firstTime = true;
    
    private View view;
    
    private final CheckType followedByType;
    
    public static RMBTTermsCheckFragment getInstance(final CheckType followedBy) {
    	return new RMBTTermsCheckFragment(followedBy);
    }
    
    private RMBTTermsCheckFragment(final CheckType followedBy) {
    	this.followedByType = followedBy;
	}
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.terms_check, container, false);
        
        final WebView tcWvl = (WebView) view.findViewById(R.id.termsCheckWebViewLong);
        tcWvl.loadUrl("file:///android_res/raw/terms_conditions_long.html");
        
        final Activity activity = getActivity();
        if (! (activity instanceof RMBTMainActivity))
            firstTime = false;
        
        if (! firstTime)
            view.findViewById(R.id.termsButtonDecline).setVisibility(View.GONE);
        
        final Button buttonTermsAccept = (Button) view.findViewById(R.id.termsAcceptButton);
        buttonTermsAccept.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                ConfigHelper.setTCAccepted(getActivity(), true);
                if (firstTime)
                {
                    ((RMBTMainActivity)getActivity()).checkSettings(true, null);
                    final boolean wasNDTTermsNecessary = ((RMBTMainActivity)getActivity()).showChecksIfNecessary();
                    if (! wasNDTTermsNecessary)
                        ((RMBTMainActivity) activity).initApp(false);
                }
                else if (followedByType != null) {
                	switch (followedByType) {
                	case INFORMATION_COMMISSIONER:
                        ((RMBTTermsActivity)getActivity()).showIcCheck();
                		break;
                	case NDT:
                        ((RMBTTermsActivity)getActivity()).showNdtCheck();
                		break;
                	}
                }
            }
        });
        
        final Button buttonTermsDecline = (Button) view.findViewById(R.id.termsDeclineButton);
        buttonTermsDecline.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onBackPressed();
            }
        });
        
        return view;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        final Activity activity = getActivity();
        final boolean tcAccepted = ConfigHelper.isTCAccepted(activity);
        if (tcAccepted)
        {
            final TextView buttonTermsAccept = (TextView) view.findViewById(R.id.termsAcceptButton);
            buttonTermsAccept.setText(R.string.terms_accept_button_continue);
            view.findViewById(R.id.termsAcceptText).setVisibility(View.GONE);
        }
    }

    public boolean onBackPressed()
    {
        // user has declined t+c!
        
        ConfigHelper.setTCAccepted(getActivity(), false);
        ConfigHelper.setUUID(getActivity(), "");
        getActivity().finish();
        return true;
    }
}
