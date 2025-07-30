package com.example.stocks;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Order {

    public enum OrderType{
        BUY,
        SELL
    }
    public enum OrderStatus{
        PENDING,
        EXECUTED,
        PARTIAL_FILL,
        CANCELLED
    }

    private int id; // Unique identifier for the order
    private int userId;
    private int stockId;
    private OrderType orderType;
    private BigDecimal price;
    private int quantity;
    private OrderStatus status;
    private LocalDateTime timestamp;


    public Order(int userId, int stockId, OrderType orderType, BigDecimal price, int quantity){
        this.userId=userId;
        this.stockId=stockId;
        this.orderType=orderType;
        this.price=price;
        this.quantity=quantity;
        this.status=OrderStatus.PENDING;
        this.timestamp=null;
    }
    public Order(int id,int userId, int stockId, OrderType orderType, BigDecimal price, int quantity, OrderStatus status, LocalDateTime timestamp ){
        this.id=id;
        this.userId=userId;
        this.stockId=stockId;
        this.orderType=orderType;
        this.price=price;
        this.quantity=quantity;
        this.status=status;
        this.timestamp=timestamp;
    }

    public int getId(){
        return id;
    }
    public int getUserId(){
        return userId;
    }
    public int getStockId(){
        return stockId;
    }
    public OrderType getOrderType(){
        return orderType;
    }
    public BigDecimal getPrice(){
        return price;
    }
    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setStatus(OrderStatus status){
        this.status=status;
    }

    public void setQuantity(int quantity){
        if(quantity<0){
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity=quantity;
    }
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", stockId=" + stockId +
                ", type=" + orderType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        Order order=(Order) o;
        return id==order.id;
    }
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

}
