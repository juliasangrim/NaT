package com.lab.trubitsyna.snake.model;

import lombok.Getter;

import java.io.*;
import java.util.Properties;

public class CustomGameConfig {

    private final Properties config;

    @Getter
    private String login;
    @Getter
    private int width;
    @Getter
    private int height;
    @Getter
    private int foodStatic;
    @Getter
    private float foodPerPlayer;
    @Getter
    private int stateDelay;
    @Getter
    private int pingDelay;
    @Getter
    private float deadProbFood;
    @Getter
    private int nodeTimeout;

    public CustomGameConfig() {
        //for save changes of player settings
        this.config = getConfig();

        this.login = config.getProperty("user");
        this.width = Integer.parseInt(config.getProperty("width"));
        this.height = Integer.parseInt(config.getProperty("height"));
        this.foodStatic = Integer.parseInt(config.getProperty("food_static"));
        this.foodPerPlayer = Float.parseFloat(config.getProperty("food_per_player"));
        this.stateDelay = Integer.parseInt(config.getProperty("state_delay_ms"));
        this.deadProbFood = Float.parseFloat(config.getProperty("dead_prob_food"));
        this.pingDelay = Integer.parseInt(config.getProperty("ping_delay_ms"));
        this.nodeTimeout = Integer.parseInt(config.getProperty("node_timeout_ms"));
    }



    private Properties getConfig() {
        Properties prop = new Properties();
        //idk how make it better :(
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            prop.load(input);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }

    public void changeWidth(String newWidth) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.width = Integer.parseInt(newWidth);
            config.setProperty("width", newWidth);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void changeHeight(String newHeight) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.height = Integer.parseInt(newHeight);
            config.setProperty("height", newHeight);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void changeFoodStatic(String newFoodStatic) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.foodStatic = Integer.parseInt(newFoodStatic);
            config.setProperty("food_static", newFoodStatic);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void changeFoodPerPlayer(String newFoodPerPlayer) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.foodPerPlayer = Float.parseFloat(newFoodPerPlayer);
            config.setProperty("food_per_player", newFoodPerPlayer);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void changeStateDelayMessage(String newStateDelay) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.stateDelay = Integer.parseInt(newStateDelay);
            config.setProperty("state_delay_ms", newStateDelay);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void changeDeadProbFood(String newDeadProbFood) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.deadProbFood = Float.parseFloat(newDeadProbFood);
            config.setProperty("dead_prob_food", newDeadProbFood);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void changePingDelay(String newPingDelay) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.pingDelay = Integer.parseInt(newPingDelay);
            config.setProperty("ping_delay_ms", newPingDelay);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void changeNodeTimeout(String newNodeTimeout) {
        try (OutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
            this.nodeTimeout = Integer.parseInt(newNodeTimeout);
            config.setProperty("node_timeout_ms", newNodeTimeout);
            config.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
