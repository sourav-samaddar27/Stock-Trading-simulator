package com.example.stocks;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StockService {

    private final StockDAO stockDAO;

    public StockService(StockDAO stockDAO){
        this.stockDAO=stockDAO;
    }

    public Stock addNewStock(String symbol, String companyName,BigDecimal initialPrice) throws SQLException{
        if(symbol==null || symbol.trim().isEmpty()){
            throw new IllegalArgumentException("Stock symbol cannot be empty.");
        }
        if(companyName==null || companyName.trim().isEmpty()){
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
        if(initialPrice==null || initialPrice.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Initial price must be positive.");
        }

        Optional<Stock> existingStock=stockDAO.findBySymbol(symbol);
        if(existingStock.isPresent()){
            throw new IllegalArgumentException("Stock symbol '" + symbol + "' already exists. Please choose another.");
        }

        Stock newStock=new Stock(symbol.toUpperCase(),companyName,initialPrice);
        Stock savedStock=stockDAO.save(newStock);
        System.out.println("Stock "+savedStock.getSymbol()+" added successfully with ID: "+savedStock.getId());
        return savedStock;
    }

    public Optional<Stock> getStockBySymbol(String symbol) throws SQLException{
        if(symbol==null || symbol.trim().isEmpty()){
            return Optional.empty();
        }
        return stockDAO.findBySymbol(symbol.toUpperCase());
    }

    public Optional<Stock> getStockById(int stockId) throws SQLException{
        if(stockId<=0){
            return Optional.empty();
        }
        return stockDAO.findById(stockId);
    }

    public List<Stock> getAllStocks() throws SQLException{
        return stockDAO.findAll();
    }

    public boolean updateStockPrice(int stockId, BigDecimal newPrice) throws SQLException{
        if(stockId<=0){
            throw new IllegalArgumentException("Invalid stock ID for price update");
        }
        if(newPrice==null || newPrice.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("New stock price must be positive.");
        }
        Optional<Stock> stockOptional=stockDAO.findById(stockId);
        if(stockOptional.isPresent()){
            Stock stockToUpdate=stockOptional.get();
            stockToUpdate.setCurrentPrice(newPrice);
            return stockDAO.updatePrice(stockToUpdate);
        }else{
            System.err.println("Stock with ID "+stockId+" not found for price update");
            return false;
        }
    }


}
