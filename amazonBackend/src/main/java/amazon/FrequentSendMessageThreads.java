package amazon;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class FrequentSendMessageThreads {
    private Map<Long, Timer> sendMessageThreads;
    public FrequentSendMessageThreads(){
        sendMessageThreads = new ConcurrentHashMap<>();
    }

    public void addNewFrequentThread(long seqnum, Timer thread){
        sendMessageThreads.put(seqnum, thread);
    }

    public boolean findACK(long ack){
        if (sendMessageThreads.containsKey(ack)){
            return true;
        }
        return false;
    }

    public void killThread(long ack){
        sendMessageThreads.get(ack).cancel();
        sendMessageThreads.remove(ack);
    }

}
