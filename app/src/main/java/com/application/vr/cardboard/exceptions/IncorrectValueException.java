package com.application.vr.cardboard.exceptions;

public class IncorrectValueException extends Exception {
    private int value;

    public IncorrectValueException(int value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return this.getClass().getName() + "[ Incorrect value ["+ value +"]. Range of available values from 0 to 20. ]";
    }
}
