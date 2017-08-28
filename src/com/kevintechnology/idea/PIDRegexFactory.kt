package com.kevintechnology.idea

class PIDRegexFactory() {
    companion object {
        // Process IDs range from 0-99999 on most systems
        // Generated using https://rgxg.github.io and tweaked to require word boundaries while ignoring negative numbers
        // and percentages.
        private const val PID_PATTERN = "\\b(?<!-)([1-9][0-9]{0,4}|0)\\b(?!%)"

        /**
         * @param pattern Regex pattern where instances of "$0" should be replaced with a regex pattern for recognizing
         * PID numbers.
         */
        fun regexForPattern(pattern: String) : Regex {
            val replacementToken = "$0"
            val patternWithPIDPatternsInserted = pattern.replace(replacementToken, PID_PATTERN)
            return Regex(patternWithPIDPatternsInserted)
        }
    }
}
