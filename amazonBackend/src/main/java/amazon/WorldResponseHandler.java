package amazon;

import amazon.proto.WorldAmazonProtocol;

import java.io.OutputStream;

import static amazon.Utils.sendMsgTo;

//use to deal with all kinds of response from world
public class WorldResponseHandler {

    public void handleAResponsesFromWorld(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        sendAckToWorldAndDealwithAPurchaseMore(responseFromWorld, worldOutput);
        sendAckToWorldAndDealwithAPacked(responseFromWorld, worldOutput);
        sendAckToWorldAndDealwithALoaded(responseFromWorld, worldOutput);
        sendAckToWorldAndDealwithFinished(responseFromWorld, worldOutput);
        sendAckToWorldAndDealwithError(responseFromWorld, worldOutput);
        //when receive ACKs, we handle it in amazonDaemon
        //sendAckToWorldAndDealwithACKs(responseFromWorld, worldOutput);
        sendAckToWorldAndDealwithAPackage(responseFromWorld, worldOutput);
    }


    // TODO: 1 PruchaseMore
    public void sendAckToWorldAndDealwithAPurchaseMore(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        sendAckToWorldAPurchaseMore(responseFromWorld, worldOutput);
        //DealwithResponseAPurchaseMore(responseFromWorld);

    }

    private void sendAckToWorldAPurchaseMore(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        WorldAmazonProtocol.ACommands.Builder acommands = WorldAmazonProtocol.ACommands.newBuilder();
        boolean existence = false;
        for(WorldAmazonProtocol.APurchaseMore aPurchaseMore: responseFromWorld.getArrivedList()){
            System.out.println("received PurchaseMore from world with seqnum:" + aPurchaseMore.getSeqnum());
            acommands.addAcks(aPurchaseMore.getSeqnum());
            existence = true;
        }
        if(existence) {
            synchronized (worldOutput) {
                System.out.println("Send back ack for APurchaseMore:"+ acommands.getAcks(0));
                sendMsgTo(acommands.build(), worldOutput);
            }
        }
    }

//    private void DealwithResponseAPurchaseMore(WorldAmazonProtocol.AResponses responseFromWorld){
//
//    }


    //TODO: 2 APacked
    private void sendAckToWorldAndDealwithAPacked(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        sendAckToWorldAPacked(responseFromWorld, worldOutput);
    }

    private void sendAckToWorldAPacked(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        WorldAmazonProtocol.ACommands.Builder acommands = WorldAmazonProtocol.ACommands.newBuilder();
        boolean existence = false;
        for(WorldAmazonProtocol.APacked aPacked: responseFromWorld.getReadyList()){
            acommands.addAcks(aPacked.getSeqnum());
            existence = true;
        }
        if(existence) {
            synchronized (worldOutput) {
                sendMsgTo(acommands.build(), worldOutput);
            }
        }
    }


    //TODO: 3 ALoaded
    private void sendAckToWorldAndDealwithALoaded(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        sendAckToWorldALoaded(responseFromWorld, worldOutput);
    }

    private void sendAckToWorldALoaded(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        WorldAmazonProtocol.ACommands.Builder acommands = WorldAmazonProtocol.ACommands.newBuilder();
        boolean existence = false;
        for(WorldAmazonProtocol.ALoaded aLoaded: responseFromWorld.getLoadedList()){
            acommands.addAcks(aLoaded.getSeqnum());
            existence = true;
        }
        if(existence) {
            synchronized (worldOutput) {
                sendMsgTo(acommands.build(), worldOutput);
            }
        }
    }


    //TODO: 4 Finished
    private void sendAckToWorldAndDealwithFinished(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        //do not need to send ack
    }

    //TODO: 5 Error
    private void sendAckToWorldAndDealwithError(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        sendAckToWorldAError(responseFromWorld, worldOutput);
    }

    private void sendAckToWorldAError(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        WorldAmazonProtocol.ACommands.Builder acommands = WorldAmazonProtocol.ACommands.newBuilder();
        boolean existence = false;
        for(WorldAmazonProtocol.AErr aErr: responseFromWorld.getErrorList()){
            acommands.addAcks(aErr.getSeqnum());
            existence = true;
        }
        if(existence) {
            synchronized (worldOutput) {
                sendMsgTo(acommands.build(), worldOutput);
            }
        }
    }

    //TODO: 6 ACKs handled in amazonDaemon

    //TODO: 7 APackage
    private void sendAckToWorldAndDealwithAPackage(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        sendAckToWorldAPackage(responseFromWorld, worldOutput);
    }

    private void sendAckToWorldAPackage(WorldAmazonProtocol.AResponses responseFromWorld, OutputStream worldOutput){
        WorldAmazonProtocol.ACommands.Builder acommands = WorldAmazonProtocol.ACommands.newBuilder();
        boolean existence = false;
        for(WorldAmazonProtocol.APackage aPackage: responseFromWorld.getPackagestatusList()){
            acommands.addAcks(aPackage.getSeqnum());
            existence = true;
        }
        if(existence) {
            synchronized (worldOutput) {
                sendMsgTo(acommands.build(), worldOutput);
            }
        }
    }
}
