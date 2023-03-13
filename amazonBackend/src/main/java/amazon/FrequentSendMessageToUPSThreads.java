package amazon;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class FrequentSendMessageToUPSThreads {
    private Map<Integer, Timer> sendMessageThreads;
    public FrequentSendMessageToUPSThreads(){
        sendMessageThreads = new ConcurrentHashMap<>();
    }

    public void addNewFrequentThread(int seqnum, Timer thread){
        sendMessageThreads.put(seqnum, thread);
    }

    public boolean findACK(int ack){
        if (sendMessageThreads.containsKey(ack)){
            return true;
        }
        return false;
    }

    public void killThread(int ack){
        sendMessageThreads.get(ack).cancel();
        sendMessageThreads.remove(ack);
    }
}
