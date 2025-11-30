package web;

import org.java_websocket.WebSocket;
import controlador.ControladorPartida;
import modelo.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptadorJuego {
    
    private ServidorWeb servidor;
    private ControladorPartida controlador;
    private Partida partida;
    
    public AdaptadorJuego(ServidorWeb servidor) {
        this.servidor = servidor;
    }
    
    public void setControlador(ControladorPartida controlador) {
        this.controlador = controlador;
        controlador.setAdaptadorWeb(this);
    }
    
    public void setPartida(Partida partida) {
        this.partida = partida;
    }
    
    public void procesarAccion(WebSocket cliente, String accion, Map<String, Object> datos) {
        System.out.println("[ADAPTADOR] Procesando acción: " + accion);
        
        switch (accion) {
            case "LANZAR_DADO":
                manejarLanzarDado(cliente);
                break;
                
            case "MOVER_FICHA":
                manejarMoverFicha(cliente, datos);
                break;
                
            case "OBTENER_ESTADO":
                manejarObtenerEstado(cliente);
                break;
                
            case "INICIAR_PARTIDA":
                manejarIniciarPartida(cliente, datos);
                break;
                
            default:
                System.out.println("[ADAPTADOR] Acción desconocida: " + accion);
        }
    }
    
    private void manejarLanzarDado(WebSocket cliente) {
        if (controlador == null || partida == null) {
            enviarError(cliente, "No hay partida activa");
            return;
        }
        
        int valorDado = controlador.lanzarDadoWeb();
        List<Map<String, Object>> fichasDisponibles = controlador.obtenerFichasDisponiblesWeb(valorDado);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("tipo", "RESULTADO_DADO");
        respuesta.put("valor", valorDado);
        respuesta.put("fichasDisponibles", fichasDisponibles);
        respuesta.put("turnoActual", partida.getTurnoActual().getNombre());
        
        servidor.enviarMensaje(cliente, respuesta);
    }
    
    private void manejarMoverFicha(WebSocket cliente, Map<String, Object> datos) {
        if (controlador == null) {
            enviarError(cliente, "No hay partida activa");
            return;
        }
        
        int fichaId = ((Double) datos.get("fichaId")).intValue();
        int pasos = ((Double) datos.get("pasos")).intValue();
        
        boolean exito = controlador.moverFichaWeb(fichaId, pasos);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("tipo", "MOVIMIENTO_RESULTADO");
        respuesta.put("exito", exito);
        respuesta.put("estadoJuego", controlador.obtenerEstadoJuegoWeb());
        
        servidor.enviarMensaje(cliente, respuesta);
    }
    
    private void manejarObtenerEstado(WebSocket cliente) {
        if (partida == null) {
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("tipo", "ESTADO_JUEGO");
            respuesta.put("partidaActiva", false);
            servidor.enviarMensaje(cliente, respuesta);
            return;
        }
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("tipo", "ESTADO_JUEGO");
        respuesta.put("partidaActiva", true);
        respuesta.put("estado", controlador.obtenerEstadoJuegoWeb());
        
        servidor.enviarMensaje(cliente, respuesta);
    }
    
    private void manejarIniciarPartida(WebSocket cliente, Map<String, Object> datos) {
        // Por ahora crear partida de prueba
        Partida nuevaPartida = new Partida(1);
        
        Jugador j1 = new Jugador(1, "Jugador 1", "Amarillo");
        Jugador j2 = new Jugador(2, "Jugador 2", "Azul");
        
        nuevaPartida.agregarJugador(j1);
        nuevaPartida.agregarJugador(j2);
        nuevaPartida.iniciarPartida();
        
        this.partida = nuevaPartida;
        
        ControladorPartida nuevoControlador = new ControladorPartida(
            nuevaPartida, 
            null, // Sin vista de consola
            null, // Sin scanner
            1     // Jugador local
        );
        
        setControlador(nuevoControlador);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("tipo", "PARTIDA_INICIADA");
        respuesta.put("mensaje", "Partida creada exitosamente");
        respuesta.put("estado", nuevoControlador.obtenerEstadoJuegoWeb());
        
        servidor.enviarMensaje(cliente, respuesta);
    }
    
    private void enviarError(WebSocket cliente, String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("tipo", "ERROR");
        error.put("mensaje", mensaje);
        servidor.enviarMensaje(cliente, error);
    }
}