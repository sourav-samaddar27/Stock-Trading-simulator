package com.example.stocks;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class OrderService {
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;
    private final StockDAO stockDAO;
    public OrderService(OrderDAO orderDAO,UserDAO userDAO,StockDAO stockDAO){
        this.orderDAO=orderDAO;
        this.userDAO=userDAO;
        this.stockDAO=stockDAO;
    }

    public Order placeBuyOrder(int userId, String stockSymbol,int quantity, BigDecimal desiredPrice) throws SQLException, IllegalArgumentException{

        if(quantity<=0){
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if(desiredPrice==null || desiredPrice.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Desired price must be positive.");
        }
        if(stockSymbol==null || stockSymbol.trim().isEmpty()){
            throw new IllegalArgumentException("Stock symbol cannot be empty");
        }

        Optional<User> userOptional=userDAO.findById(userId);
        if(!userOptional.isPresent()){
            throw new IllegalArgumentException("Stock symbol cannot be empty.");
        }
        User user=userOptional.get();

        Optional<Stock> stockOptional=stockDAO.findBySymbol(stockSymbol.toUpperCase());
        if(!stockOptional.isPresent()){
            throw new IllegalArgumentException("stock with symbol"+stockSymbol+" not found");
        }
        Stock stock=stockOptional.get();

        BigDecimal totalCost=desiredPrice.multiply(new BigDecimal(quantity));
        if(user.getBalance().compareTo(totalCost)<0){
            throw new IllegalArgumentException("Insufficient balance. Required "+totalCost+", Availabel: "+user.getBalance());
        }
        Order newOrder=new Order(user.getId(), stock.getId(),Order.OrderType.BUY, desiredPrice,quantity);

        Order saveOrder=orderDAO.save(newOrder);
        System.out.println("Buy order places: "+saveOrder.getQuantity()+" of "+stock.getSymbol()+" at "+saveOrder.getPrice()+" for User ID: "+saveOrder.getUserId());
        return saveOrder;
    }

    public Order placeSellOrder(int userId, String stockSymbol, int quantity, BigDecimal desiredPrice)throws SQLException, IllegalArgumentException{

        if(quantity<=0){
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if(desiredPrice==null || desiredPrice.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Desired price must be positive");
        }
        if(stockSymbol==null || stockSymbol.trim().isEmpty()){
            throw new IllegalArgumentException("Stock Symbol cannot be empty.");
        }
        Optional<User> userOptional=userDAO.findById(userId);
        if(!userOptional.isPresent()){
            throw new IllegalArgumentException("User with ID "+userId+" not found.");
        }
        User user=userOptional.get();
        Optional<Stock> stockOptional=stockDAO.findBySymbol(stockSymbol.toUpperCase());
        if(!stockOptional.isPresent()){
            throw new IllegalArgumentException("Stock with symbol " +stockSymbol+" not found.");
        }
        Stock stock=stockOptional.get();
        Order newOrder=new Order(user.getId(), stock.getId(), Order.OrderType.SELL,desiredPrice,quantity);
        Order savedOrder=orderDAO.save(newOrder);
        System.out.println("Sell order placed: " + savedOrder.getQuantity() + " of " + stock.getSymbol() + " at " + savedOrder.getPrice() + " for User ID: " + savedOrder.getUserId());
        return savedOrder;
    }

    public Optional<Order> getOrderById(int orderId) throws SQLException{
        if(orderId<=0){
            return Optional.empty();
        }
        return orderDAO.findById(orderId);
    }
    public List<Order> getPendingBuyOrdersByStockId(int stockId) throws SQLException{
        return orderDAO.findPendingBuyOrdersByStockId(stockId);
    }

    public List<Order> getPendingSellOrdersByStockId(int stockId) throws SQLException {
        return orderDAO.findPendingSellOrdersByStockId(stockId);
    }

    public List<Order> getOrdersByUserId(int userId) throws SQLException,IllegalArgumentException{
        Optional<User> userOptional=userDAO.findById(userId);
        if(!userOptional.isPresent()){
            throw new IllegalArgumentException("User with ID"+userId+" not found");
        }
        return orderDAO.findOrdersByUserId(userId);
    }
}
