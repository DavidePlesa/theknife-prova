package theknife.server;

import java.io.*;
import java.net.*;

public class GeoTheKnife {

    /**
     * Verifica se un indirizzo esiste tramite Nominatim.
     */
    public static boolean domicilioEsistente(String domicilio) throws IOException {
        String urlString = "https://nominatim.openstreetmap.org/search?q="
                + domicilio.replace(" ", "+") + "&format=json&limit=1";
        URL url = URI.create(urlString).toURL();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line = rd.readLine();
            return line != null && !line.equals("[]");
        }
    }

    /**
     * Ottiene latitudine e longitudine di un indirizzo tramite Nominatim.
     * Restituisce null se l'indirizzo non è trovato.
     */
    public static float[] getLatitudineLongitudine(String indirizzo) throws IOException {
        String urlString = "https://nominatim.openstreetmap.org/search?q="
                + indirizzo.replace(" ", "+") + "&format=json&limit=1";
        URL url = URI.create(urlString).toURL();
        StringBuilder json = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = in.readLine()) != null)
                json.append(line);
        }

        int latIndex = json.indexOf("\"lat\":\"");
        int lonIndex = json.indexOf("\"lon\":\"");
        if (latIndex == -1 || lonIndex == -1) return null;

        float[] coords = new float[2];
        coords[0] = Float.parseFloat(json.substring(latIndex + 7, json.indexOf("\"", latIndex + 7)));
        coords[1] = Float.parseFloat(json.substring(lonIndex + 7, json.indexOf("\"", lonIndex + 7)));
        return coords;
    }

    /**
     * Calcola la distanza approssimativa in km tra due coordinate geografiche.
     */
    public static double calcolaDistanza(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = (lat2 - lat1) * 111;
        double lonDistance = (lon2 - lon1) * 111;
        return Math.sqrt(latDistance * latDistance + lonDistance * lonDistance);
    }
}