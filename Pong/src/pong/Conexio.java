/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pong;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ssb
 */
public class Conexio extends Thread {

    private ServerSocket sc;
    private DataInputStream recive;
    private Socket so;

    public Conexio() {
        try {
            sc = new ServerSocket(7874);
            so = new Socket();
        } catch (IOException ex) {
            Logger.getLogger(Conexio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            so = sc.accept();
            System.out.println("Controlador connectat");
            recive = new DataInputStream(so.getInputStream());
            int d = 0;
            while (true) {
                try {
                    d = recive.readInt();
                } catch (IOException ex) {
                    System.out.println("Mando desconectat");
                }
                Pong.key = d;
            }
        } catch (IOException ex) {
            Logger.getLogger(Conexio.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
