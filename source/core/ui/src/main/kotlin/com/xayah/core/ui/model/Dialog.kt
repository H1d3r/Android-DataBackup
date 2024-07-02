package com.xayah.core.ui.model

data class DialogRadioItem<T>(
    val enum: T? = null,
    val title: StringResourceToken,
    val desc: StringResourceToken? = null,
)

data class DialogCheckBoxItem<T>(
    val enum: T? = null,
    val title: StringResourceToken,
    val desc: StringResourceToken? = null,
)
