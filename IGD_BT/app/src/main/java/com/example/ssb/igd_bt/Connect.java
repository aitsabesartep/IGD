package com.example.ssb.igd_bt;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Created by ssb on 26/3/16.
 */
public class Connect extends AsyncTask {

    public Socket sc;
    public String ip;
    public String port;
    public Semaphore conect;
    public int frec;
    public int tam;
    public DataOutputStream send;

    public Connect(String ip_p, String port_p, Semaphore s, String fre, int t){
        ip = ip_p;
        port = port_p;
        sc = new Socket();
        conect = s;
        frec = Integer.parseInt(fre);
        tam = t;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            sc.connect(new InetSocketAddress(ip, Integer.parseInt(port)));
            PreferenceScreen.conectat = true;
            send = new DataOutputStream(sc.getOutputStream());
        } catch (IOException e) {
            PreferenceScreen.conectat = false;
            sc = null;
        }finally {
            conect.release();
        }
    }

    @Override
    protected Object doInBackground(Object[] params) {
        while(!isCancelled() && sc != null){
            sleep(frec);

            float mediax = 0;
            float mediay = 0;
            float mediaz = 0;
            int le = 0;
            int ri = 0;

            try {
                MainActivity.sem.acquire();
                for (int i = 0; i < tam; i++) {
                    mediax = mediax + MainActivity.bufferx[i];
                    mediay = mediay + MainActivity.buffery[i];
                    mediaz = mediaz + MainActivity.bufferz[i];
                }
                MainActivity.sem.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                MainActivity.sem1.acquire();
                le = MainActivity.left;
                MainActivity.left = 0;
                MainActivity.sem1.release();
                MainActivity.sem2.acquire();
                MainActivity.right=0;
                ri = MainActivity.right;
                MainActivity.sem2.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mediax = mediax/tam;
            mediay = mediay/tam;
            mediaz = mediaz/tam;

            try {
                send.write((mediax + "/" + mediay + "/" + mediaz + "/" + le + "/" + ri + "#\n").getBytes());
                System.out.println((mediax + "/" + mediay + "/" + mediaz + "/" + le + "/" + ri + "#\n").getBytes());
            } catch (IOException e) {
            }
        }
        return null;
    }

    private void sleep(int i){
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
