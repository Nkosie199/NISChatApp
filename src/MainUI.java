
/**
 *
 * @author adam
 */
public class MainUI {
    // public class TestThread {
    static long time1 = System.currentTimeMillis(); //start time
    static long time2 = System.currentTimeMillis(); //current time
    static long timeout =time2-time1;
    static int limit = 30*60000; // 35 (minutes) x 60000 (a minute in milliseconds)  
    
    public static void main(String args[]) {
        
        Server R1 = new Server( "Thread-1");
        R1.start();
        Server R2 = new Server( "Thread-2");
        R2.start(); 
//        Server R3 = new Server( "Thread-3");
//        R3.start();
//        Server R4 = new Server( "Thread-4");
//        R4.start();
 
//        System.out.println("Timeout: "+ timeout);
//        System.out.println("Limit: "+limit);
        while (timeout < limit){
            if (R1.isDead()){ //if the thread dies create a new 1
                System.out.println("Thead-1 just DIED! CREATING A NEW ONE...");
                R1 = new Server( "Thread-1");
                R1.start();   
                //
                threadCreator();
            }
            
            if (R2.isDead()){ //if the thread dies create a new 1
                System.out.println("Thead-2 just DIED! CREATING A NEW ONE...");
                R2 = new Server( "Thread-2");
                R2.start();     
                //
                threadCreator();
            }
//            if (R3.isDead()){ //if the thread dies create a new 1
//                System.out.println("Thead-3 just DIED! CREATING A NEW ONE...");
//                R3 = new Server( "Thread-3");
//                R3.start();   
//                //
//                threadCreator();
//            }
//            if (R4.isDead()){ //if the thread dies create a new 1
//                System.out.println("Thead-4 just DIED! CREATING A NEW ONE...");
//                R4 = new Server( "Thread-4");
//                R4.start();     
//                //
//                threadCreator();
//            }
            time2 = System.currentTimeMillis();
            timeout = time2-time1;
            //System.out.println(timeout);
        }
        quit();
        
    } 
    
    public static synchronized void threadCreator(){         
        time1 = System.currentTimeMillis();
        timeout =time2-time1;
        System.out.println("Timeout changed to: "+timeout);     
    }
    
    public static void quit(){
        System.out.println("APPLICATION HAS OFFICIALLY TIMED-OUT. PLEASE REFRESH.");
    }
   
}
