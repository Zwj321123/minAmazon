package amazon;

import amazon.proto.WorldAmazonProtocol;

public class Package {
    private final String PACKING = "packing";
//    private final String PACKED = "packed";
//    private final String LOADING = "loading";
//    private final String LOADED = "loaded";
//    private final String DELIVERING = "delivering";
//    private final String DELIVERED = "delivered";


    private long packageID;
    private int whnum;
    private int destX;
    private int destY;
    private String status;
    private int truckID;
    private WorldAmazonProtocol.APack aPack;
    private String upsaccount;
    private WarehouseInPackage warehouseInPackage;
    private boolean flagAPacked;
    private boolean flagUTruckArrived;

    public Package(long packageID, int whnum, WorldAmazonProtocol.APack aPack){
        this.packageID = packageID;
        this.whnum = whnum;
        this.status = PACKING;
        this.aPack = aPack;
        this.warehouseInPackage = new WarehouseInPackage(whnum);
        this.flagAPacked = false;
        this.flagUTruckArrived = false;
        this.truckID = -1;
        Position position = new PostgreSQL().getPosition(packageID);
        this.destX = position.getX();
        this.destY = position.getY();
    }

    public boolean isFlagAPacked() {
        return flagAPacked;
    }

    public boolean isFlagUTruckArrived() {
        return flagUTruckArrived;
    }

    public void setFlagAPacked(boolean flagAPacked) {
        this.flagAPacked = flagAPacked;
    }

    public void setTruckID(int truckID) {
        this.truckID = truckID;
    }


    public void setFlagUTruckArrived(boolean flagUTruckArrived) {
        this.flagUTruckArrived = flagUTruckArrived;
    }

    public int getTruckID() {
        return truckID;
    }

    public WorldAmazonProtocol.APack getAPack(){
        return aPack;
    }

    public WorldAmazonProtocol.APack getaPack() {
        return aPack;
    }

    public long getPackageID(){
        return packageID;
    }

    public int getWhnum() {
        return whnum;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDestX() {
        return destX;
    }

    public int getDestY() {
        return destY;
    }
}
