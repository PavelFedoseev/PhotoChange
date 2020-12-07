package com.project.PhotoChange

import com.zomato.photofilters.SampleFilters
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.*

enum class Filters(var filterName: String, var photoFilter: Filter?) {
    NONE("None", null),
    BRIGHTNESS("Brightness", Filter().apply { addSubFilter(BrightnessSubFilter(50)) }),
    STARLIT("StarLit", SampleFilters.getStarLitFilter()),
    BLUEMESS("BlueMess", SampleFilters.getBlueMessFilter()),
    AWESTRUCK("AweStruck", SampleFilters.getAweStruckVibeFilter()),
    LIME("Lime", SampleFilters.getLimeStutterFilter()),
    NIGHT("Night", SampleFilters.getNightWhisperFilter()),
    DARK("Dark", Filter().apply { addSubFilter(BrightnessSubFilter(-100))}),
    MONO("Mono", Filter().apply { addSubFilter(SaturationSubFilter(-100.0f))}),
    MARS("Mars", Filter().apply {addSubFilter(ColorOverlaySubFilter(100, .30f, .16f, .30f))})
    ;
}