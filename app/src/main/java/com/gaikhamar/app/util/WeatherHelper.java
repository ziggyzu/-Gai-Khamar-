package com.gaikhamar.app.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Free Open-Meteo API (no key). Please keep the "Weather data by Open-Meteo.com" credit on screen. */
public final class WeatherHelper {
    private WeatherHelper() {}

    // Same order as the district_names array in strings.xml
    public static final double[] LAT = {22.70, 24.85, 22.36, 23.46, 23.81, 25.63, 23.60, 23.99,
            23.17, 22.83, 23.90, 24.75, 24.00, 24.37, 25.75, 24.90};
    public static final double[] LON = {90.37, 89.37, 91.84, 91.18, 90.41, 88.63, 89.84, 90.41,
            89.21, 89.55, 89.10, 90.40, 89.23, 88.60, 89.25, 91.87};

    public static class Result {
        public double temp;
        public double humidity;
        public double rain;
        public int district;
        public long time;
    }

    /**
     * Blocking network call — must be run on a background thread.
     * Returns null if the network is unavailable or the response cannot be parsed.
     */
    public static Result fetch(int districtIndex) {
        try {
            double lat = LAT[districtIndex];
            double lon = LON[districtIndex];
            String urlStr = "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=" + lat
                    + "&longitude=" + lon
                    + "&current_weather=true"
                    + "&hourly=relativehumidity_2m,precipitation"
                    + "&forecast_days=1"
                    + "&timezone=Asia%2FDhaka";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            JSONObject root = new JSONObject(sb.toString());
            JSONObject cw = root.getJSONObject("current_weather");
            double temp = cw.getDouble("temperature");

            // Average today's hourly humidity
            org.json.JSONArray humArr = root.getJSONObject("hourly").getJSONArray("relativehumidity_2m");
            double humSum = 0;
            for (int i = 0; i < humArr.length(); i++) humSum += humArr.getDouble(i);
            double humidity = humSum / humArr.length();

            // Sum today's hourly precipitation
            org.json.JSONArray precArr = root.getJSONObject("hourly").getJSONArray("precipitation");
            double rain = 0;
            for (int i = 0; i < precArr.length(); i++) rain += precArr.getDouble(i);

            Result r = new Result();
            r.temp = temp;
            r.humidity = humidity;
            r.rain = rain;
            r.district = districtIndex;
            r.time = System.currentTimeMillis();
            return r;
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    /**
     * Temperature Humidity Index (THI) — a simple heat stress indicator for dairy cattle.
     * Returns 0 (comfortable), 1 (mild), 2 (moderate) or 3 (severe).
     */
    public static int heatStress(double tempC, double humidity) {
        double thi = (1.8 * tempC + 32) - ((0.55 - 0.0055 * humidity) * ((1.8 * tempC + 32) - 58));
        if (thi < 72) return 0;
        if (thi < 79) return 1;
        if (thi < 89) return 2;
        return 3;
    }
}
