package web;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.google.gson.Gson;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Servidor WebSocket que conecta la interfaz web con el juego Java
 */
public class ServidorWeb extends WebSocketServer {
    
    private int puertoReal; // ⬅️ NUEVO: guardar el puerto real usado
    private Gson gson;
    private AdaptadorJuego adaptador;
    
    /**
     * Constructor que busca un puerto disponible automáticamente
     */
    public ServidorWeb() {
        super(new InetSocketAddress(0)); // ⬅️ 0 = puerto automático
        this.gson = new Gson();
        this.adaptador = null;
    }
    
    public void setAdaptador(AdaptadorJuego adaptador) {
        this.adaptador = adaptador;
    }
    
    /**
     * Obtiene el puerto real que se está usando
     */
    public int getPuertoReal() {
        return puertoReal;
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[WEB] Cliente conectado: " + conn.getRemoteSocketAddress());
        
        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("tipo", "CONEXION_EXITOSA");
        mensaje.put("mensaje", "Conectado al servidor Parchis");
        enviarMensaje(conn, mensaje);
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[WEB] Cliente desconectado: " + conn.getRemoteSocketAddress());
    }
    
    @Override
    public void onMessage(WebSocket conn, String mensaje) {
        System.out.println("[WEB] Mensaje recibido: " + mensaje);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> datos = gson.fromJson(mensaje, Map.class);
            String accion = (String) datos.get("accion");
            
            if (adaptador != null) {
                adaptador.procesarAccion(conn, accion, datos);
            }
            
        } catch (Exception e) {
            System.err.println("[WEB] Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[WEB] Error: " + ex.getMessage());
        ex.printStackTrace();
    }
    
    @Override
    public void onStart() {
        // ⬅️ NUEVO: obtener el puerto real asignado
        puertoReal = getPort();
        System.out.println("\n================================================");
        System.out.println("   SERVIDOR WEBSOCKET ACTIVO");
        System.out.println("================================================");
        System.out.println("  Puerto: " + puertoReal);
        System.out.println("  WebSocket: ws://localhost:" + puertoReal);
        System.out.println("================================================\n");
    }
    
    public void enviarMensaje(WebSocket conn, Map<String, Object> datos) {
        if (conn != null && conn.isOpen()) {
            String json = gson.toJson(datos);
            conn.send(json);
        }
    }
    
    public void broadcast(Map<String, Object> datos) {
        String json = gson.toJson(datos);
        broadcast(json);
    }
}