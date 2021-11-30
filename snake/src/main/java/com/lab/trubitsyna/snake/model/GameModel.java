package com.lab.trubitsyna.snake.model;

import com.lab.trubitsyna.snake.gameException.GameException;

import java.util.concurrent.CopyOnWriteArrayList;

public class GameModel implements IModel {
    private final CopyOnWriteArrayList<IListener> listeners = new CopyOnWriteArrayList<>();
    @Override
    public void updateModel() {

    }

    //notify listeners about changes
    protected void notifyListeners() throws GameException {
        for (IListener listener : listeners) {
            notifyListener(listener);
        }
    }

    private void notifyListener(IListener listener) throws GameException {
        if (listener == null) {
            throw new GameException("No listeners for our model...");
        }
        listener.modelChanged(this);
    }

    //method for following our model changes
    public void listen(IListener listener) throws GameException {
        if (listener == null) {
            throw new NullPointerException("Empty param...");
        }
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Repeat listeners...");
        }
        listeners.add(listener);
        notifyListener(listener);
    }

    //method for stop following our model changes
    public void noListen(IListener listener) {
        if (listener == null) {
            throw new NullPointerException("Empty param...");
        }
        if (!listeners.contains(listener)) {
            throw new IllegalArgumentException("Model don't have this listener...");
        }
    }

}
