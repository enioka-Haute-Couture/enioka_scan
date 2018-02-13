package com.enioka.scanner.activities;

/**
 * Items of AutoCompleteText in ManualInputFragment
 */
public class ManualInputItem {
    private String text;
    private boolean done;

    public ManualInputItem(String text, boolean done) {
        this.text = text;
        this.done = done;
    }

    public boolean isDone() {
        return this.done;
    }

    public String getText() {
        return this.text;
    }
}