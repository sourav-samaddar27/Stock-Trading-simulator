package com.example.stocks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class TradeDAO {


    public Trade save(Trade trade) throws SQLException{
        String sql="INSERT INTO Transactions (buyer_user_id, seller_user_id,stock_id, price,quantity) VALUES(?,?,?,?,?)";

        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){

            stmt.setInt(1,trade.getBuyerUserId());
            stmt.setInt(2,trade.getSellerUserId());
            stmt.setInt(3,trade.getStockId());
            stmt.setBigDecimal(4,trade.getPrice());
            stmt.setInt(5,trade.getQuantity());

            int affectedRows=stmt.executeUpdate();
            if(affectedRows==0){
                throw new SQLException("Creating trade failed, no rows affected");
            }
            try(ResultSet generatedKeys=stmt.getGeneratedKeys()){
                if(generatedKeys.next()){
                    int id=generatedKeys.getInt(1);
                    Optional<Trade> fetchedTrade=findById(id);
                    if(fetchedTrade.isPresent()){
                        return fetchedTrade.get();
                    }else{
                        throw new SQLException("Creating trade failed, could not retrieve full object after insert");
                    }
                }else{
                    throw new SQLException("Creating trade failed, no ID obtained.");
                }
            }
        }
    }

    public Optional<Trade> findById(int id) throws SQLException{
        String sql="SELECT id, buyer_user_id, seller_user_id,stock_id,price,quantity,timestamp FROM Transactions WHERE id=?";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,id);
            try(ResultSet rs=stmt.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSetToTrade(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Trade> findTradesByUserId(int userId) throws SQLException{
        List<Trade> trades= new ArrayList<>();
        String sql="SELECT id, buyer_user_id,seller_user_id,stock_id,price,quantity,timestamp FROM Transactions "+
                "WHERE buyer_user_id=? OR seller_user_id=? ORDER BY timestamp DESC";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,userId);
            stmt.setInt(2,userId);

            try(ResultSet rs=stmt.executeQuery()){
                while(rs.next()){
                    trades.add(mapResultSetToTrade(rs));
                }
            }

        }
        return trades;

    }
    public List<Trade> findTradeByStockId(int stockId) throws SQLException{
        List<Trade> trades=new ArrayList<>();
        String sql="SELECT id, buyer_user_id, seller_user_id, stock_id,price,quantity,timestamp FROM Transactions"+
                "WHERE stock_id=? ORDER BY timestamp DESC";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,stockId);

            try(ResultSet rs=stmt.executeQuery()){
                while(rs.next()){
                    trades.add(mapResultSetToTrade(rs));
                }
            }

        }
        return trades;
    }

    private Trade mapResultSetToTrade(ResultSet rs) throws SQLException{
        int id=rs.getInt("id");
        int buyerUserId=rs.getInt("buyer_user_id");
        int sellerUserId=rs.getInt("seller_user_id");
        int stockId=rs.getInt("stock_id");
        BigDecimal price=rs.getBigDecimal("price");
        int quantity=rs.getInt("quantity");
        Timestamp timestampSql=rs.getTimestamp("timestamp");
        LocalDateTime timestamp=timestampSql!=null? timestampSql.toLocalDateTime():null;
        return new Trade(id,buyerUserId,sellerUserId,stockId,price,quantity,timestamp);
    }


}
