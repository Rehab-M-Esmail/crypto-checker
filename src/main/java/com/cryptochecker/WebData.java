package com.cryptochecker;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.JOptionPane;
import com.google.gson.Gson; // fetching json data
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

public class WebData {
    public ArrayList<Coin> coin;
    public ArrayList<ArrayList<Coin>> portfolio;
    public ArrayList<String> portfolio_names = new ArrayList<String>();
    public int portfolio_nr = 0;
    public Global_Data global_data;

    public WebData() throws Exception {
        if (coin == null && global_data == null) {
            this.deserialize();
        }
    }

    String fetchJson(String urlString) throws IOException, InterruptedException {
        int attempts = 0;
        while (attempts < 3) { // retry up to 3 times
            URL url = new URL(urlString);
            javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 429) {
                System.err.println("⚠️  Rate limit hit (HTTP 429). Waiting before retry...");
                Thread.sleep(5000); // wait 5 seconds before retry
                attempts++;
                continue;
            }
            if (responseCode != 200) {
                throw new IOException("Unexpected response code: " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        }
        throw new IOException("Failed after 3 attempts due to rate limiting or network issues.");
    }
    public void fetch() throws Exception {
        try {
            // Helper function to fetch from URL with retry and headers


            // ----------------------------
            // 1️⃣ Fetch Coin Data
            // ----------------------------
            String coinJson = fetchJson("https://api.coingecko.com/api/v3/coins/markets?vs_currency="
                    + Main.currency.toLowerCase());
            coin = new Gson().fromJson(coinJson, new TypeToken<ArrayList<Coin>>(){}.getType());
            if (coin == null || coin.isEmpty()) {
                throw new IOException("Empty or invalid coin data returned from API.");
            }

            // ----------------------------
            // 2️⃣ Fetch Global Data
            // ----------------------------
            String globalJson = fetchJson("https://api.coingecko.com/api/v3/global");
            global_data = new Gson().fromJson(globalJson, Global_Data.class);
            if (global_data == null) {
                throw new IOException("Empty or invalid global data returned from API.");
            }

            Debug.log("✅ Successfully fetched and parsed data from CoinGecko.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(Main.frame,
                    "⚠️ Unable to connect or fetch from API.\nPlease check your internet or try again later.");
            if (coin == null) coin = new ArrayList<>();
            if (global_data == null) global_data = new Global_Data();
            Debug.log("ERROR: Failed to fetch data from API");
        }

        // ----------------------------
        // 3️⃣ Serialize fetched data
        // ----------------------------
        try (FileOutputStream file = new FileOutputStream(Main.dataSerLocation);
             BufferedOutputStream buffer = new BufferedOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(buffer)) {

            out.writeObject(global_data);
            out.writeObject(coin);
            Debug.log("💾 Serialized Data To " + Main.dataSerLocation);

        } catch (IOException i) {
            Debug.log("EXCEPTION: WebData.java - fetch()");
            i.printStackTrace();
        }
    }
    public static class RefreshCoins implements Runnable { // manuaully refresh with online API
        public RefreshCoins() {
            Thread t = new Thread(this, "Refresh Thread"); // create a separate thread that refreshes the coins
            t.start();
        }

        public void run() {
            try {
                long timerStart = System.nanoTime(); // TIMER START
                Main.gui.webData.fetch();
                long timerEnd = System.nanoTime(); // TIMER STOP
                Debug.log("TIMER - Main.gui.webData.fetch() took\n--seconds: "+(timerEnd - timerStart)/1000000000.0+"\n--milliseconds: "+((timerEnd-timerStart)/1000000.0)+"\n--nanoseconds: "+(timerEnd-timerStart));
    
                Main.gui.panelPortfolio.refreshPortfolio();
                Main.gui.panelPortfolio.serializePortfolio();
    
                Main.gui.panelCoin.reCreate();
                Main.gui.panelConverter.reCreate();
                Main.gui.panelPortfolio.reCreate();
            } catch(NoClassDefFoundError ex) {
                Debug.log("EXCEPTION: Missing Gson dependency!");
                JOptionPane.showMessageDialog(Main.frame, "Missing Gson dependency!\nDownload and use the default \"crypto-checker.jar\" instead of \"crypto-checker-no-dependencies.jar\"");
            } catch(Exception ex) {
                Debug.log("EXCEPTION: WebData.java - RefreshCoins().run()");
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked") // coin = (ArrayList<Coin> in.readObject()) supressing
    private void deserialize() throws Exception {
        if (!(new File(Main.dataSerLocation).canRead())) {
            Debug.log("ERROR: Couldn't find "+Main.dataSerLocation+".. fetching from API.");
            this.fetch();
            Debug.log("-- Fetched file");
            return;
        }

        try {
            FileInputStream file = new FileInputStream(Main.dataSerLocation);
            BufferedInputStream buffer = new BufferedInputStream(file);
            ObjectInputStream in = new ObjectInputStream(buffer);

            global_data = (Global_Data) in.readObject();
            coin = (ArrayList<Coin>) in.readObject();
            Debug.log("Deserialized Data From " + Main.dataSerLocation);
            in.close();
        } catch(Exception ex) {
            Debug.log("ERROR: WebData.deserialize(), deleting " + Main.dataSerLocation);
            File deleteFile = new File(Main.dataSerLocation);
            deleteFile.delete();

            this.fetch();
        }
    }

    public class Global_Data implements Serializable { // global data API
        private static final long serialVersionUID = 1L;

        @SerializedName(value="total_market_cap_usd", alternate={"total_market_cap_sek", "total_market_cap_eur", "total_market_cap_aud", "total_market_cap_brl", "total_market_cap_cad", "total_market_cap_chf", "total_market_cap_clp", "total_market_cap_cny", "total_market_cap_czk",
        "total_market_cap_dkk", "total_market_cap_gbp", "total_market_cap_hkd", "total_market_cap_huf", "total_market_cap_idr", "total_market_cap_ils", "total_market_cap_inr", "total_market_cap_jpy", "total_market_cap_krw", "total_market_cap_mxn", "total_market_cap_myr",
        "total_market_cap_nok", "total_market_cap_nzd", "total_market_cap_php", "total_market_cap_pkr", "total_market_cap_pln", "total_market_cap_rub", "total_market_cap_sgd", "total_market_cap_thb", "total_market_cap_try", "total_market_cap_twd", "total_market_cap_zar"})
        long total_market_cap;
        
        @SerializedName(value="total_24h_volume_usd", alternate={"total_24h_volume_sek", "total_24h_volume_eur", "total_24h_volume_aud", "total_24h_volume_brl", "total_24h_volume_cad", "total_24h_volume_chf", "total_24h_volume_clp", "total_24h_volume_cny", "total_24h_volume_czk",
        "total_24h_volume_dkk", "total_24h_volume_gbp", "total_24h_volume_hkd", "total_24h_volume_huf", "total_24h_volume_idr", "total_24h_volume_ils", "total_24h_volume_inr", "total_24h_volume_jpy", "total_24h_volume_krw", "total_24h_volume_mxn", "total_24h_volume_myr",
        "total_24h_volume_nok", "total_24h_volume_nzd", "total_24h_volume_php", "total_24h_volume_pkr", "total_24h_volume_pln", "total_24h_volume_rub", "total_24h_volume_sgd", "total_24h_volume_thb", "total_24h_volume_try", "total_24h_volume_twd", "total_24h_volume_zar"})
        long total_24h_volume;
        
        double bitcoin_percentage_of_market_cap;
        int active_currencies;
        int active_assets;
        int active_markets;
        long last_updated;

        public String toString() { // for global data button
            DecimalFormat df = new DecimalFormat("###,###");
            return "Total Market Cap: " + df.format(total_market_cap)
            + "\nTotal 24 Hour Volume: " + df.format(total_24h_volume)
            + "\nBitcoin Dominance: " + bitcoin_percentage_of_market_cap + "%"
            + "\nActive Currencies: " + active_currencies
            + "\nActive Assets: " + active_assets
            + "\nActive Markets: " + active_markets
            + "\nLast Updated: " + last_updated;
        }
    }

    public Coin getCoin() {
        return new Coin();
    }

    public class Coin implements Serializable, Cloneable {
        private static final long serialVersionUID = 1L;

        String id;
        String name;
        String symbol;

        @SerializedName("market_cap_rank")
        int rank;

        @SerializedName("current_price")
        double price;

        @SerializedName("market_cap")
        double market_cap;

        @SerializedName("total_volume")
        double _24h_volume;

        @SerializedName("circulating_supply")
        double available_supply;

        @SerializedName("total_supply")
        double total_supply;

        @SerializedName("max_supply")
        double max_supply;

        @SerializedName("price_change_percentage_1h_in_currency")
        double percent_change_1h;

        @SerializedName("price_change_percentage_24h")
        double percent_change_24h;

        @SerializedName("price_change_percentage_7d_in_currency")
        double percent_change_7d;

        String last_updated;

        // portfolio data
        double portfolio_amount;
        double portfolio_price;
        double portfolio_value;
        double portfolio_gains;
        String portfolio_currency;
        double portfolio_price_start;
        double portfolio_value_start;

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public Object copy() {
            try {
                return clone();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return new Object();
        }

        public String trimPrice(double trimPrice) {
            DecimalFormat df;
            if (trimPrice > 1) df = new DecimalFormat("#.##");
            else if (trimPrice > 0.1) df = new DecimalFormat("#.###");
            else if (trimPrice > 0.01) df = new DecimalFormat("#.####");
            else if (trimPrice > 0.001) df = new DecimalFormat("#.#####");
            else if (trimPrice > 0.0001) df = new DecimalFormat("#.######");
            else df = new DecimalFormat("#.############");
            return df.format(trimPrice);
        }

        public String getInfo() {
            DecimalFormat df = new DecimalFormat("###,###");
            return "Rank: " + rank
                    + "\nID: " + id
                    + "\nName: " + name
                    + "\nSymbol: " + symbol
                    + "\nPrice " + Main.currency + ": " + trimPrice(price)
                    + "\nMarket Cap: " + df.format(market_cap)
                    + "\n24 Hour Volume: " + df.format(_24h_volume)
                    + "\nAvailable Supply: " + df.format(available_supply)
                    + "\nTotal Supply: " + df.format(total_supply)
                    + "\nMax Supply: " + df.format(max_supply)
                    + "\nPercent 1 Hour: " + percent_change_1h + "%"
                    + "\nPercent 24 Hour: " + percent_change_24h + "%"
                    + "\nPercent 7 Days: " + percent_change_7d + "%"
                    + "\nLast Updated: " + last_updated;
        }

        public String getPortfolio() {
            DecimalFormat df = new DecimalFormat("###,###.##");
            return getInfo()
                    + "\n\nPortfolio Amount: " + trimPrice(portfolio_amount)
                    + "\nPortfolio Value: " + df.format(portfolio_value)
                    + "\nPortfolio Gains: " + df.format(portfolio_gains)
                    + "\n\nPortfolio Currency: " + portfolio_currency
                    + "\nPortfolio Price Start: " + df.format(portfolio_price_start)
                    + "\nPortfolio Value Start: " + df.format(portfolio_value_start);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
