package amazon;

import amazon.proto.WorldAmazonProtocol.*;

import java.sql.*;


public class PostgreSQL {
    // database configuration
    private static final String dbUrl = "jdbc:postgresql://db:5432/amazon";//TODO: 修改db amazon(数据库名)
    private static final String dbUser = "postgres";
    private static final String dbPassword = "postgres";

    // table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_PRODUCT = "product";
    private static final String TABLE_USERPRODUCT = "userproduct";
    private static final String TABLE_PACKAGE = "package";
    private static final String TABLE_WAREHOUSE = "warehouse";
    private static final String TABLE_CATEGORY = "category";


    public PostgreSQL(){

    }

    //TODO: need package all the connection with database
    public APurchaseMore.Builder purchaseMoreReq(long packageID){
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("System database is valid?:" + conn.isValid(0));
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();
            //query the new package from frontend
            String checkWarehouseEnoughOrNotSQL = "SELECT count, title, amazon_userproduct.\"productID_id\", amazon_product.whnum_id "+
                                                  "FROM amazon_userProduct, amazon_package, amazon_product "+
                                                  "WHERE amazon_userproduct.\"productID_id\"=amazon_product.id AND amazon_userproduct.package_id=amazon_package.id AND amazon_package.id="+packageID+";";
            ResultSet result = statement.executeQuery(checkWarehouseEnoughOrNotSQL);

            //purchase more products in this package in corresponding warehouse
            //create APurchaseMore message sent to warehouse
            APurchaseMore.Builder apurchasemore = APurchaseMore.newBuilder();

            int whNum = 0;
            while(result.next()){
                whNum = result.getInt("whnum_id");
                System.out.println("whnum:"+ whNum);
                long AProductID = result.getLong("productID_id");//TODO: 交易中id为int64
                String description = result.getString("title");
                System.out.println("description:"+ description);
                int count = result.getInt("count");
                apurchasemore.addThings(AProduct.newBuilder().setId(AProductID).setDescription(description).setCount(count));
            }
            apurchasemore.setWhnum(whNum);
            apurchasemore.setSeqnum(SeqNumCounter.getInstance().getCurrent_seqnum());

            if(whNum == 0){
                throw new IllegalArgumentException("No package's packageID="+packageID+" is found in database!");
            }

            statement.close();
            conn.close();
            return apurchasemore;

        }catch (Exception e){
            System.out.println("errrrrrrrrrr happen!");
            System.err.println(e.toString());
        }
        return null;
    }


    public boolean updatePackageStatus(long packageID, String newStatus){
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();
            String updatePackageStatusSQL = "UPDATE amazon_package " +
                                            "SET status='"+ newStatus+"' "+
                                            "WHERE id="+packageID+";";
            statement.executeUpdate(updatePackageStatusSQL);
            conn.commit();
            statement.close();
            conn.close();
            return true;
        }catch (Exception e){
            System.err.println(e.toString());
        }
        return false;
    }

    public Position getPosition(long packageID){
        Position position = null;
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();

            String getDestinationSQL = "SELECT amazon_package.dest_x, amazon_package.dest_y "+
                                       "FROM amazon_package "+
                                       "WHERE id="+packageID+";";
            ResultSet result = statement.executeQuery(getDestinationSQL);
            while(result.next()){
                position = new Position(result.getInt("dest_x"), result.getInt("dest_y"));
                break;
            }


            statement.close();
            conn.close();
        }catch(Exception e){
            System.err.println(e.toString());
        }
        return position;
    }

    public String getUserName(long packageID){
        String username = null;
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();

            String getUsernameSQL = "SELECT username "+
                                    "FROM amazon_package, auth_user "+
                                    "WHERE amazon_package.id="+packageID+" AND amazon_package.owner_id=auth_user.id"+";";
            ResultSet result = statement.executeQuery(getUsernameSQL);

            while(result.next()){
                username = result.getString("username");
                break;
            }

            statement.close();
            conn.close();
        }catch (Exception e){
            System.err.println(e.toString());
        }
        return username;
    }
















}
