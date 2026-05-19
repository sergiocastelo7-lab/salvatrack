package com.sergi.salvatrackjava;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    public static final String BASE_URL = "http://10.0.2.2:8000/api";

    private static final ExecutorService executor   = Executors.newCachedThreadPool();
    private static final Handler         mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String mensaje);
    }

    // ---- AUTH ----

    public static void register(String nombre, String password, Callback cb) {
        executor.execute(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("nombre", nombre);
                b.put("password", password);
                ok(post("/register/", b, null), cb);
            } catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    public static void login(String nombre, String password, Callback cb) {
        executor.execute(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("nombre", nombre);
                b.put("password", password);
                ok(post("/login/", b, null), cb);
            } catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    public static void deleteAccount(String token, Callback cb) {
        executor.execute(() -> {
            try { ok(delete("/delete-account/", token, null), cb); }
            catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    // ---- FAVORITOS ----

    public static void toggleFavorito(String token, Athlete a, Callback cb) {
        executor.execute(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("nombre",         a.getNombre());
                b.put("ano_nacimiento", a.getAnoNacimiento());
                b.put("club",           a.getClub()      != null ? a.getClub()      : "");
                b.put("categoria",      a.getCategoria() != null ? a.getCategoria() : "");
                b.put("genero",         a.getGenero()    != null ? a.getGenero()    : "");
                ok(post("/favoritos/toggle/", b, token), cb);
            } catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    // ---- HISTORIAL ----

    public static void addBusqueda(String token, String query, Callback cb) {
        executor.execute(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("query", query);
                ok(post("/historial/add/", b, token), cb);
            } catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { /* silencioso */ }
        });
    }

    public static void removeBusqueda(String token, String query, Callback cb) {
        executor.execute(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("query", query);
                ok(delete("/historial/remove/", token, b), cb);
            } catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { /* silencioso */ }
        });
    }

    public static void clearHistorial(String token, Callback cb) {
        executor.execute(() -> {
            try { ok(delete("/historial/clear/", token, null), cb); }
            catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)  { /* silencioso */ }
        });
    }

    // ---- CRONO ----

    public static void guardarTiempo(String token, JSONObject datos, Callback cb) {
        executor.execute(() -> {
            try { ok(post("/crono/guardar/", datos, token), cb); }
            catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    public static void getTiempos(String token, int limite, Callback cb) {
        executor.execute(() -> {
            try { ok(get("/crono/tiempos/?limit=" + limite, token), cb); }
            catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    public static void editarTiempo(String token, int id, String nombre,
                                    String prueba, String piscina, Callback cb) {
        executor.execute(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("nombre",  nombre);
                b.put("prueba",  prueba);
                b.put("piscina", piscina);
                ok(put("/crono/editar/" + id + "/", b, token), cb);
            } catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    public static void eliminarTiempo(String token, int id, Callback cb) {
        executor.execute(() -> {
            try { ok(delete("/crono/eliminar/" + id + "/", token, null), cb); }
            catch (ApiException e) { err(e.getMessage(), cb); }
            catch (Exception e)    { err("Error de conexión.", cb); }
        });
    }

    // ---- HTTP INTERNOS ----

    private static JSONObject post(String ep, JSONObject body, String token) throws Exception {
        HttpURLConnection c = conn(ep, "POST", token);
        c.setDoOutput(true);
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        return leer(c);
    }

    private static JSONObject put(String ep, JSONObject body, String token) throws Exception {
        HttpURLConnection c = conn(ep, "PUT", token);
        c.setDoOutput(true);
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        return leer(c);
    }

    private static JSONObject get(String ep, String token) throws Exception {
        return leer(conn(ep, "GET", token));
    }

    private static JSONObject delete(String ep, String token, JSONObject body) throws Exception {
        HttpURLConnection c = conn(ep, "DELETE", token);
        if (body != null) {
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        return leer(c);
    }

    private static HttpURLConnection conn(String ep, String method, String token) throws Exception {
        URL url = new URL(BASE_URL + ep);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("Accept", "application/json");
        if (token != null) c.setRequestProperty("Authorization", "Token " + token);
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        return c;
    }

    private static JSONObject leer(HttpURLConnection c) throws Exception {
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 400 ? c.getErrorStream() : c.getInputStream(),
                StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        JSONObject json = new JSONObject(sb.toString());
        if (code >= 400) throw new ApiException(json.optString("error", "Error " + code));
        return json;
    }

    private static void ok(JSONObject r, Callback cb) {
        mainHandler.post(() -> cb.onSuccess(r));
    }

    private static void err(String msg, Callback cb) {
        mainHandler.post(() -> cb.onError(msg));
    }

    static class ApiException extends Exception {
        ApiException(String m) { super(m); }
    }
}