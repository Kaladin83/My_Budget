package com.example.mybudget.helpers;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.components.categorypicker.CategoryRecyclerAdapter;
import com.example.mybudget.components.colorpicker.ColorRecyclerAdapter;
import com.example.mybudget.components.item.ExpensesListAdapter;
import com.example.mybudget.components.item.ExpensesListDialog;
import com.example.mybudget.components.item.ItemsRecyclerTreeAdapter;

/**
 * Class that responsible of swiping the item off
 */
public class RecyclerTouchHelper extends ItemTouchHelper.SimpleCallback {
    private final RecyclerItemTouchHelperListener listener;

    public RecyclerTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View foregroundView;
        if (viewHolder instanceof ExpensesListAdapter.MyViewHolder1)
        {
            foregroundView = ((ExpensesListAdapter.MyViewHolder1) viewHolder).mainExpenseLayout;
        }
        else if (viewHolder instanceof ExpensesListAdapter.MyViewHolder2)
        {
            foregroundView = ((ExpensesListAdapter.MyViewHolder2) viewHolder).mainExpenseLayout;
        }
        else if (viewHolder instanceof ColorRecyclerAdapter.MyViewHolder)
        {
            foregroundView = ((ColorRecyclerAdapter.MyViewHolder)viewHolder).mainLayout;
        }
        else
        {
            foregroundView = ((CategoryRecyclerAdapter.MyViewHolder)viewHolder).mainLayout;
        }
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView;
        if (viewHolder instanceof ExpensesListAdapter.MyViewHolder1)
        {
            foregroundView = ((ExpensesListAdapter.MyViewHolder1) viewHolder).mainExpenseLayout;
        }
        else if (viewHolder instanceof ExpensesListAdapter.MyViewHolder2)
        {
            foregroundView = ((ExpensesListAdapter.MyViewHolder2) viewHolder).mainExpenseLayout;
        }
        else if (viewHolder instanceof  ColorRecyclerAdapter.MyViewHolder)
        {
            foregroundView = ((ColorRecyclerAdapter.MyViewHolder)viewHolder).mainLayout;
        }
        else
        {
            foregroundView = ((CategoryRecyclerAdapter.MyViewHolder)viewHolder).mainLayout;
        }

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
