/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.statisticServer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public class StatisticParameters implements Serializable, Funnel<StatisticParameters>
{
    private static final long serialVersionUID = 1L;
    
    private final String lang;
    private final float quantile;
    private final int duration; //duration in days
    private final int maxDevices;
    private final String type;
    private final String networkTypeGroup;
    private final double accuracy;
    private final String country;
    
    public StatisticParameters(String defaultLang, String params)
    {
        String _lang = defaultLang;
        float _quantile = 0.5f; // median is default quantile
        int _duration = 90;
        int _maxDevices = 100;
        String _type = "mobile";
        String _networkTypeGroup = null;
        double _accuracy = -1;
        String _country = null;
        
        if (params != null && !params.isEmpty())
            // try parse the string to a JSON object
            try
            {
                final JSONObject request = new JSONObject(params);
                _lang = request.optString("language" , _lang);
                
                final double __quantile = request.optDouble("quantile", Double.NaN);
                if (__quantile >= 0 && __quantile <= 1)
                    _quantile = (float) __quantile;
                
                final int __months = request.optInt("months", 0); // obsolete, old format (now in days)
                if (__months > 0)
                    _duration = __months * 30;

                final int __duration = request.optInt("duration", 0); 
                if (__duration > 0)
                    _duration = __duration;
                
                final int __maxDevices = request.optInt("max_devices", 0);
                if (__maxDevices > 0)
                    _maxDevices = __maxDevices;
                
                final String __type = request.optString("type", null);
                if (__type != null)
                    _type = __type;
                
                final String __networkTypeGroup = request.optString("network_type_group", null);
                if (__networkTypeGroup != null && ! __networkTypeGroup.equalsIgnoreCase("all"))
                    _networkTypeGroup = __networkTypeGroup;
                
                final double __accuracy = request.optDouble("location_accuracy",-1);
                if (__accuracy != -1)
                	_accuracy = __accuracy;
                
                final String __country = request.optString("country", null);
                if (__country != null && __country.length() == 2)
                	_country = __country;
            }
            catch (final JSONException e)
            {
            }
        lang = _lang;
        quantile = _quantile;
        duration = _duration;
        maxDevices = _maxDevices;
        type = _type;
        networkTypeGroup = _networkTypeGroup;
        accuracy = _accuracy;
        country = _country;
    }

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    public String getLang()
    {
        return lang;
    }

    public float getQuantile()
    {
        return quantile;
    }

        public int getDuration() //dz obsoletes getMonths
    {
        return duration;
    }

    public int getMaxDevices()
    {
        return maxDevices;
    }

    public String getType()
    {
        return type;
    }

    public String getNetworkTypeGroup()
    {
        return networkTypeGroup;
    }
    
    public double getAccuracy() {
    	return accuracy;
    }
    
    public String getCountry() {
    	return country;
    }

    @Override
    public void funnel(StatisticParameters o, PrimitiveSink into)
    {
        into
            .putUnencodedChars(o.getClass().getCanonicalName())
            .putChar(':')
            .putUnencodedChars(Strings.nullToEmpty(o.lang))
            .putFloat(o.quantile)
            .putInt(o.duration)
            .putUnencodedChars(Strings.nullToEmpty(o.type))
            .putInt(o.maxDevices)
            .putUnencodedChars(Strings.nullToEmpty(o.networkTypeGroup))
            .putDouble(o.accuracy)
            .putUnencodedChars(Strings.nullToEmpty(o.country));
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + maxDevices;
        result = prime * result + duration;
        result = prime * result + ((networkTypeGroup == null) ? 0 : networkTypeGroup.hashCode());
        result = prime * result + Float.floatToIntBits(quantile);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((accuracy == -1) ? 0 : (int) accuracy);
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatisticParameters other = (StatisticParameters) obj;
        if (lang == null)
        {
            if (other.lang != null)
                return false;
        }
        else if (!lang.equals(other.lang))
            return false;
        if (maxDevices != other.maxDevices)
            return false;
        if (duration != other.duration)
            return false;
        if (networkTypeGroup == null)
        {
            if (other.networkTypeGroup != null)
                return false;
        }
        else if (!networkTypeGroup.equals(other.networkTypeGroup))
            return false;
        if (Float.floatToIntBits(quantile) != Float.floatToIntBits(other.quantile))
            return false;
        if (accuracy != other.accuracy) {
        	return false;
        }
        if (country != null && other.country != null && !country.equals(other.country)) {
        	return false;
        }
        if (type == null)
        {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;
        return true;
    }

}
