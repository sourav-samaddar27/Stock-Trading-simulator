package com.example.stocks;

import javax.sound.sampled.Port;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class TradeService {
    private final UserDAO userDAO;
    private final StockDAO stockDAO;
    private final OrderDAO orderDAO;
    private final TradeDAO tradeDAO;
    private final PortfolioDAO portfolioDAO;

    public TradeService(UserDAO userDAO,StockDAO stockDAO,OrderDAO orderDAO,TradeDAO tradeDAO,PortfolioDAO portfolioDAO){
        this.userDAO=userDAO;
        this.stockDAO=stockDAO;
        this.orderDAO=orderDAO;
        this.tradeDAO=tradeDAO;
        this.portfolioDAO=portfolioDAO;

    }
    public Trade executetrade(Order buyerOrder, Order sellerOrder, BigDecimal executedPrice, int executedQuantity) throws SQLException,IllegalArgumentException{
        if(buyerOrder==null || sellerOrder==null){
            throw new IllegalArgumentException("Both buyer nas seller orders must be provided.");
        }
        if(executedPrice==null|| executedPrice.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Executed price must be positive");
        }
        if(executedQuantity<=0){
            throw new IllegalArgumentException("Executed quantity must be positive");
        }
        if(buyerOrder.getUserId()==sellerOrder.getUserId()){
            throw new IllegalArgumentException("Cannot execute trade between the same user.");
        }
        if(buyerOrder.getStockId()==sellerOrder.getStockId()){
            throw new IllegalArgumentException("Cannot execute trade for different stocks.");
        }
        if(executedQuantity>buyerOrder.getQuantity() || executedQuantity>sellerOrder.getQuantity()){
            throw new IllegalArgumentException("Executed quantity exceeds available order quantity");
        }
        Connection connection=null;
        try{
            connection=DatabaseManager.getConnection();
            connection.setAutoCommit(false);
            // 1. Update Buyer's Balance (Decrease)
            Optional<User> buyerOptional=userDAO.findById(buyerOrder.getUserId());
            if(!buyerOptional.isPresent()){
                throw new SQLException("buyer user not found for ID:"+buyerOrder.getUserId());
            }
            User buyer=buyerOptional.get();
            BigDecimal cost=executedPrice.multiply(new BigDecimal(executedQuantity));
            buyer.setBalance(buyer.getBalance().subtract(cost));
            userDAO.updateBalance(buyer);

            //2. update seller's Balance(increase)
            Optional<User> sellerOptional=userDAO.findById(sellerOrder.getUserId());
            if(!sellerOptional.isPresent()){
                throw new SQLException("Seller user not found for ID: "+sellerOrder.getUserId());
            }
            User seller=sellerOptional.get();
            BigDecimal revenue=executedPrice.multiply(new BigDecimal(executedQuantity));
            seller.setBalance(seller.getBalance().add(revenue));
            userDAO.updateBalance(seller);

            // 3. Update Buyer's Portfolio (Add Stock)
            // Get current portfolio item for buyer-stock.
            Optional<PortfolioItem> buyerPortfolioItemOptional=PortfolioDAO.findByUserIDAndStockId(buyer.getId(),buyerOrder.getStockId());
            PortfolioItem buyerPortfolioItem;
            if(buyerPortfolioItemOptional.isPresent()){
                buyerPortfolioItem=buyerPortfolioItemOptional.get();
                buyerPortfolioItem.setQuantity(buyerPortfolioItem.getQuantity()+executedQuantity);
            }else{
                buyerPortfolioItem=new PortfolioItem(buyer.getId(),buyerOrder.getStockId(),executedQuantity);
            }
            portfolioDAO.saveOrUpdate(buyerPortfolioItem);

            // 4. Update Seller's Portfolio (Remove Stock)
            // Seller MUST have this stock.

            Optional<PortfolioItem> sellerPortfolioItemOptional=portfolioDAO.findByUserIDAndStockId(seller.getId(),sellerOrder.getStockId());
            if(!sellerPortfolioItemOptional.isPresent()){
                throw new SQLException("Seller does not hold the stock being sold for ID: "+ seller.getId()+", Stock ID: "+sellerOrder.getStockId());

            }
            PortfolioItem sellerPortfolioItem=sellerPortfolioItemOptional.get();
            sellerPortfolioItem.setQuantity(sellerPortfolioItem.getQuantity()-executedQuantity);
            if(sellerPortfolioItem.getQuantity()==0){
                portfolioDAO.delete(sellerPortfolioItem.getUserId(),sellerPortfolioItem.getStockId());
            }else if(sellerPortfolioItem.getQuantity()<0){
                throw new SQLException("Seller's portfolio quantity became negative after trade.");
            }else{
                portfolioDAO.saveOrUpdate(sellerPortfolioItem);
            }
            // 5. Update Order Statuses and Quantities
            // Update buyer order.
            if(executedQuantity==buyerOrder.getQuantity()){
                buyerOrder.setStatus(Order.OrderStatus.EXECUTED);
            }else{
                buyerOrder.setStatus(Order.OrderStatus.PARTIAL_FILL);
                buyerOrder.setQuantity(buyerOrder.getQuantity()-executedQuantity);
            }
            orderDAO.updateStatusAndQuantity(buyerOrder);

            if(executedQuantity==sellerOrder.getQuantity()){
                sellerOrder.setStatus(Order.OrderStatus.EXECUTED);
            }else{
                sellerOrder.setStatus(Order.OrderStatus.PARTIAL_FILL);
                sellerOrder.setQuantity(sellerOrder.getQuantity()-executedQuantity);
            }
            orderDAO.updateStatusAndQuantity(sellerOrder);

            // 6. Record the Trade
            Trade newTrade=new Trade(buyer.getId(), seller.getId(),buyerOrder.getStockId(),executedPrice,executedQuantity);
            Trade savedTrade=tradeDAO.save(newTrade);
            connection.commit();
            System.out.println("Trade executed successfully: "+executedQuantity+" of Stock ID "+buyerOrder.getStockId()+" at "+executedPrice);
            return savedTrade;
        }catch (SQLException e){
            if(connection!=null){
                try{
                    connection.rollback();
                    System.err.println("Trade transaction rolled back due to error: "+e.getMessage());
                }catch (SQLException rollback){
                    System.err.println("Error during transaction rollback: "+rollback.getMessage());
                }
            }
            throw e;
        }finally {
            if(connection!=null){
                try{
                    connection.setAutoCommit(true);
                    connection.close();
                }catch (SQLException closeEx){
                    System.err.println("Error closing connection after trade: "+closeEx.getMessage());
                }
            }
        }
    }

    public List<Trade> findTradesByUserId(int userId) throws SQLException {
        return tradeDAO.findTradesByUserId(userId);
    }

}
