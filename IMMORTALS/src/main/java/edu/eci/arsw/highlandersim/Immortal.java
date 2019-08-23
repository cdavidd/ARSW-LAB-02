package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    public static final AtomicInteger cantidadPausados = new AtomicInteger(0);

    public static Object immortalMonitor = new Object();

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public void run() {
        while (true) {
            if (!ControlFrame.pausa) {
                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);
                try{
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                pausar();
            }
        }

    }

    private void pausar() {
        avisar(); //Ultimo thread en pausar avisa que se puede obtener la vida
        synchronized (immortalMonitor) {
            if (ControlFrame.pausa) {
                try {
                    immortalMonitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void avisar(){
        cantidadPausados.incrementAndGet();
        synchronized (ControlFrame.healthMonitor){
            if (cantidadPausados.get() == immortalsPopulation.size()){
                ControlFrame.healthMonitor.notify();
            }
        }
    }

    public void fight(Immortal i2) {
       Immortal immortalOne = getId() > i2.getId() ? this : i2;
       Immortal immortalTwo = getId() > i2.getId() ? i2 : this;

        synchronized (immortalOne){
            synchronized (immortalTwo){
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    }




    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
