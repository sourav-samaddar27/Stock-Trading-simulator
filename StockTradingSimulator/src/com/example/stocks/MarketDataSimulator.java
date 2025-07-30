package com.example.stocks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MarketDataSimulator {
    private final StockService stockService;
    private final Timer timer;
    private final Random random;

    private static final long UPDATED_INTERVAL_MS=5*1000;
    private static final double MAX_PRICE_CHANGE_PERCENT=0.02;

    public MarketDataSimulator(StockService stockService){
        this.stockService=stockService;
        this.timer=new Timer(true);
        this.random=new Random();
    }

    public void startSimulation() {
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                updateStockPrices();

            }
        },0,UPDATED_INTERVAL_MS);
        System.out.println("Market Data Simulator started. Updating prices every"+(UPDATED_INTERVAL_MS/1000)+" seconds.");
    }

    public void stopSimulation(){
        timer.cancel();
        System.out.println("Market Data Simulator stopped");
    }

    private void updateStockPrices(){
        try{
            List<Stock> allStocks=stockService.getAllStocks();
            for(Stock stock:allStocks){
                BigDecimal oldPrice=stock.getCurrentPrice();
                BigDecimal changeAmount=oldPrice.multiply(new BigDecimal(random.nextDouble()*MAX_PRICE_CHANGE_PERCENT*(random.nextBoolean()?1:-1)));
                // random.nextDouble() generates a value between 0.0 (inclusive) and 1.0 (exclusive).
                // MAX_PRICE_CHANGE_PERCENT scales it.
                // random.nextBoolean() ? 1 : -1 randomly makes it positive (increase) or negative (decrease).

                BigDecimal newPrice=oldPrice.add(changeAmount).setScale(2,RoundingMode.HALF_UP);

                if(newPrice.compareTo(new BigDecimal("0.01"))<0){
                    newPrice=new BigDecimal("0.01");
                }
                stockService.updateStockPrice(stock.getId(),newPrice);
            }
        }catch (SQLException e){
            System.err.println("Error updating stock prices from simulator: "+e.getMessage());
        }catch(IllegalArgumentException e){
            System.err.println("Validating error during price update: "+e.getMessage());
        }
    }

}
