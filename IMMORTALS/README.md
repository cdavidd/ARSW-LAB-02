## Compile and run instructions
Entrar al directorio `ARSW-LAB-02/IMMORTALS`:
* **Para compilar:** Ejecutar `mvn package`
* **Para ejecutar StartProduction:** Ejecutar `mvn exec:java -Dexec.mainClass="edu.eci.arst.concprg.prodcons.StartProduction"`
* **Para ejecutar ControlFrame (Immortals):** Ejecutar `mvn exec:java -Dexec.mainClass="edu.eci.arsw.highlandersim.ControlFrame"`
* **Para ejecutar las pruebas:** Ejecutar `mvn test` 
                                      
## Part I

1) La siguiente imagen muestra el uso del CPU al correr el programa.
![](img/cpu.PNG)\
Este uso del CPU es debido a que el hilo de la clase `Consumer`
 pregunta a cada momento si se ha agregado un elemento a la cola de 
 los productos. Estas consultas innecesarias causan mayor uso de la 
 CPU.
 2) La siguiente imagen muestra el uso del CPU al 
 realizarlo más eficiente.
 ![](img/cpuMejorada.PNG)\
 Se realizó lo siguiente para reducir el consumo de la CPU.
 
    ```java
    public class Consumer extends Thread{
        @Override
        public void run() {
            while (true) {
                synchronized(queue){
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    int elem=queue.poll();
                    System.out.println("Consumer consumes "+elem);
                }
            }
        }
    }

    ```
    ```java
    public class Producer extends Thread {
        @Override
        public void run() {
            while (true) {
                dataSeed = dataSeed + rand.nextInt(100);
                System.out.println("Producer added " + dataSeed);
                synchronized(queue){
                    queue.add(dataSeed);
                    queue.notifyAll();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    ```
 3) Se verificó el consumo del CPU con un límite pequeño.\
 ![](img/p3.PNG)\
 El productor llena su lista y notifica 
 al consumidor para que este la vacíe.
 
    Clase `Producer`:
    ```java 
    synchronized(queue){
        while(queue.size()==stockLimit){
            queue.notifyAll();
            try {
                queue.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        dataSeed = dataSeed + rand.nextInt(100);
        System.out.println("Producer added " + dataSeed);
        queue.add(dataSeed);
    }
    ```
    Clase `Consumer`:
    ```java
    synchronized(queue){
        while (queue.isEmpty()) {
            queue.notifyAll();
            try {
                queue.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int elem=queue.poll();
        System.out.println("Consumer consumes "+elem);
    }
    ```

## Part II
1. *Review the “highlander-simulator” program, provided in the edu.eci.arsw.highlandersim package. This is a game in which:*
    * You have N immortal players. 
    * Each player knows the remaining N-1 player.
    * Each player permanently attacks some other immortal. The one who first attacks subtracts M life points from his opponent, and increases his own life points by the same amount. 
    * The game could never have a single winner. Most likely, in the end there are only two left, fighting indefinitely by removing and adding life points. 
2. *Review the code and identify how the functionality indicated above was implemented. Given the intention of the game, an invariant
should be that the sum of the life points of all
players is always the same (of course, in an instant
of time in which a time increase / reduction operation is not in
process ). For this case, for N players, what 
should this value be?*
    
    Para N jugadores la invariante sería la siguiente:
     *N* * `DEFAULT_IMMORTAL_HEALTH`
3. *Run the application and verify how the ‘pause and check’ option works. 
Is the invariant fulfilled?*
    
    No, la suma de los puntos de vida de todos los jugadores se ve alterado,
     disminuyen o aumentando de una forma aparentemente aleatoria.
4. *A first hypothesis that the race condition for this function 
(pause and check) is presented is that the program consults the list whose 
values ​​it will print, while other threads modify their values. 
To correct this, do whatever is necessary so that, before printing the current
results, all other threads are paused. Additionally, implement the ‘resume’
option.*

    Se utilizaron monitores y un entero atómico para determinar cuando todos los hilos estaban pausados para así poder obtener la vida de cada uno.

5. *Check the operation again 
(click the button many times). Is the invariant fulfilled or not ?.*

    La invariante aún no se está cumpliendo.

6. *Identify possible critical regions in regards to the fight of the immortals.
Implement a blocking strategy that avoids race conditions. Remember that if you 
need to use two or more ‘locks’ simultaneously, you can use nested synchronized
blocks:*

    El método `fight()` en la clase `Immortal` se consideró como una región critica. 

7. *After implementing your strategy, start running your program, and pay attention to whether it comes to a halt.
If so, use the jps and jstack programs to identify why the program stopped.*

    Se utiliza la herramienta jstack para identificar porque el programa se detuvo.
    
    ![](img/deadlock.JPG)
    
    El programa se detuvo debido a que ocurrió un deadlock.

8. *Consider a strategy to correct the problem identified above 
(you can review Chapter 15 of Java Concurrency in Practice again)*

    La estrategia utilizada para evitar el deadlock fue mantener el orden en el cual los objetos obtienen el bloqueo, a continuación, se muestra cómo se implementó:

    Clase `Immortal`:
    ```java
    public void fight(Immortal i2) {
       Immortal immortalOne = getId() > i2.getId() ? this : i2;
       Immortal immortalTwo = getId() > i2.getId() ? i2 : this;

        synchronized (immortalOne){
            synchronized (immortalTwo){
                if (i2.getHealth() > 0 && vivo) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    }
    ```

9. *Once the problem is corrected, rectify that the program continues to 
function consistently when 100, 1000 or 10000 immortals are executed.
If in these large cases the invariant begins to be breached again, 
you must analyze what was done in step 4.*
    
   El invariante no cambio al crear 10, 100 y 1000 hilos.
   
10. *An annoying element for the simulation is that at a certain point in it there are few living
 'immortals' making failed fights with 'immortals' already dead.
 It is necessary to suppress the immortal dead of the simulation as they die.*
    
    Respuesta en ANSWER.txt
    1. Analyzing the simulation operation scheme, could this create a race condition? Implement the functionality, run the simulation and see what problem arises when there are many 'immortals' in it. Write your conclusions about it in the file ANSWERS.txt. 
    2. Correct the previous problem WITHOUT using synchronization, since making access to the shared list of immortals sequential would make simulation extremely slow.

11.  *To finish, implement the STOP option.*

Cada hilo se detiene al momento de realizar la acción de stop.

```java
JButton btnStop = new JButton("STOP");
btnStop.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
            stop = true;
            btnPauseAndCheck.setEnabled(false);
            btnResume.setEnabled(false);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            System.err.println("Error stopping");
        }
        output.selectAll();
            output.replaceSelection("");
    }
});
```