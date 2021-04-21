package com.example.mybudget.Data;

import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.dtos.ItemToAdd;
import com.example.mybudget.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Analyzer {

    public ItemToAdd getItemFromVoice(List<String> matches) {
        double amount = Arrays.stream(matches.get(0).split(" "))
                .filter(word -> word.matches("([0-9]*[.])?[0-9]*"))
                .mapToDouble(Double::parseDouble)
                .findFirst().orElse(0);
        if (amount == 0)
        {
            amount = Utils.firstFoundWordsToNumber(matches.get(0));
        }

        String newCategoryName = "";
        if (amount > 0)
        {
            List<String> categories = Utils.getCategoriesNames(getPredicate(false, matches));
            if (categories.isEmpty())
            {
                categories = Utils.getCategoriesNames(getPredicate(true, matches));
                if (!categories.isEmpty())
                {
                    String parentCategory = categories.get(0);
                    newCategoryName = Utils.getCategoriesNames(cat -> cat.getParent().equals(parentCategory)).isEmpty() ?
                            parentCategory : Utils.getDefaultCategoryName(parentCategory);
                    return new ItemToAdd(newCategoryName, parentCategory, parentCategory, amount, "");
                }
            }
            else
            {
                return new ItemToAdd(newCategoryName, categories.get(0), categories.get(0), amount, "");
            }
        }
        return null;
    }

    private Predicate<Category> getPredicate(boolean bool, List<String> matches){
        return cat -> (bool == cat.getParent().equals(""))
                && categoryFound(cat.getName().split(" "), matches.get(0).split(" "));
    }

    private boolean categoryFound(String[] category, String[] matches) {
        for (int i = 0; i < matches.length; i++)
        {
            if (matches[i].equalsIgnoreCase(category[0]))
            {
                for (int j = i; j < category.length; j++)
                {
                    if (matches.length == j || !matches[i].equalsIgnoreCase(category[j]))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * <h4>Preparing the data to add an item from form.<h4/>
     * <p>If given category has a parent category or has children categories, parentName (previous parent name) and defaultName
     * (new default
     * parent name) are populated accordingly. <p/>
     *
     * @param category given category
     * @param amount given amount
     * @param description given description
     * */
    public ItemToAdd getItemFromEdit(String category, double amount, String description) {
        String defaultName = "", parentName = "";

        Category parent = Utils.getParentCategory(category);
        if (parent != null || !Utils.getCategoriesNames(cat -> cat.getParent().equals(category)).isEmpty())
        {
            parentName = parent == null ? category : parent.getName();
            defaultName = parent == null || parent.getOtherName().equals("") ? Utils.getDefaultCategoryName(parentName) :
                parent.getOtherName();
        }
        return new ItemToAdd(defaultName, parentName, category, amount, description);
    }
}
