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
    
        private Queue<Integer> queue = null;
    
        private int dataSeed = 0;
        private Random rand=null;
        private final long stockLimit;
    
        public Producer(Queue<Integer> queue,long stockLimit) {
            this.queue = queue;
            rand = new Random(System.currentTimeMillis());
            this.stockLimit=stockLimit;
        }
    
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
 3) 
