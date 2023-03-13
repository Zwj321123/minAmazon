package amazon;

import amazon.proto.AmazonUPSProtocol;

import java.io.OutputStream;
import java.util.ArrayList;
import static amazon.Utils.sendMsgTo;

public class UPSResponseHandler {

    public void handleUMsgFromUPS(AmazonUPSProtocol.UMsg uMsg, OutputStream upsOutput){
        ArrayList<Integer> allNeedToResp = new ArrayList<>();
        for (AmazonUPSProtocol.UDeliverRsp uDeliverRsp: uMsg.getDeliveredList()){
            allNeedToResp.add(uDeliverRsp.getSeqnum());
        }
        for (AmazonUPSProtocol.UTruckSent uTruckSent: uMsg.getTrucksentList()){
            allNeedToResp.add(uTruckSent.getSeqnum());
        }
        for (AmazonUPSProtocol.UTruckArrived uTruckArrived: uMsg.getTruckarrivedList()){
            allNeedToResp.add(uTruckArrived.getSeqnum());
        }
        for (AmazonUPSProtocol.Exception exception: uMsg.getErrorList()){
            allNeedToResp.add(exception.getSeqnum());
        }
        if(allNeedToResp.size() > 0){
            AmazonUPSProtocol.AMsg.Builder aMsg = AmazonUPSProtocol.AMsg.newBuilder();
            for(int a: allNeedToResp){
                aMsg.addAcks(a);
            }
            synchronized (upsOutput){
                sendMsgTo(aMsg.build(), upsOutput);
            }
        }
    }

}
