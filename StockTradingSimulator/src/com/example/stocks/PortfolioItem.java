package com.example.stocks;

import java.util.Objects;
public class PortfolioItem {
    private int userId;
    private int stockId;
    private int quantity;

    public  PortfolioItem(int userId,int stockId, int quantity){
        this.userId=userId;
        this.stockId=stockId;
        this.quantity=quantity;
    }
    public int getUserId(){
        return userId;
    }
    public int getStockId(){
        return stockId;
    }
    public int getQuantity(){
        return quantity;
    }

    public void setQuantity(int quantity){
        if(quantity<0){
            throw new IllegalArgumentException("Protfolio quantity cannot be negative.");

        }
        this.quantity=quantity;
    }
    @Override
    public String toString(){
        return "ProtfolioItem{"+"userId="+userId+", stockId="+stockId+", quantity"+quantity+'}';
    }
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        PortfolioItem port=(PortfolioItem)o;
        return userId==port.userId && stockId==port.stockId;
    }
    @Override
    public int hashCode(){
        // Hash code is based on the composite primary key fields.
        return Objects.hash(userId,stockId);
    }


}
