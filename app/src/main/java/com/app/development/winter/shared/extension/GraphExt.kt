package com.app.development.winter.shared.extension

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.app.development.winter.R
import com.app.development.winter.ui.profile.model.ProfileEarnings
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.model.GradientColor


fun BarChart.setBarChartProperties(
    chartEarningData: MutableList<ProfileEarnings>?,
    isLoadedWithAnim: Boolean,
    axisTextSize: Float,
    rightAxisFormatter: ValueFormatter
) {
    this.apply {

        resetChart()

        addCommonPropertyOfBarGraph(axisTextSize)

        setXAxisValue(chartEarningData)
        setLeftAxisValue(rightAxisFormatter)
        setRightAxisValue()


        // add a nice and smooth animation
        if (isLoadedWithAnim) animateY(500)

        val entries = ArrayList<BarEntry>()
        chartEarningData?.forEachIndexed { index, item ->
            entries.add(BarEntry(index.toFloat(), item.earningInCredits))
        }

        val mBarDataSet: BarDataSet?

        if (data != null && data.dataSetCount > 0) {
            mBarDataSet = data.getDataSetByIndex(0) as BarDataSet
            mBarDataSet.values = entries
            data.notifyDataChanged()
        } else {
            mBarDataSet = BarDataSet(entries, "")
            mBarDataSet.setDrawValues(true)
            setFitBars(true)
        }

        mBarDataSet.gradientColors =
            listOf(GradientColor(context.getColor(R.color.colorGraphStart), context.getColor(R.color.colorGraphEnd)))

        data = BarData(mBarDataSet).apply {
            barWidth = 0.5f
            setDrawValues(false)
            isHighlightEnabled = false
        }
        invalidate()
    }
}

fun BarChart.resetChart() {
    if (isEmpty.not()) {
        clearValues()
    }
}

fun BarChart.setXAxisValue(weekData: MutableList<ProfileEarnings>?) {
    val axisFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = (value).toInt()
            if (weekData.isNullOrEmpty() || index > weekData.size) {
                return ""
            }
            return weekData[index].scale
        }
    }
    xAxis.valueFormatter = axisFormatter
    xAxis.setDrawAxisLine(false)
}

fun BarChart.setLeftAxisValue(axisFormatter: ValueFormatter) {
    axisLeft.valueFormatter = axisFormatter
}

fun BarChart.setRightAxisValue() {
    val axisFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return ""
        }
    }
    axisRight.valueFormatter = axisFormatter
}

fun BarChart.addCommonPropertyOfBarGraph(axisTextSize: Float) {
    apply {
        minOffset = 0f
        setExtraOffsets(0f, 15f, 0f, 15f)
        description = Description()    // Hide the description
        legend.isEnabled = false
        description.isEnabled = false
        setScaleEnabled(false)
        setTouchEnabled(false)
        setPinchZoom(false)
        isDragEnabled = false
        setFitBars(true)

        setXAxisProperties(axisTextSize)
        setYAxisProperties(this.context, axisLeft)
        setYAxisProperties(this.context, axisRight)

        setNoDataText(resources.getString(R.string.loading))
        setNoDataTextTypeface(Typeface.DEFAULT_BOLD)
        setNoDataTextColor(Color.WHITE)
    }
}

fun setYAxisProperties(context: Context, axis: YAxis) {
    axis.gridColor = ContextCompat.getColor(context, R.color.colorWhiteAlpha15)
    axis.axisLineColor = ContextCompat.getColor(context, R.color.colorWhiteAlpha15)
    axis.textColor = ContextCompat.getColor(context, R.color.colorWhite)
    axis.typeface = ResourcesCompat.getFont(context, R.font.montserrat_semi_bold)
    axis.textSize = 8f
    axis.axisMinimum = 0f
    axis.isEnabled = true
    axis.xOffset = 10f
    axis.setGridDashedLine(DashPathEffect(floatArrayOf(3f, 3f), 0f))
    axis.setDrawAxisLine(false)
    axis.setDrawGridLines(true)
    axis.setDrawLabels(true)
}

fun BarChart.setXAxisProperties(axisTextSize: Float) {
    xAxis.setDrawGridLines(true)
    xAxis.setDrawAxisLine(true)
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.isGranularityEnabled = true
    xAxis.granularity = 1F
    xAxis.yOffset = 10f
    xAxis.textSize = axisTextSize
    xAxis.gridColor = ContextCompat.getColor(this.context, android.R.color.transparent)
    xAxis.textColor = ContextCompat.getColor(this.context, R.color.colorWhite)
    xAxis.typeface = ResourcesCompat.getFont(context, R.font.montserrat_semi_bold)
}

