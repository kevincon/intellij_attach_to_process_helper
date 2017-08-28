package com.kevintechnology.idea

import org.junit.Test
import org.junit.Before
import org.junit.Assert.*
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo
import com.intellij.execution.filters.Filter.ResultItem

class ConsolePIDFilterTest {
    class TestConsolePIDFilter : ConsolePIDFilter() {
        var fakeProcessList: IntArray? = null
        override fun isValidPID(PID: Int): Boolean = fakeProcessList?.contains(PID) ?: false
    }

    private val filter = TestConsolePIDFilter()

    @Before fun setUp() {
        filter.fakeProcessList = null
        filter.userPattern = "$0"
    }

    @Test fun emptyConsole() {
        assertNoPIDsMatched("")
    }

    @Test fun plainText() {
        assertNoPIDsMatched("Hello world")
    }

    @Test fun ignoreNegativeNumber() {
        filter.fakeProcessList = intArrayOf(12345)
        assertNoPIDsMatched("-12345")
    }

    @Test fun ignorePercentages() {
        filter.fakeProcessList = intArrayOf(75)
        assertNoPIDsMatched("75%")
    }

    @Test fun ignoreOutOfRangePID() {
        filter.fakeProcessList = intArrayOf(99999)
        assertNoPIDsMatched("999999")
    }

    @Test fun ignoreUnknownPID() {
        assertNoPIDsMatched("123")
    }

    @Test fun ignoreUserPatternMismatchButOtherwiseValidPID() {
        filter.fakeProcessList = intArrayOf(123)
        filter.userPattern = "Started test has PID $0"
        assertNoPIDsMatched("PID 123")
    }

    @Test fun missingPlaceholderInUserPatternDoesNotCrash() {
        filter.userPattern = "Missing placeholder/format characters"
        assertNoPIDsMatched("PID 123")
    }

    @Test fun validPID() {
        filter.fakeProcessList = intArrayOf(123)
        assertPID("PID 123", 4, "123")
    }

    @Test fun multipleSamePIDs() {
        filter.fakeProcessList = intArrayOf(123)
        filter.userPattern = "$0 PID $0"
        assertPIDs("123 PID 123", arrayOf(Pair(0, "123"), Pair(8, "123")))
    }

    @Test fun multipleDifferentPIDs() {
        filter.fakeProcessList = intArrayOf(123, 456)
        assertPIDs("123 PID 456 789", arrayOf(Pair(0, "123"), Pair(8, "456")))
    }

    private fun assertNoPIDsMatched(textLine: String) {
        val result = filter.applyFilter(textLine, textLine.length)
        val results = result?.resultItems

        assertEquals(0, results?.size)
    }

    private fun assertResult(item: ResultItem, startIndex: Int, expectedPID: String) {
        assertEquals(startIndex, item.getHighlightStartOffset())
        assertEquals(startIndex + expectedPID.length, item.getHighlightEndOffset())
        val hyperlinkInfo = item.getHyperlinkInfo()
        assertNotNull(hyperlinkInfo)
        assertTrue(hyperlinkInfo is OpenUrlHyperlinkInfo)
    }

    private fun assertPIDs(textLine: String, PIDS: Array<Pair<Int, String>>) {
        val result = filter.applyFilter(textLine, textLine.length)
        assertNotNull(result)
        val resultItems = result!!.resultItems

        assertEquals(PIDS.size, resultItems.size)
        (PIDS zip resultItems).forEach { resultPair ->
            val expected = resultPair.first
            val expectedOffset = expected.first
            val expectedPID = expected.second
            assertResult(resultPair.second, expectedOffset, expectedPID)
        }
    }

    private fun assertPID(textLine: String, startIndex: Int, PID: String) {
        assertPIDs(textLine, arrayOf(Pair(startIndex, PID)))
    }
}
