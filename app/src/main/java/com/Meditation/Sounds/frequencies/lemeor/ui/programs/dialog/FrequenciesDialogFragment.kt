package com.Meditation.Sounds.frequencies.lemeor.ui.programs.dialog

import android.widget.Button
import androidx.lifecycle.MutableLiveData
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn0
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn000
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn1
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn2
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn3
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn4
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn5
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn6
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn7
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn8
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btn9
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btnAdd
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btnDelete
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.btnDot
import kotlinx.android.synthetic.main.custom_bottom_sheet_frequencies.edtHz

class FrequenciesDialogFragment(private val listener: (Double, FrequenciesDialogFragment) -> Unit) :
    BaseBottomSheetDialogFragment() {
    override val layoutId = R.layout.custom_bottom_sheet_frequencies
    private val liveTv = MutableLiveData<StringBuilder>()
    private val tv = StringBuilder()
    override fun initView() {
        liveTv.observe(viewLifecycleOwner) {
            val s = it.toString().formatDouble()
            edtHz.setText(s)
        }
        setupKeyboard()
    }

    private fun setupKeyboard() {
        btn0.setClickNumber("0")
        btn1.setClickNumber("1")
        btn2.setClickNumber("2")
        btn3.setClickNumber("3")
        btn4.setClickNumber("4")
        btn5.setClickNumber("5")
        btn6.setClickNumber("6")
        btn7.setClickNumber("7")
        btn8.setClickNumber("8")
        btn9.setClickNumber("9")
        btn000.setClickNumber("000")

        btnDot.setOnClickListener {
            if (tv.indexOf(".") < 0 && tv.length < 12) {
                if (tv.toString().formatNumber() == "0") {
                    liveTv.postValue(
                        tv.append("0.")
                    )
                } else {
                    liveTv.postValue(
                        tv.append(".")
                    )
                }
            }
        }

        btnAdd.setOnClickListener {
            try {
                if (tv.isEmpty()) {
                    showToast(getString(R.string.tv_error_hz, "1", "22000"))
                } else {
                    val num = tv.toString().toDouble()
                    if (num in 1.0..22000.0) {
                        listener.invoke(num, this)
                    } else {
                        showToast(getString(R.string.tv_error_hz, "1", "22000"))
                    }
                }
            } catch (_: NumberFormatException) {
                showToast(getString(R.string.tv_error_hz, "1", "22000"))
            }
        }

        btnDelete.setOnClickListener {
            if (tv.isNotEmpty()) {
                if (tv.toString().toDouble() < 0) {
                    tv.clear()
                    liveTv.postValue(
                        tv.append(0)
                    )
                } else {
                    liveTv.postValue(
                        tv.deleteCharAt(tv.length - 1)
                    )
                    if (tv.isEmpty()) {
                        tv.clear()
                        liveTv.postValue(
                            tv.append(0)
                        )
                    }
                }
            }
        }
    }

    private fun String.formatNumber(): String {
        return if (this.isNotEmpty()) {
            val number = this.toDouble()
            if (tv.indexOf(".") >= 0) {
                "%,f".format(number)
            } else {
                "%,.0f".format(number)
            }

        } else {
            "0"
        }
    }

    private fun String.formatDouble(): String {
        return if (this.isNotEmpty()) {
            if (tv.indexOf(".") >= 0) {
                val pr = tv.split(".")
                val s = "%,.0f".format(pr.first().toDouble())
                "$s.${pr.last()}"
            } else {
                val number = this.toDouble()
                "%,.0f".format(number)
            }
        } else {
            ""
        }
    }

    private fun Button.setClickNumber(number: String) {
        setOnClickListener {
            if (tv.length < 12) {
                if (number == "0" || number == "000") {
                    if (tv.toString().formatNumber() != "0") {
                        liveTv.postValue(
                            tv.append(number)
                        )
                    }
                } else {
                    liveTv.postValue(
                        tv.append(number)
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(listener: (Double, FrequenciesDialogFragment) -> Unit): FrequenciesDialogFragment {
            return FrequenciesDialogFragment(listener)
        }
    }
}