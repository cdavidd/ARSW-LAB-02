package edu.eci.arsw.highlandersim;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class HighlanderismTest {

    private CopyOnWriteArrayList<Immortal> immortals = new CopyOnWriteArrayList<>();
    private int n;
    private int health;
    private int defaultDamage;
    private ImmortalUpdateReportCallback ucb;
    private int invariant;

    @Before
    public void setup(){
        n = 85;
        health = 100;
        defaultDamage = 10;
        invariant = n * health;
        ucb = new TextAreaUpdateReportCallback(new JTextArea(),new JScrollPane());
        immortals = new CopyOnWriteArrayList<>();
        for (int i = 0; i < n ; i++){
            immortals.add(new Immortal("im"+i,immortals,health,defaultDamage,ucb));
        }
        for (Immortal im : immortals){
            im.start();
        }
    }

    @Test
    public void testNonChangingInvariant() {
        int testInvariant = getInvariant();
        assertTrue(invariant == testInvariant);
        resume();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail();
        }
        testInvariant = getInvariant();
        assertTrue(invariant == testInvariant);
        stop();
    }

    private void stop() {
        ControlFrame.stop = true;
    }


    private int getInvariant(){
        ControlFrame.pausa = true;

        //Esperar a que todos se pausen
        synchronized (ControlFrame.healthMonitor) {
            try {
                if (Immortal.cantidadPausados.get() != immortals.size()) { ControlFrame.healthMonitor.wait();}
            } catch (InterruptedException ex) {
                fail();
            }
        }
        int sum = 0;
        for (Immortal im : immortals) {
            sum += im.getHealth();
        }
        return sum;
    }

    private void resume(){
        synchronized (Immortal.immortalMonitor) {
            ControlFrame.pausa = false;
            Immortal.cantidadPausados.set(0);
            Immortal.immortalMonitor.notifyAll();
        }
    }

}
