package com.example.mybudget.domain.domain;


import static com.example.mybudget.utils.Enums.Level;

/**
 * ItemDrawer class - holds all the data necessary for drawing an item/category in the recyclerView
 */
public class ItemDrawer {
    Item item;
    boolean isSelected;
    Level level;
    boolean isExtended;

    public ItemDrawer(Item item, boolean isSelected, boolean isExtended, Level level) {
        this.item = item;
        this.isSelected = isSelected;
        this.isExtended = isExtended;
        this.level = level;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(int background) {
        this.isSelected = isSelected;
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
