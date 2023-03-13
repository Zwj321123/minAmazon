package amazon;

import amazon.proto.AmazonUPSProtocol;
import amazon.proto.AmazonUPSProtocol.*;
import amazon.proto.WorldAmazonProtocol;
import amazon.proto.WorldAmazonProtocol.*;
import static amazon.Utils.sendMsgTo;
import static amazon.Utils.recvMsgFrom;

import java.awt.image.PackedColorModel;
import java.io.*;
import java.lang.Exception;
import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AmazonDaemon {


    //private static final String WORLD_HOST = "vcm-23974.vm.duke.edu";
    //private static final int WORLD_PORTNUM = 23456;//for amazon
    private static final String WORLD_HOST = "localhost";
    private static final int WORLD_PORTNUM = 23456;//for amazon

    private static final String CONNECT_UPS_HOST = "vcm-24667.vm.duke.edu";//TODO:change to UPS hostname
    private static final int CONNECT_UPS_PORTNUM = 34567;

    private final ThreadPoolExecutor threadPoolExecutor;
    private Socket upsSocket;
    private Socket worldSocket;
    private InputStream worldInput;
    private OutputStream worldOutput;
    private Server frontendServer;
    private FrequentSendMessageThreads frequentSendMessageThreads;
    private FrequentSendMessageToUPSThreads upsFrequentSendMessageThreads;
    private APackageManagement aPackageManagement;
    private WorldResponseHandler worldResponseHandler;
    private UPSResponseHandler upsResponseHandler;
    public AmazonDaemon(){
        this.threadPoolExecutor = new ThreadPoolExecutor(50,100,10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));
        this.upsSocket = null;
        this.worldSocket = null;
        this.frequentSendMessageThreads = new FrequentSendMessageThreads();
        this.upsFrequentSendMessageThreads = new FrequentSendMessageToUPSThreads();
        this.aPackageManagement = new APackageManagement();
        this.worldResponseHandler = new WorldResponseHandler();
        this.upsResponseHandler = new UPSResponseHandler();
    }







    public static void main(String[] args) {
        long worldID = 1;
        AmazonDaemon amazonDaemon = new AmazonDaemon();
        amazonDaemon.handleAllThingsFromReceivingBuyRequestFromFrontend();
//        try {
//            amazonDaemon.connectUpsToGetWorldid();
//            //amazonDaemon.amazonConnectWorld(worldID);
//            amazonDaemon.handleAllThingsFromReceivingBuyRequestFromFrontend();
//
//        }catch (IOException e){
//            System.err.println(e.toString());
//        }
    }

    public void handleAllThingsFromReceivingBuyRequestFromFrontend(){
        threadPoolExecutor.prestartAllCoreThreads();
        startFrontendConnection();
        startUpsConnectionAfterGetWorldid();
        startWorldConnectionAfterInitWarehouse();
    }
    //listen to ups and get worldid, then connect to world and create 3 warehouses
    //after this function returns, then we should go on listening from client's buy request
    public void connectUpsToGetWorldid() throws IOException {
        System.out.println("Amazon is connecting to UPS to get worldID and try to get connection at UPS's port:" + CONNECT_UPS_PORTNUM);
        upsSocket = new Socket(CONNECT_UPS_HOST, CONNECT_UPS_PORTNUM);
        while(true) {

//            upsSocket = new Socket(CONNECT_UPS_HOST, CONNECT_UPS_PORTNUM);
            if(upsSocket.isConnected()) {
                System.out.println("Connected to UPS");
                InputStream input = upsSocket.getInputStream();
                OutputStream output = upsSocket.getOutputStream();
//                U2AWorldId.Builder u2AWorldId = U2AWorldId.newBuilder();


//                recvMsgFrom(u2AWorldId, input);
//                if(u2AWorldId.hasWorldid()){
//                    //get worldID from UPS, use this worldID to connect to world
//                    System.out.println("Have connected to UPS and get worldID from UPS");
//                    //send back ACK to UPS
//                    sendMsgTo(AMsg.newBuilder().addAcks(u2AWorldId.getSeqnum()).build(), output);
//                    if (amazonConnectWorld(u2AWorldId.getWorldid())){
//                        System.out.println("Amazon has connected to World and build 3 warehouses");
//                        break;
//                    }
//                }
                UMsg.Builder uMsg = UMsg.newBuilder();
                recvMsgFrom(uMsg, input);
                //int existence = 0;
                for (U2AWorldId u2AWorldId : uMsg.getWorldidList()) {
                    if (u2AWorldId.hasWorldid()) {
                        //existence = 1;
                        System.out.println("Have connected to UPS and get worldID:" + u2AWorldId.getWorldid());
                        sendMsgTo(AMsg.newBuilder().addAcks(u2AWorldId.getSeqnum()).build(), output);
                        if (amazonConnectWorld(u2AWorldId.getWorldid())) {
                            System.out.println("Amazon has connected to Worldid:" + u2AWorldId.getWorldid() + " and build 3 warehouses");
                            return;
                        }
                    }
                }
//                if(existence == 1){
//                    break;
//                }
            }

        }

    }



    //connect with world(warehouse) and create 3 warehouses
    public boolean amazonConnectWorld(long worldID) throws IOException {
        worldSocket = new Socket(WORLD_HOST, WORLD_PORTNUM);
        worldInput = worldSocket.getInputStream();
        worldOutput = worldSocket.getOutputStream();
        AConnect.Builder aconnectWorld = AConnect.newBuilder();
        aconnectWorld.setIsAmazon(true);
        aconnectWorld.setWorldid(worldID);
        for (int i = 1; i < 4; i ++){
            aconnectWorld.addInitwh(AInitWarehouse.newBuilder().setId(i).setX(i).setY(i));
        }

        AConnected.Builder aconnectedWorld = AConnected.newBuilder();

        sendMsgTo(aconnectWorld.build(), worldOutput);
        recvMsgFrom(aconnectedWorld, worldInput);

        System.out.println("world id: " + aconnectedWorld.getWorldid());
        System.out.println("result: " + aconnectedWorld.getResult());

        return aconnectedWorld.getResult().equals("connected!");

    }


    //set a thread to connect to frontend, and transfer it to a daemon
    void startFrontendConnection(){
        Thread frontendThread = new Thread(() -> {
            System.out.println("Has joined function(startFrontendConnection)!");
            while (!Thread.currentThread().isInterrupted()){
                try{
                    frontendServer = new Server(8888);//TODO:change this port for frontend
                    System.out.println("Start server for frontend at port 8888");
                    //Socket frontendSocket = frontendServer.accept();
                    while(!Thread.currentThread().isInterrupted()){

                        Socket frontendSocket = frontendServer.accept();
                        if(frontendSocket != null){
                            System.out.println("connect with front end");
                            handleBuyRequestFromFrontend(frontendSocket);
                        }
                    }
                }catch (Exception e){
                    System.err.println(e.toString());
                }
            }
        });
        frontendThread.setDaemon(true);
        frontendThread.start();
    }

    //this function will deal with the buy request from frontend
    private void handleBuyRequestFromFrontend(Socket frontendSocket) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(frontendSocket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String buyRequest = bufferedReader.readLine();
        System.out.println("buy request from frontend: "+ buyRequest);
        PrintWriter printWriter = new PrintWriter(frontendSocket.getOutputStream());
        long packageID = Long.parseLong(buyRequest);
        printWriter.write("ack:"+packageID);
        printWriter.flush();
        frontendSocket.close();
        purchaseMoreFromWorld(packageID);
    }

    private void sendUpdateStatusToFrontend(long packageID, String newStatus){
        try{
            Socket frontendsocket = new Socket("web", 8000);
            PrintWriter printWriter = new PrintWriter(frontendsocket.getOutputStream());
            String updateStatusMessage = packageID + ":" + newStatus;
            printWriter.write(updateStatusMessage);
            printWriter.flush();
            frontendsocket.close();
            System.out.println("Send to frontend new status:"+ newStatus);
        }catch(Exception e){
            System.err.println("Can not send update status message to frontend!");
        }
    }
    //this function will request warehouse to purchase more products in this package
    private void purchaseMoreFromWorld(long packageID){
        System.out.println("requesting warehouse(world) to buy more products in this packageID:" + packageID);
//        Thread handlepurchasemore = new Thread(() -> {
//            System.out.println("go into pruchase more thread pool");
//            APurchaseMore.Builder purchaseMoreReq = new PostgreSQL().purchaseMoreReq(packageID);
//            ACommands.Builder acommand = ACommands.newBuilder();
//            acommand.addBuy(purchaseMoreReq);
//            APack.Builder aPack = APack.newBuilder().setWhnum(purchaseMoreReq.getWhnum()).addAllThings(purchaseMoreReq.getThingsList()).setShipid(packageID).setSeqnum(SeqNumCounter.getInstance().getCurrent_seqnum());
//            aPackageManagement.addPackage(packageID, new Package(packageID, purchaseMoreReq.getWhnum(), aPack.build()));
//            aPackageManagement.changePackageStatus(packageID, APackageManagement.PACKING);
//            //sendUpdateStatusToFrontend(packageID, "packing");
//            frequentSendMessageToWorld(acommand, purchaseMoreReq.getSeqnum());
//        });
//        handlepurchasemore.start();
        threadPoolExecutor.execute(() -> {
            System.out.println("go into pruchase more thread pool");
            APurchaseMore.Builder purchaseMoreReq = new PostgreSQL().purchaseMoreReq(packageID);
            ACommands.Builder acommand = ACommands.newBuilder();
            acommand.addBuy(purchaseMoreReq);
            APack.Builder aPack = APack.newBuilder().setWhnum(purchaseMoreReq.getWhnum()).addAllThings(purchaseMoreReq.getThingsList()).setShipid(packageID).setSeqnum(SeqNumCounter.getInstance().getCurrent_seqnum());
            aPackageManagement.addPackage(packageID, new Package(packageID, purchaseMoreReq.getWhnum(), aPack.build()));
            aPackageManagement.changePackageStatus(packageID, APackageManagement.PACKING);
            //sendUpdateStatusToFrontend(packageID, "packing");
            System.out.println("Send message Apurchase more to world with seqnum:"+ acommand.getBuy(0).getSeqnum());
            frequentSendMessageToWorld(acommand, purchaseMoreReq.getSeqnum());
        });
//            //TODO:package this into a function(frequently send acommand message, when receive ack, stop)
////            acommand.setSimspeed(500);
////            Timer timer = new Timer();
////
////            TimerTask sendPurchaseMore = new TimerTask() {
////                @Override
////                public void run() {
////                    synchronized (worldOutput){
////                        sendMsgTo(acommand.build(), worldOutput);
////                    }
////                }
////            };
////            timer.schedule(sendPurchaseMore, 0, 4000);
//
//        });
    }

    private void frequentSendMessageToWorld(ACommands.Builder acommand, long seqnum){
        acommand.setSimspeed(500);
        Timer timer = new Timer();

        TimerTask sendPurchaseMore = new TimerTask() {
            @Override
            public void run() {
                synchronized (worldOutput){
                    //System.out.println("Send message Apurchase more to world with seqnum:"+ acommand.getBuy(0).getSeqnum());
                    sendMsgTo(acommand.build(), worldOutput);
                }
            }
        };
        timer.schedule(sendPurchaseMore, 0, 4000);

        //add this timer(thread) to frequentSendMessageThreads and when receive it's ack(= seqnum), cancel thread
        frequentSendMessageThreads.addNewFrequentThread(seqnum, timer);
    }

    private void frequentSendMessageToUPS(AMsg aMsg, int seqnum){
        try{
            InputStream inputStream = upsSocket.getInputStream();
            OutputStream outputStream = upsSocket.getOutputStream();
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    sendMsgTo(aMsg, outputStream);
                }
            };
            timer.schedule(timerTask, 0, 4000);
            upsFrequentSendMessageThreads.addNewFrequentThread(seqnum, timer);

        }catch(Exception e){
            System.err.println("frequentSendMessageToWorld: " + e.toString());
        }


    }


    void startUpsConnectionAfterGetWorldid(){
        Thread upsThread = new Thread(() -> {
            //TODO: we assume that UPS server socket will not be closed after transfer worldid, so we use upsSocket
            while (!Thread.currentThread().isInterrupted()){
                try {
                    //Socket upssocket = new Socket(CONNECT_UPS_HOST, CONNECT_UPS_PORTNUM);
                    UMsg.Builder uacommand = UMsg.newBuilder();
                    InputStream inputStream = upsSocket.getInputStream();
                    OutputStream outputStream = upsSocket.getOutputStream();
                    if (recvMsgFrom(uacommand, inputStream)) {
                        threadPoolExecutor.execute(() -> {
                            //System.out.println("start connect with ups after get worldid");
                            try {
//                                UMsg.Builder uMsg = UMsg.newBuilder();
//                                recvMsgFrom(uMsg, inputStream);
                                upsResponseHandler.handleUMsgFromUPS(uacommand.build(), outputStream);
                                allKindsOfResponseFromUPS(uacommand.build());
                                allKindsOfActionsToResponseFromUPS(uacommand.build());


                            }catch (Exception e){
                                System.err.println("handleUpsConnection:"+ e.toString());
                            }
                        });
                    } else {
                        //System.err.println("startUpsConnectionAfterGetWorldid: UPS server socket is null after transfer world Id");
                    }
                }catch (Exception e){

                }
            }
        });
        upsThread.start();
    }

    private void handleUpsConnection(Socket upssocket){
        //first receive message from UPS
        //second send back ACK
        //handle all kinds response from UPS
        try {
            InputStream inputStream = upssocket.getInputStream();
            OutputStream outputStream = upssocket.getOutputStream();
            UMsg.Builder uMsg = UMsg.newBuilder();
            recvMsgFrom(uMsg, inputStream);

            upsResponseHandler.handleUMsgFromUPS(uMsg.build(), outputStream);
            allKindsOfResponseFromUPS(uMsg.build());
            allKindsOfActionsToResponseFromUPS(uMsg.build());


        }catch (Exception e){
            System.err.println("handleUpsConnection:"+ e.toString());
        }
    }

    private void allKindsOfResponseFromUPS(UMsg responseFromUPS){

        //this shut down timer thread for  responses which is ack
        DealwithUPSACKs(responseFromUPS);

    }

    private void DealwithUPSACKs(UMsg responseFromUPS){
        //do not need to send ack
        //when we receive ack, we stop sending message of Timer
        for(int ack: responseFromUPS.getAcksList()){
            if(upsFrequentSendMessageThreads.findACK(ack)){
                System.out.println("receive ack:"+ack);
                upsFrequentSendMessageThreads.killThread(ack);
            }
        }
    }

    private void allKindsOfActionsToResponseFromUPS(UMsg responseFromUPS){
        //DealwithResponseAPurchaseMore(responseFromUPS);
        DealwithUMsgUTruckSent(responseFromUPS);
        DealwithUMsgUTruckArrived(responseFromUPS);
        DealwithUMsgUDeliverRsp(responseFromUPS);
    }

    private void DealwithUMsgUTruckSent(UMsg responseFromUPS){
        for(UTruckSent uTruckSent: responseFromUPS.getTrucksentList()){
            //for UTruckSent:
            //first bind the truckID with packageID
            synchronized (aPackageManagement){
                System.out.println("UTruckSent seqnum:"+ uTruckSent.getSeqnum());
                aPackageManagement.handleUMsgUTruckSent(uTruckSent);
            }
        }
    }

    private void DealwithUMsgUTruckArrived(UMsg responseFromUPS){
        for (UTruckArrived uTruckArrived: responseFromUPS.getTruckarrivedList()){
            //for UTruckArrived:
            //second change variable in package(and check APacked)
            //if APacked, send APutOnTruck(change status to loading)
            //send ALoading (which is newly updated in proto)
            synchronized (aPackageManagement){
                System.out.println("UTruckArrived seqnum:"+ uTruckArrived.getSeqnum());
                boolean whetherPutOnTruck = aPackageManagement.handleUMsgUTruckArrived(uTruckArrived);
                if(whetherPutOnTruck == true){
                    amazonAskWorldToPutOnTruck(uTruckArrived.getPackageid());
                    //amazonSendUPSALoading(uTruckArrived.getPackageid());
                }
            }
        }
    }

    private void DealwithUMsgUDeliverRsp(UMsg responseFromUPS){
        for (UDeliverRsp uDeliverRsp: responseFromUPS.getDeliveredList()){
            //for UDelivered:
            //first change status to delivered
            //TODO: remove it from aPackageManagement?
            synchronized (aPackageManagement){
                aPackageManagement.handleUMsgUDeliverRsp(uDeliverRsp);
                sendUpdateStatusToFrontend(uDeliverRsp.getPackageid(), "delivered");
                aPackageManagement.removePackage(uDeliverRsp.getPackageid());
            }

        }
    }


    //handle all kinds of response from the world(warehouse)
    void startWorldConnectionAfterInitWarehouse(){
        Thread worldThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()){
                //System.out.println("start connection with world after get worldid");
                AResponses.Builder worldResponse = AResponses.newBuilder();
                recvMsgFrom(worldResponse, worldInput);
                allKindsOfResponseFromWorld(worldResponse.build());
                //worldResponseHandler.handleAResponsesFromWorld(worldResponse.build(), worldOutput);
                allKindsOfActionsToResponseFromWorld(worldResponse.build());
            }
        });
        worldThread.start();
    }

    //


    private void allKindsOfResponseFromWorld(AResponses responseFromWorld){
        //when we receive a response(command) from world, we need to send back a ack
        //there exists many kinds of response in responseFromWorld
        //so we need to deal with every kind of response and send back ack
        //TODO: package all this functions into a class
//        sendAckToWorldAndDealwithAPurchaseMore(responseFromWorld);
//        sendAckToWorldAndDealwithAPacked(responseFromWorld);
//        sendAckToWorldAndDealwithALoaded(responseFromWorld);
//        sendAckToWorldAndDealwithFinished(responseFromWorld);
//        sendAckToWorldAndDealwithError(responseFromWorld);
//        sendAckToWorldAndDealwithAPackage(responseFromWorld);

        //this send back acks to all kind of responses except ack
        worldResponseHandler.handleAResponsesFromWorld(responseFromWorld, worldOutput);
        //this shut down timer thread for  responses which is ack
        DealwithWorldACKs(responseFromWorld);

    }

    private void DealwithWorldACKs(AResponses responseFromWorld){
        //do not need to send ack
        //when we receive ack, we stop sending message of Timer
        for(long ack: responseFromWorld.getAcksList()){
            if(frequentSendMessageThreads.findACK(ack)){
                System.out.println("received ack:"+ ack);
                frequentSendMessageThreads.killThread(ack);
            }
        }
    }
    private void allKindsOfActionsToResponseFromWorld(AResponses responseFromWorld){
        DealwithResponseAPurchaseMore(responseFromWorld);
        DealwithResponseAPacked(responseFromWorld);
        DealwithResponseALoaded(responseFromWorld);
        DealwithResponseAErr(responseFromWorld);
        DealwithResponseFinished(responseFromWorld);
    }

    private void DealwithResponseAErr(AResponses responseFromWorld){
        for (AErr aerr: responseFromWorld.getErrorList()){
            //System.err.println(aerr.getErr());
        }
    }

    private void DealwithResponseFinished(AResponses responseFromWorld){
        if(responseFromWorld.hasFinished() && responseFromWorld.getFinished() == true){
            System.out.println("amazon disconnect finish");
        }
    }


    private void DealwithResponseAPurchaseMore(AResponses responseFromWorld){
        for (APurchaseMore aPurchaseMore: responseFromWorld.getArrivedList()){
            synchronized (aPackageManagement){
                System.out.println("received purchase more and start handling it");
                long packageID = aPackageManagement.handleAResponseAPurchaseMore(aPurchaseMore);
                //once receive APurchaseMore response from world
                //we ask warehouse to pack
                //we request ups a truck
                if(packageID == 0){
                    continue;
                }
                amazonAskWorldToPack(packageID);
                amazonRequestUPSTruck(packageID);
            }
        }
    }

    private void amazonAskWorldToPack(long packageID){
        //get APack from aPackageManagement

        threadPoolExecutor.execute(() -> {
            ACommands.Builder acommand = ACommands.newBuilder();
            APack apack = aPackageManagement.findAPack(packageID);
            acommand.addTopack(apack);
            System.out.println("APack with seqnum:" + apack.getSeqnum());
            frequentSendMessageToWorld(acommand, apack.getSeqnum());
        });
    }

    private void amazonRequestUPSTruck(long packageID){
        //create a ATruckReq and send it to UPS
        threadPoolExecutor.execute(() -> {
            AMsg.Builder aMsg = AMsg.newBuilder();
            ATruckReq.Builder aTruckReq = ATruckReq.newBuilder();
            int whnum = aPackageManagement.findAPack(packageID).getWhnum();
            aTruckReq.setWh(Warehouse.newBuilder().setId(whnum).setX(whnum).setY(whnum));
            aTruckReq.setUpsaccount(new PostgreSQL().getUserName(packageID));
            aTruckReq.setPackageid(packageID);

            for(AProduct aProduct: aPackageManagement.findAPack(packageID).getThingsList()){
                Product.Builder oneProduct = Product.newBuilder();
                oneProduct.setId(aProduct.getId());
                oneProduct.setDescription(aProduct.getDescription());
                oneProduct.setCount(aProduct.getCount());
                aTruckReq.addThings(oneProduct);
            }
            aTruckReq.setSeqnum(SeqNumCounter.getInstance().getCurrent_seqnum());
            aMsg.addTruckreq(aTruckReq);
            System.out.println("ATruckReq with seqnum:"+aTruckReq.getSeqnum());
            frequentSendMessageToUPS(aMsg.build(), aTruckReq.getSeqnum());
        });
    }

    private void DealwithResponseAPacked(AResponses responseFromWorld){
        for(APacked aPacked: responseFromWorld.getReadyList()){
            //for APacked:
            //first change status to packed
            //second change variable in package(and check UTruckArrived)
            //if UTruckArrived, send APutOnTruck(change status to loading)
            //send ALoading (which is newly updated in proto)
            synchronized (aPackageManagement){
                boolean whetherPutOnTruck = aPackageManagement.handleAResponseAPacked(aPacked);
                sendUpdateStatusToFrontend(aPacked.getShipid(), "packed");
                if(whetherPutOnTruck == true){
                    amazonAskWorldToPutOnTruck(aPacked.getShipid());
                    //amazonSendUPSALoading(aPacked.getShipid());
                }
            }
        }
    }

    private void amazonAskWorldToPutOnTruck(long packageID){
        threadPoolExecutor.execute(() -> {
            //send APutOnTruck(change status to loading)
            aPackageManagement.changePackageStatus(packageID, APackageManagement.LOADING);
            sendUpdateStatusToFrontend(packageID, "loading");
            ACommands.Builder acommand = ACommands.newBuilder();
            APutOnTruck.Builder aPutOnTruck = APutOnTruck.newBuilder();
            aPutOnTruck.setWhnum(aPackageManagement.findPackage(packageID).getWhnum());
            aPutOnTruck.setTruckid(aPackageManagement.findPackage(packageID).getTruckID());
            aPutOnTruck.setShipid(packageID);
            aPutOnTruck.setSeqnum(SeqNumCounter.getInstance().getCurrent_seqnum());
            acommand.addLoad(aPutOnTruck);
            System.out.println("APutOnTruck with seqnum:"+aPutOnTruck.getSeqnum()+" packageID:"+aPutOnTruck.getShipid());
            frequentSendMessageToWorld(acommand, aPutOnTruck.getSeqnum());
        });
    }

//    private void amazonSendUPSALoading(long packageID){
//        threadPoolExecutor.execute(() -> {
//            AMsg.Builder aMsg = AMsg.newBuilder();
//            ALoading.Builder aLoading = ALoading.newBuilder();
//            aLoading.setTruckid(aPackageManagement.findPackage(packageID).getTruckID());
//            aLoading.setSeqnum(SeqNumCounter.getInstance().getCurrent_seqnum());
//            aMsg.addLoading(aLoading);
//            frequentSendMessageToUPS(aMsg.build(), aLoading.getSeqnum());
//        });
//    }

    private void DealwithResponseALoaded(AResponses responseFromWorld){
        //for ALoaded:
        //first change status to loaded
        //second send ADeliverReq to ups
        for (ALoaded aLoaded: responseFromWorld.getLoadedList()){
            synchronized (aPackageManagement){
                aPackageManagement.handleAResponseALoaded(aLoaded);
                sendUpdateStatusToFrontend(aLoaded.getShipid(), "loaded");
                amazonSendUPSADeliverReq(aLoaded.getShipid());
            }
        }
    }

    private void amazonSendUPSADeliverReq(long packageID){
        threadPoolExecutor.execute(() -> {
            //change status to delivering
            aPackageManagement.changePackageStatus(packageID, APackageManagement.DELIVERING);
            sendUpdateStatusToFrontend(packageID, "delivering");
            AMsg.Builder aMsg = AMsg.newBuilder();
            ADeliverReq.Builder aDeliverReq = ADeliverReq.newBuilder();
            aDeliverReq.setPackageid(packageID);
            aDeliverReq.setTruckid(aPackageManagement.findPackage(packageID).getTruckID());
            aDeliverReq.setDestX(aPackageManagement.findPackage(packageID).getDestX());
            aDeliverReq.setDestY(aPackageManagement.findPackage(packageID).getDestY());
            aDeliverReq.setSeqnum(SeqNumCounter.getInstance().getCurrent_seqnum());
            aMsg.addDeliverreq(aDeliverReq);
            System.out.println("ADeliverReq with packageID:"+ aDeliverReq.getPackageid()+" dest_x:"+ aDeliverReq.getDestX()+ " dest_y:"+ aDeliverReq.getDestY());
            frequentSendMessageToUPS(aMsg.build(), aDeliverReq.getSeqnum());
        });
    }

//    private void sendAckToWorldAndDealwithAPurchaseMore(AResponses responseFromWorld){
//        //send back acks
////        sendAckToWorldAPurchaseMore(responseFromWorld);
//        //deal with response APurchaseMore
//        //update database and go on next step Apack
//        DealwithResponseAPurchaseMore(responseFromWorld);
//    }


//    private void sendAckToWorldAndDealwithAPacked(AResponses responseFromWorld){
//        sendAckToWorldAPacked(responseFromWorld);
//    }
//
//    private void sendAckToWorldAndDealwithALoaded(AResponses responseFromWorld){
//        sendAckToWorldALoaded(responseFromWorld);
//    }
//
//    private void sendAckToWorldAndDealwithFinished(AResponses responseFromWorld){
//        //do not need to send ack
//    }
//
//    private void sendAckToWorldAndDealwithError(AResponses responseFromWorld){
//        sendAckToWorldAError(responseFromWorld);
//    }



//    private void sendAckToWorldAndDealwithAPackage(AResponses responseFromWorld){
//        sendAckToWorldAPackage(responseFromWorld);
//    }


//    private void sendAckToWorldAPurchaseMore(AResponses responseFromWorld){
//        ACommands.Builder acommands = ACommands.newBuilder();
//        boolean existence = false;
//        for(APurchaseMore aPurchaseMore: responseFromWorld.getArrivedList()){
//            acommands.addAcks(aPurchaseMore.getSeqnum());
//            existence = true;
//        }
//        if(existence) {
//            synchronized (worldOutput) {
//                sendMsgTo(acommands.build(), worldOutput);
//            }
//        }
//    }



//    private void sendAckToWorldAPacked(AResponses responseFromWorld){
//        ACommands.Builder acommands = ACommands.newBuilder();
//        boolean existence = false;
//        for(APacked aPacked: responseFromWorld.getReadyList()){
//            acommands.addAcks(aPacked.getSeqnum());
//            existence = true;
//        }
//        if(existence) {
//            synchronized (worldOutput) {
//                sendMsgTo(acommands.build(), worldOutput);
//            }
//        }
//    }


//    private void sendAckToWorldALoaded(AResponses responseFromWorld){
//        ACommands.Builder acommands = ACommands.newBuilder();
//        boolean existence = false;
//        for(ALoaded aLoaded: responseFromWorld.getLoadedList()){
//            acommands.addAcks(aLoaded.getSeqnum());
//            existence = true;
//        }
//        if(existence) {
//            synchronized (worldOutput) {
//                sendMsgTo(acommands.build(), worldOutput);
//            }
//        }
//    }


//    private void sendAckToWorldAError(AResponses responseFromWorld){
//        ACommands.Builder acommands = ACommands.newBuilder();
//        boolean existence = false;
//        for(AErr aErr: responseFromWorld.getErrorList()){
//            acommands.addAcks(aErr.getSeqnum());
//            existence = true;
//        }
//        if(existence) {
//            synchronized (worldOutput) {
//                sendMsgTo(acommands.build(), worldOutput);
//            }
//        }
//    }

//    private void sendAckToWorldAPackage(AResponses responseFromWorld){
//        ACommands.Builder acommands = ACommands.newBuilder();
//        boolean existence = false;
//        for(APackage aPackage: responseFromWorld.getPackagestatusList()){
//            acommands.addAcks(aPackage.getSeqnum());
//            existence = true;
//        }
//        if(existence) {
//            synchronized (worldOutput) {
//                sendMsgTo(acommands.build(), worldOutput);
//            }
//        }
//    }




















}
