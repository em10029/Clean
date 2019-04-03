/*
 * ©Informática Atlántida 2019.
 * Derechos Reservados.
 * 
 * Este software es propiedad intelectual de Informática Atlántida (Infatlan). La información contenida
 * es de carácter confidencial y no deberá revelarla. Solamente podrá utilizarlo de conformidad con los
 * términos del contrato suscrito con Informática Atlántida S.A.
 */
package hn.bancatlan.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal de aplicación.
 *
 * @author Erick Fabricio Martínez Castellanos
 * (<a href='mailto:efmartinez@bancatlan.hn'>efmartinez@bancatlan.hn</a>)
 * @version 1.0.0 02-abr-2019
 */
public class Main {

    /**
     * Directorio raíz del cual se eliminaran los ficheros.
     */
    private static String CLEAN_ROOT;

    /**
     * Días transcurridos desde la creación del fichero.
     */
    private static int CLEAN_DAYS;

    /**
     * Carga el archivo <b>Clean.properties</b> y obtiene las variables de
     * propiedades.
     *
     * @return boolean True si todas la variables han sido cargadas
     * exitosamente, False en caso contrario.
     */
    private static boolean configurar() {

        boolean config = false; //determinara si la configuracion del programa es correcta
        Properties propiedades = new Properties(); //prodiedades del programa
        InputStream configuracion = null; //archivo de propiedades

        try {
            //Obteniento la ruta absoluta del archivo de propiedades
            String ruta = System.getProperty("user.dir");
            String rutaAbsoluta = ruta + "/configs/Clean.properties";

            //Cargando el archivo de configuracion
            configuracion = new FileInputStream(rutaAbsoluta);
            propiedades.load(configuracion);

            //------------OBTENIENDO LAS VARIABLES DE CONFIGURACION------------//            
            CLEAN_ROOT = propiedades.getProperty("clean.root").trim();
            CLEAN_DAYS = Integer.valueOf(propiedades.getProperty("clean.days").trim());

            configuracion.close();
            config = true;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "ERROR con el archivo de congiguración Clean.properties no se encuentra.", ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "ERROR con el archivo de congiguración Clean.properties.", ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "ERROR con el archivo de congiguración Clean.properties las propiedades han sido renombradas.", ex);
        }

        return config;
    }

    /**
     * Obtiene la fecha y la hora según formato requerido.
     *
     * @param formato: Formato de fecha que se desea. Ejemplo: DD/MM/YYYY ->
     * 10/11/1994
     * @return Fecha del sistema.
     */
    private static String getFechaHoraActual(String formato) {
        SimpleDateFormat fecha = new SimpleDateFormat(formato);
        Date fechaActual = new Date(System.currentTimeMillis());
        return fecha.format(fechaActual);
    }

    /**
     * Preparación de aplicación.
     */
    private static void init() throws IOException, ParseException {
        //1- Carga de variables de configuración.
        configurar();

        //2- Log de configuración.        
        print("----------------------------------------------------");
        print("Limpieza de archivos " + getFechaHoraActual("dd/MM/yyyy - HH.mm.ss"));
        print("----------------------------------------------------");
        print("CLEAN_ROOT: " + CLEAN_ROOT);
        print("CLEAN_DAYS: " + CLEAN_DAYS);
        print("----------------------------------------------------");

        //3- Verificación de configuración.
        File root = new File(CLEAN_ROOT); //Directorio principal
        if (root.isDirectory()) {
            clean();
        } else {
            print("Error la propiedad CLEAN_ROOT: " + CLEAN_ROOT + " no es un directorio valído.");
        }
    }

    /**
     * Escribe en el archivo .log del programa. Se utiliza para registrar los
     * procesos realizados.
     *
     * @param proceso
     */
    private static void log(String proceso) {

        FileWriter lector = null;
        PrintWriter escritor = null;

        try {
            //Obteniento la ruta absoluta del archivo App.log
            String ruta = System.getProperty("user.dir");
            String rutaAbsoluta = ruta + "/logs/App.log";

            //#1 Abrir
            lector = new FileWriter(rutaAbsoluta, true);
            escritor = new PrintWriter(lector);

            //#2 Escribir
            escritor.write(proceso + "\r\n");

            //#3 Cerrar
            escritor.close();
            lector.close();

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Registra el proceso en el log de la aplicación y lo muestra en la
     * consola.
     *
     * @param string
     */
    private static void print(String string) {
        System.out.println(string);
        log(string);
    }

    /**
     * Elimina las carpetas.
     *
     * @throws IOException
     * @throws ParseException
     */
    private static void clean() throws IOException, ParseException {

        File root = new File(CLEAN_ROOT); //Directorio principal

        File[] directories = root.listFiles(); //Lista de archivos y directorios

        int cont = 0; //Contador de archivos

        if (directories != null) {

            print("## --> Archivo --> Fecha creación --> Días transcurridos --> Mensaje");

            for (File file : directories) { //Recorrido de archivos     

                //if (file.isDirectory()) { //Solo directorios
                cont++; //Incremento del contador

                //++++++++++++++++++++++++++++++++++++
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class); //Propiedades de la carpeta
                long cTime = attr.creationTime().toMillis(); //Fecha de creacion
                ZonedDateTime t = Instant.ofEpochMilli(cTime).atZone(ZoneId.of("UTC")); //Formato de fecha
                String dateCreated = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(t); //Fecha 

                //++++++++++++++++++++++++++++++++++++
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date fechaInicial = dateFormat.parse(dateCreated);
                Date fechaFinal = dateFormat.parse(getFechaHoraActual("dd/MM/yyyy"));

                int dias = (int) ((fechaFinal.getTime() - fechaInicial.getTime()) / 86400000);

                if (dias > CLEAN_DAYS) {
                    if (file.delete()) {
                        print(String.format("%02d", cont) + " --> " + file.getName() + " --> " + dateCreated + " --> " + dias + " --> Eliminado");
                    } else {
                        print(String.format("%02d", cont) + " --> " + file.getName() + " --> " + dateCreated + " --> " + dias + " --> ERROR al eliminar");
                    }
                } else {
                    print(String.format("%02d", cont) + " --> " + file.getName() + " --> " + dateCreated + " --> " + dias + " --> No se elimino");
                }
                //}

            }
        } else {
            print("No hay ficheros en el directorio: " + CLEAN_ROOT);
        }

    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        print("INICIO ********************************************");
        init();
        print("FIN    ********************************************\r\n");
    }

}
