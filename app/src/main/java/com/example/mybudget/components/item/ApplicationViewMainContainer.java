package com.example.mybudget.components.item;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.mybudget.MainActivity;
import com.example.mybudget.databinding.ApplicationViewMainContainerBinding;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Enums;
import com.example.mybudget.utils.Utils;

import static com.example.mybudget.utils.Enums.DateFormat.PAY;

public class ApplicationViewMainContainer extends Fragment {
    private final MainActivity mainActivity;
    private ApplicationViewBuilder appViewBuilder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ApplicationViewMainContainerBinding bind = ApplicationViewMainContainerBinding.inflate(inflater, container, false);
        ConstraintLayout mainView = bind.getRoot();
        MonthlyStatistics statistics = DataHelper.getDataHelper(requireContext()).getMonthlyStatistics(Utils.getCurrentDate(PAY));
        appViewBuilder = new ApplicationViewBuilder(mainActivity, mainView, null,
                statistics == null? null: statistics.getStatistics().get(Utils.TOTAL), Utils.TOTAL, Utils.NO_PARENT_PREDICATE);
        return mainView;
    }

    public void refreshItems(Enums.Action action) {
        appViewBuilder.refreshItems(action, true);
    }

    public ApplicationViewMainContainer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
