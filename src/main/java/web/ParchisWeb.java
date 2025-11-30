package web;

/**
 * Punto de entrada para la versión web del juego
 */
public class ParchisWeb {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n==============================================");
        System.out.println("     PARCHIS STAR - VERSION WEB");
        System.out.println("==============================================\n");
        
        System.out.println("[INFO] Iniciando servidor...");
        
        ServidorWeb servidor = new ServidorWeb();
        AdaptadorJuego adaptador = new AdaptadorJuego(servidor);
        servidor.setAdaptador(adaptador);
        
        // Iniciar servidor
        servidor.start();
        
        // Esperar a que el servidor esté listo
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Mostrar información del puerto
        System.out.println("\n[INFO] Para conectar desde tu navegador:");
        System.out.println("[INFO]   1. Abre tu HTML");
        System.out.println("[INFO]   2. Conecta a: ws://localhost:" + servidor.getPuertoReal());
        System.out.println("\n[INFO] Presiona Ctrl+C para detener el servidor\n");
        
        // Mantener el programa corriendo
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("\n[INFO] Servidor detenido");
            servidor.stop();
        }
    }
}