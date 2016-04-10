/*
 * Copyright (C) 2013 The CyanogenMod project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.preference.EditTextPreference;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;

import cyanogenmod.providers.CMSettings;

public class CustomDefaultGatewayPreference extends EditTextPreference {

    private static final String TAG = "CustomDefaultGatewayPreference";

    private static final String PROP_DEFAULT_GATEWAY = "net.defaultgateway";

    private final String DEFAULT_GATEWAY;

    InputFilter mGatewayInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {

            if (source.length() == 0)
                return null;

            // remove any character that is not alphanumeric, period, or hyphen
            return source.subSequence(start, end).toString().replaceAll("[^-.a-zA-Z0-9]", "");
        }
    };

    public CustomDefaultGatewayPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // determine the default gateway
        String ip = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.DEFAULT_GATEWAY);

        if (ip != null && ip.length() > 0) {
            DEFAULT_GATEWAY = ip;
        } else {
            DEFAULT_GATEWAY = "192.168.0.1";
        }

        setSummary(getText());
        getEditText().setFilters(new InputFilter[] { mGatewayInputFilter });
        getEditText().setHint(DEFAULT_GATEWAY);
    }

    public CustomDefaultGatewayPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.editTextPreferenceStyle);
    }

    public CustomDefaultGatewayPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String gateway = getEditText().getText().toString();

            // remove any preceding or succeeding periods or hyphens
            gateway = gateway.replaceAll("(?:\\.|-)+$", "");
            gateway = gateway.replaceAll("^(?:\\.|-)+", "");

            if (gateway.length() == 0) {
                if (DEFAULT_GATEWAY.length() != 0) {
                    // if no gateway is given, use the default
                    gateway = DEFAULT_GATEWAY;
                } else {
                    // if no other name can be determined
                    // fall back on the current gateway
                    gateway = getText();
                }
            }
            setText(gateway);
        }
    }

    @Override
    public void setText(String text) {
        if (text == null) {
            Log.e(TAG, "tried to set null gateway, request ignored");
            return;
        } else if (text.length() == 0) {
            Log.w(TAG, "setting empty gateway");
        } else {
            Log.i(TAG, "gateway has been set: " + text);
        }
        SystemProperties.set(PROP_DEFAULT_GATEWAY, text);
        persistGateway(text);
        setSummary(text);
    }

    @Override
    public String getText() {
        return SystemProperties.get(PROP_DEFAULT_GATEWAY);
    }

    @Override
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String gateway = getText();
        persistGateway(gateway);
    }

    public void persistGateway(String gateway) {
        Settings.Secure.putString(getContext().getContentResolver(),
                Settings.Secure.DEFAULT_GATEWAY, gateway);
    }

}
