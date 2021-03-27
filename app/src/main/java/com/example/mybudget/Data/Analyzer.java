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
                            parentCategory : parentCategory + " (other)";
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
     * Adding item from Edit form
     * Preparing data to handle 3 different cases:
     * 1. Adding item that has no subcategories
     * 2. Adding item to a subcategory when parent category already has items
     * 3. Adding item to a parent category when subcategory exists
     * */
    public ItemToAdd getItemFromEdit(String category, double amount, String description, List<Category> listOfCategories) {
        String otherName, parentName = category, addedCategoryName = category;
        if (!Utils.getCategoriesNames(cat -> cat.getParent().equals(category)).isEmpty())
        {
            otherName = category + " (other)";
            addedCategoryName = otherName;
        }
        else
        {
            otherName = listOfCategories.stream()
                    .filter(cat -> cat.getName().equals(category))
                    .filter(cat -> !cat.getOtherName().equals("") || Utils.getStatsForCategory(cat.getParent()) != null)
                    .map(cat -> !cat.getOtherName().equals("") ? cat.getOtherName() : cat.getParent() + " (other)")
                    .findFirst().orElse("");
            parentName = Utils.getParentCategoryName(category);
        }
        return new ItemToAdd(otherName.equals(" (other)") ? "": otherName, parentName, addedCategoryName, amount, description);
    }
}
