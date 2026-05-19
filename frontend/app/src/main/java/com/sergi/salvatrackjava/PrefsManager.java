package com.sergi.salvatrackjava;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrefsManager {

    private static final String PREF_NAME = "salvatrack_prefs";
    private static final String KEY_FAVS  = "favoritos";
    private static final String KEY_HIST  = "historial";
    private static final int    MAX_HIST  = 15;

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ==========================================
    // SESIÓN
    // ==========================================

    private static final String KEY_TOKEN    = "auth_token";
    private static final String KEY_NOMBRE   = "auth_nombre";
    private static final String KEY_IS_ADMIN = "auth_is_admin";

    public static String  getToken(Context ctx)          { return prefs(ctx).getString(KEY_TOKEN, null); }
    public static void    setToken(Context ctx, String v){ prefs(ctx).edit().putString(KEY_TOKEN, v).apply(); }
    public static String  getNombreUsuario(Context ctx)  { return prefs(ctx).getString(KEY_NOMBRE, ""); }
    public static void    setNombreUsuario(Context ctx, String v){ prefs(ctx).edit().putString(KEY_NOMBRE, v).apply(); }
    public static boolean isAdmin(Context ctx)           { return prefs(ctx).getBoolean(KEY_IS_ADMIN, false); }
    public static void    setAdmin(Context ctx, boolean v){ prefs(ctx).edit().putBoolean(KEY_IS_ADMIN, v).apply(); }
    public static boolean haySesion(Context ctx)         { String t = getToken(ctx); return t != null && !t.isEmpty(); }

    public static void cerrarSesion(Context ctx) {
        prefs(ctx).edit()
                .remove(KEY_TOKEN).remove(KEY_NOMBRE).remove(KEY_IS_ADMIN)
                .remove(KEY_FAVS).remove(KEY_HIST)
                .apply();
    }

    // ==========================================
    // FAVORITOS (caché local + sync API)
    // ==========================================

    public static boolean esFavorito(Context ctx, String nombre) {
        try {
            JSONArray arr = new JSONArray(prefs(ctx).getString(KEY_FAVS, "[]"));
            for (int i = 0; i < arr.length(); i++)
                if (nombre.equals(arr.getJSONObject(i).optString("nombre"))) return true;
        } catch (JSONException ignored) {}
        return false;
    }

    /**
     * Toggle local + llama a la API. Devuelve true si ahora ES favorito.
     */
    public static boolean toggleFavorito(Context ctx, Athlete athlete) {
        boolean esFav = esFavorito(ctx, athlete.getNombre());
        // Actualizar caché local
        try {
            JSONArray old = new JSONArray(prefs(ctx).getString(KEY_FAVS, "[]"));
            JSONArray arr = new JSONArray();
            if (esFav) {
                for (int i = 0; i < old.length(); i++)
                    if (!athlete.getNombre().equals(old.getJSONObject(i).optString("nombre")))
                        arr.put(old.getJSONObject(i));
            } else {
                JSONObject obj = new JSONObject();
                obj.put("nombre", athlete.getNombre());
                obj.put("anoNacimiento", athlete.getAnoNacimiento());
                obj.put("club",      athlete.getClub()      != null ? athlete.getClub() : "");
                obj.put("categoria", athlete.getCategoria() != null ? athlete.getCategoria() : "");
                obj.put("genero",    athlete.getGenero()    != null ? athlete.getGenero() : "");
                arr.put(obj);
                for (int i = 0; i < old.length(); i++) arr.put(old.getJSONObject(i));
            }
            prefs(ctx).edit().putString(KEY_FAVS, arr.toString()).apply();
        } catch (JSONException ignored) {}

        // Sync con API (fire & forget)
        String token = getToken(ctx);
        if (token != null) {
            ApiClient.toggleFavorito(token, athlete, new ApiClient.Callback() {
                @Override public void onSuccess(org.json.JSONObject r) {}
                @Override public void onError(String e) {}
            });
        }
        return !esFav;
    }

    public static List<Athlete> getFavoritos(Context ctx) {
        List<Athlete> lista = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(ctx).getString(KEY_FAVS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Athlete a = new Athlete();
                a.setNombre(o.optString("nombre"));
                a.setAnoNacimiento(o.optLong("anoNacimiento"));
                a.setClub(o.optString("club"));
                a.setCategoria(o.optString("categoria"));
                a.setGenero(o.optString("genero"));
                lista.add(a);
            }
        } catch (JSONException ignored) {}
        return lista;
    }

    /** Sobrescribe el caché local de favoritos con los del servidor. */
    public static void setFavoritosFromServer(Context ctx, JSONArray favs) {
        try {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < favs.length(); i++) {
                JSONObject s = favs.getJSONObject(i);
                JSONObject o = new JSONObject();
                o.put("nombre",         s.optString("nombre"));
                o.put("anoNacimiento",  s.optLong("ano_nacimiento"));
                o.put("club",           s.optString("club"));
                o.put("categoria",      s.optString("categoria"));
                o.put("genero",         s.optString("genero"));
                arr.put(o);
            }
            prefs(ctx).edit().putString(KEY_FAVS, arr.toString()).apply();
        } catch (JSONException ignored) {}
    }

    // ==========================================
    // HISTORIAL (caché local + sync API)
    // ==========================================

    public static List<String> getHistorial(Context ctx) {
        List<String> lista = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(ctx).getString(KEY_HIST, "[]"));
            for (int i = 0; i < arr.length(); i++) lista.add(arr.getString(i));
        } catch (JSONException ignored) {}
        return lista;
    }

    public static void addBusqueda(Context ctx, String query) {
        if (query == null || query.trim().length() < 2) return;
        String q = query.trim().toUpperCase();
        try {
            JSONArray old = new JSONArray(prefs(ctx).getString(KEY_HIST, "[]"));
            JSONArray arr = new JSONArray();
            arr.put(q);
            for (int i = 0; i < old.length() && arr.length() < MAX_HIST; i++)
                if (!q.equals(old.getString(i))) arr.put(old.getString(i));
            prefs(ctx).edit().putString(KEY_HIST, arr.toString()).apply();
        } catch (JSONException ignored) {}

        String token = getToken(ctx);
        if (token != null) ApiClient.addBusqueda(token, q, new ApiClient.Callback() {
            @Override public void onSuccess(org.json.JSONObject r) {}
            @Override public void onError(String e) {}
        });
    }

    public static void eliminarBusqueda(Context ctx, String query) {
        try {
            JSONArray old = new JSONArray(prefs(ctx).getString(KEY_HIST, "[]"));
            JSONArray arr = new JSONArray();
            for (int i = 0; i < old.length(); i++)
                if (!query.equals(old.getString(i))) arr.put(old.getString(i));
            prefs(ctx).edit().putString(KEY_HIST, arr.toString()).apply();
        } catch (JSONException ignored) {}

        String token = getToken(ctx);
        if (token != null) ApiClient.removeBusqueda(token, query, new ApiClient.Callback() {
            @Override public void onSuccess(org.json.JSONObject r) {}
            @Override public void onError(String e) {}
        });
    }

    public static void borrarHistorial(Context ctx) {
        prefs(ctx).edit().putString(KEY_HIST, "[]").apply();
        String token = getToken(ctx);
        if (token != null) ApiClient.clearHistorial(token, new ApiClient.Callback() {
            @Override public void onSuccess(org.json.JSONObject r) {}
            @Override public void onError(String e) {}
        });
    }

    public static void setHistorialFromServer(Context ctx, JSONArray hist) {
        try {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < hist.length(); i++) arr.put(hist.getString(i));
            prefs(ctx).edit().putString(KEY_HIST, arr.toString()).apply();
        } catch (JSONException ignored) {}
    }
}