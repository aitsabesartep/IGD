package pong;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import javafx.stage.Screen;
import javax.swing.JFrame;

public class Pong extends JFrame implements KeyListener {

    private int windowWidth = 800;
    private int windowHeight = 600;
    private Paleta paleta;
    private Pelota pelota;

    public static int key = 0;
    private long goal;
    private long tiempoDemora = 8;

    private int Buenas;
    private int Malas;

    private Conexio conec;

    public static void main(String[] args) {
        new Pong();
    }

    public Pong() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(windowWidth, windowHeight);
        this.setResizable(false);
        this.setLocation(100, 100);
        this.setVisible(true);

        this.createBufferStrategy(2);

        this.addKeyListener(this);

        inicializoObjetos();

        while (true) {
            pelota();
            sleep();
        }

    }

    private void chequearColision() {
        if ((pelota.y >= windowHeight-75) && (pelota.y <= windowHeight-60)) {
            if ((pelota.x > paleta.x - paleta.ancho/2) && (pelota.x < paleta.x + paleta.ancho/2)) {
                pelota.veloY = - pelota.veloY;
            }
            if (pelota.veloY < 0) {
                Buenas++;
            }
        }
    }

    private void pelota() {

        pelota.x = pelota.x + pelota.veloX;
        pelota.y = pelota.y + pelota.veloY;

        chequearColision();

        if (pelota.x <= 0 || pelota.x >= windowWidth) {
            pelota.veloX = -pelota.veloX;
            Malas++;
        }

        if (pelota.y <= 20 || pelota.y >= (windowHeight)) // 20 y 40 son valores de compensacion
        {
            pelota.veloY = -pelota.veloY;
        }

        dibujoPantalla();
    }

    private void inicializoObjetos() {
        conec = new Conexio();
        conec.start();
        pelota = new Pelota(windowWidth / 2, windowHeight / 2, 5, -5);
        paleta = new Paleta(windowWidth / 2, 150);
    }

    private void dibujoPantalla() {

        BufferStrategy bf = this.getBufferStrategy();
        Graphics g = null;

        try {
            g = bf.getDrawGraphics();

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, windowWidth, windowHeight);

            muestroPuntos(g);
            dibujoPelota(g);
            dibujoPaletas(g);

        } finally {
            g.dispose();
        }
        bf.show();

        Toolkit.getDefaultToolkit().sync();
    }

    private void dibujoPelota(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillOval(pelota.x, pelota.y, 20, 20);
    }

    private void dibujoPaletas(Graphics g) {
        switch (key) {
            case KeyEvent.VK_LEFT:
                if (paleta.x > 0) {
                    paleta.x = paleta.x - 6;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (paleta.x < windowWidth - 80) {
                    paleta.x = paleta.x + 6;
                }
                break;
            case KeyEvent.VK_E:
                System.exit(0);

        }

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(paleta.x, windowHeight - 75, paleta.ancho, 15);
        //g.fillRect(710, paleta.x, 15, paleta.ancho);
    }

    private void muestroPuntos(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Buenas: " + Buenas, 20, 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Malas: " + Malas, 20, 70);
    }

    private void sleep() {
        goal = (System.currentTimeMillis() + tiempoDemora);
        while (System.currentTimeMillis() < goal) {

        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        key = e.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        key = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
