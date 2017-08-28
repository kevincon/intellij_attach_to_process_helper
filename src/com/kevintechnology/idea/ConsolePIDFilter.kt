package com.kevintechnology.idea

import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.Filter.Result
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo
import com.intellij.execution.process.OSProcessUtil

open class ConsolePIDFilter : Filter {
    // TODO extract this to an option clients can provide
    var userPattern = "$0"

    open fun isValidPID(PID: Int): Boolean {
        val processList = OSProcessUtil.getProcessList()
        return processList.map { it.pid }.contains(PID)
    }

    override fun applyFilter(textLine: String, endPoint: Int): Filter.Result? {
        val startPoint = endPoint - textLine.length
        // TODO make this part of the constructor
        val regex = PIDRegexFactory.regexForPattern(userPattern)
        // TODO abstract the "extract PIDs from string" to another class
        val resultList = regex.findAll(textLine).flatMap {
            it.groups.drop(1).asSequence()
        }.filterNotNull().filter {
            isValidPID(it.value.toInt())
        }.map {
            Result(
                    startPoint + it.range.first,
                    startPoint + it.range.last + 1,
                    OpenUrlHyperlinkInfo(it.value))

        }.toList()
        return Result(resultList)
    }
}
