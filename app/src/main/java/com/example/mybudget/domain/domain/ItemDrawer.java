package com.example.mybudget.domain.domain;


import static com.example.mybudget.utils.Enums.Level;

/**
 * ItemDrawer class - holds all the data necessary for drawing an item/category in the recyclerView
 */
public class ItemDrawer {
    Item item;
    int background;
    Level level;
    boolean isExtended;

    public ItemDrawer(Item item, int background, boolean isExtended, Level level) {
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

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean isExtended() {
        return isExtended;
    }

    public void setIsExtended(boolean isExtended) {
        this.isExtended = isExtended;
    }
}
