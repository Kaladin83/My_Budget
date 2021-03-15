package com.example.mybudget.domain.domain;

/**
 * ItemDrawer class - holds all the data necessary for drawing an item/category in the recyclerView
 */
public class ItemDrawer {
    Item item;
    int background;
    int level;
    boolean isExtended;

    public ItemDrawer(Item item, int background, boolean isExtended, int level) {
        this.item = item;
        this.background = background;
        this.isExtended = isExtended;
        this.level = level;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isExtended() {
        return isExtended;
    }

    public void setIsExtended(boolean isExtended) {
        this.isExtended = isExtended;
    }
}
