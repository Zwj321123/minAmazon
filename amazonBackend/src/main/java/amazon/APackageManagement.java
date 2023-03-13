package amazon;

import amazon.proto.AmazonUPSProtocol;
import amazon.proto.WorldAmazonProtocol;

import java.util.HashMap;

public class APackageManagement {
    public static final String PACKING = "packing";
    public static final String PACKED = "packed";
    public static final String LOADING = "loading";
    public static final String LOADED = "loaded";
    public static final String DELIVERING = "delivering";
    public static final String DELIVERED = "delivered";

    private HashMap<Long, Package> packages;

    public APackageManagement(){
        this.packages = new HashMap<>();
    }

    public void addPackage(long packageID, Package newPackage){
        packages.put(packageID, newPackage);
    }

    public void changePackageStatus(long packageID, String status){
        packages.get(packageID).setStatus(status);
        new PostgreSQL().updatePackageStatus(packageID, status);
    }

    public void changeFlagAPacked(long packageID){
        packages.get(packageID).setFlagAPacked(true);
    }

    public void changeFlagUTruckArrived(long packageID){
        packages.get(packageID).setFlagUTruckArrived(true);
    }

    public boolean checkPutOnTruck(long packageID){
        if (packages.get(packageID).isFlagAPacked() && packages.get(packageID).isFlagUTruckArrived()){
            return true;
        }
        return false;
    }

    //return the handled packageID
    public long handleAResponseAPurchaseMore(WorldAmazonProtocol.APurchaseMore responseFromWorld){
        for (Package pack: packages.values()){
            if(responseFromWorld.getWhnum() == pack.getWhnum() && responseFromWorld.getThingsList().equals(pack.getAPack().getThingsList())){
                System.out.println("handling purchaseMore with packageID:" + pack.getPackageID());
                changePackageStatus(pack.getPackageID(), PACKING);
                return pack.getPackageID();
            }
        }
        return 0;
    }

    public boolean handleAResponseAPacked(WorldAmazonProtocol.APacked responseFromWorld){
        for (Package pack: packages.values()){
            if (responseFromWorld.getShipid() == pack.getPackageID()){
                System.out.println("Received APacked with packageID:"+pack.getPackageID());
                changePackageStatus(pack.getPackageID(), PACKED);
                changeFlagAPacked(pack.getPackageID());
                return checkPutOnTruck(pack.getPackageID());
            }
        }
        return false;
    }

    public void handleUMsgUTruckSent(AmazonUPSProtocol.UTruckSent uTruckSent){
        for(Package pack: packages.values()){
            if (uTruckSent.getPackageid() == pack.getPackageID()){
                System.out.println("received UTruckSent with packageID:"+ uTruckSent.getPackageid()+ " truckID:"+uTruckSent.getTruckid());
                packages.get(pack.getPackageID()).setTruckID(uTruckSent.getTruckid());
                return;
            }
        }
    }

    public boolean handleUMsgUTruckArrived(AmazonUPSProtocol.UTruckArrived uTruckArrived){
        for (Package pack: packages.values()){
            if (uTruckArrived.getPackageid() == pack.getPackageID()){
                System.out.println("received UTruckArrived with packageID:"+ uTruckArrived.getPackageid()+ " truckID:"+ uTruckArrived.getTruckid());
                changeFlagUTruckArrived(pack.getPackageID());
                return checkPutOnTruck(pack.getPackageID());
            }
        }
        return false;
    }

    public void handleAResponseALoaded(WorldAmazonProtocol.ALoaded responseFromWorld){
        for (Package pack: packages.values()){
            if (responseFromWorld.getShipid() == pack.getPackageID()){
                System.out.println("Received ALoaded with packageid:" + pack.getPackageID());
                changePackageStatus(pack.getPackageID(), LOADED);
                return;
            }
        }
    }

    public void handleUMsgUDeliverRsp(AmazonUPSProtocol.UDeliverRsp uDeliverRsp){
        for (Package pack: packages.values()){
            if(uDeliverRsp.getPackageid() == pack.getPackageID()){
                System.out.println("received UDeliverRsp with packageID:"+ uDeliverRsp.getPackageid());
                changePackageStatus(pack.getPackageID(), DELIVERED);
                return;
            }
        }
    }

    public WorldAmazonProtocol.APack findAPack(long packageID){
        WorldAmazonProtocol.APack apack = packages.get(packageID).getAPack();
        return apack;
    }

    public Package findPackage(long packageID){
        Package myPackage = packages.get(packageID);
        return myPackage;
    }

    public void removePackage(long packageID){
        packages.remove(packageID);
    }

}
