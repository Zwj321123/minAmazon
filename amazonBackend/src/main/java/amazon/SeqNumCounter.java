package amazon;

public class SeqNumCounter {
    private static SeqNumCounter counter_obj = null;
    private static int next_counter;
    private static int current_id;

    /**
     * Private Constructor
     */
    private SeqNumCounter(){
        next_counter = 1;
    }

    public static SeqNumCounter getInstance(){
        if (counter_obj == null){
            synchronized(SeqNumCounter.class){
                if (counter_obj == null){
                    counter_obj = new SeqNumCounter();
                }
            }
        }
        current_id = next_counter;
        next_counter++;
        return counter_obj;
    }

    public int getCurrent_seqnum() {
        return current_id;
    }

}
