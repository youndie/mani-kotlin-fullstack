package ru.workinprogress.feature.transaction.ui.model

import mani.composeapp.generated.resources.Res
import mani.composeapp.generated.resources.repeat_half_year
import mani.composeapp.generated.resources.repeat_month
import mani.composeapp.generated.resources.repeat_one_time
import mani.composeapp.generated.resources.repeat_quarter
import mani.composeapp.generated.resources.repeat_two_week
import mani.composeapp.generated.resources.repeat_week
import mani.composeapp.generated.resources.repeat_year
import ru.workinprogress.feature.transaction.Transaction.Period

internal val Period.stringResource
    get() = when (this) {
        Period.OneTime -> Res.string.repeat_one_time
        Period.Week -> Res.string.repeat_week
        Period.TwoWeek -> Res.string.repeat_two_week
        Period.Month -> Res.string.repeat_month
        Period.ThreeMonth -> Res.string.repeat_quarter
        Period.HalfYear -> Res.string.repeat_half_year
        Period.Year -> Res.string.repeat_year
    }
