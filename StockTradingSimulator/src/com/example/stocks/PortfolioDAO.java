package com.example.stocks;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PortfolioDAO {

    public PortfolioItem saveOrUpdate(PortfolioItem portfolioItem) throws SQLException{
        Optional<PortfolioItem> existingItem=findByUserIDAndStockId(portfolioItem.getUserId(), portfolioItem.getStockId());

        if(existingItem.isPresent()){
            String sql="UPDATE Portfolios SET quantity=? WHERE user_id=? AND stock_id=?";
            try(Connection connection=DatabaseManager.getConnection();
            PreparedStatement stmt=connection.prepareStatement(sql)){
                stmt.setInt(1,portfolioItem.getQuantity());
                stmt.setInt(2,portfolioItem.getUserId());
                stmt.setInt(3,portfolioItem.getStockId());
                stmt.executeUpdate();
                System.out.println("Portfolio item updated for User ID: "+portfolioItem.getUserId());
                return portfolioItem;
            }
        }else{
            String sql="INSERT INTO Portfolios (user_id, stock_id, quantity VALUES(?,?,?)";
            try(Connection connection=DatabaseManager.getConnection();
            PreparedStatement stmt=connection.prepareStatement(sql)){
                stmt.setInt(1,portfolioItem.getUserId());
                stmt.setInt(2,portfolioItem.getStockId());
                stmt.setInt(3,portfolioItem.getQuantity());

                int affectedRows=stmt.executeUpdate();
                if(affectedRows==0){
                    throw new SQLException("Creating portfolio item failed, no rows affected.");

                }
                System.out.println("Portfolio item created for User ID: "+portfolioItem.getUserId()+", Stock ID "+portfolioItem.getStockId());
                return portfolioItem;
            }
        }

    }
    public static Optional<PortfolioItem> findByUserIDAndStockId(int userId, int stockId) throws SQLException{
        String sql="SELECT user_id, stock_id,quantity FROM Portfolios WHERE user_id=? AND stock_is=?";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,userId);
            stmt.setInt(2,stockId);

            try(ResultSet rs= stmt.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSetToPortfolioItem(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<PortfolioItem> findByUserId(int userId) throws SQLException{
        List<PortfolioItem> portfolioItems=new ArrayList<>();
        String sql="SELECT user_id, stock_id,quantity FROM Portfolios WHERE user_id=?";
        try(Connection connection= DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,userId);
            try(ResultSet rs=stmt.executeQuery()){
                while(rs.next()){
                    portfolioItems.add(mapResultSetToPortfolioItem(rs));
                }
            }
        }
        return portfolioItems;
    }

    public boolean delete(int userId, int stockId) throws SQLException{
        String sql="DELETE FROM Portfolios WHERE user_is=? AND stock_is=?";
        try(Connection connection=DatabaseManager.getConnection();
        PreparedStatement stmt=connection.prepareStatement(sql)){
            stmt.setInt(1,userId);
            stmt.setInt(2,stockId);

            int affectedRows=stmt.executeUpdate();
            return affectedRows>0;
        }
    }
    private static PortfolioItem mapResultSetToPortfolioItem(ResultSet rs) throws SQLException{
        int userId=rs.getInt("user_id");
        int stockId=rs.getInt("stock_id");
        int quantity=rs.getInt("quantity");
        return new PortfolioItem(userId, stockId, quantity);
    }

}
