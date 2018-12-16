package com.example.viratkumar.testproject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.media.ExifInterface;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyExifUtils {

    private boolean valid = false;
    Double latitude, longitude;

    MyExifUtils(String imageFilePath) {

        ExifInterface exifIntent = null;
        try {
            exifIntent = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String exifLatitude = exifIntent.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String exifLatitudeReference = exifIntent.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);

        String exifLongitude = exifIntent.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String exifLongitudeReference = exifIntent.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);


        if (
                (exifLatitude != null)
                        && (exifLongitudeReference != null)
                        && (exifLongitude != null)
                        && (exifLongitudeReference != null)
                ) {
            valid = true;

            if (exifLatitudeReference.equals("N")) {
                latitude = convertToDegree(exifLatitude);
            } else {
                latitude = 0 - convertToDegree(exifLatitude);
            }

            if (exifLongitudeReference.equals("E")) {
                longitude = convertToDegree(exifLongitude);
            } else {
                longitude = 0 - convertToDegree(exifLongitude);
            }

        }
    }

    private Double convertToDegree(String stringDMS) {
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);
        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0 / S1;

        result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result.doubleValue();
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return (String.valueOf(latitude)
                + ", "
                + String.valueOf(longitude));
    }

    public int getLatitudeE6() {
        return (int) (latitude * 1000000);
    }

    public int getLongitudeE6() {
        return (int) (longitude * 1000000);
    }


    public String latLngToAddress(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            //address = addresses.get(0).getAddressLine(0);
            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            if (addresses != null) {
                String locality = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String featureName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                String premises = addresses.get(0).getPremises();
                String subAdminArea = addresses.get(0).getSubAdminArea();
                String subLocality = addresses.get(0).getSubLocality();

                Log.d("SIY", "locality - city : " + locality);
                Log.d("SIY", "admin area - state : " + state);
                Log.d("SIY", "Country : " + country);
                Log.d("SIY", "Postal Code : " + postalCode);
                Log.d("SIY", "Featured Name : " + featureName);
                Log.d("SIY", "Premises : " + premises);
                Log.d("SIY", "Sub Admin Area : " + subAdminArea);
                Log.d("SIY", "Sub Locality  : " + subLocality); // Sector

                //  c-152, Sector-63, Noida

                if (subLocality == null) {
                    subLocality = state;
                    locality = country;
                }
                if (locality == null) {
                    subLocality = subAdminArea;
                }
                address = featureName + ", " + subLocality + ", " + locality;
                Log.d("SIY", "Address : " + address);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SIY", "Exception while converting LatLng to address : " + e.getMessage());
        }
        return address;
    }
}
