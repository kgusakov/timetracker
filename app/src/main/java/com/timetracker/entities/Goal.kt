package com.timetracker.entities

import org.joda.time.Duration

import org.joda.time.DateTimeConstants as DT

data class Goal(val id: Int, val catergoryId: Int, val type: GoalType, val goal: Duration)
data class CreateGoal(val catergoryId: Int, val type: GoalType, val goal: Duration)

enum class DayOfWeek {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
    THURSDAY, FRIDAY, SATURDAY;

    companion object {
        private val intsToDays = mapOf(DT.SUNDAY to SUNDAY,
            DT.MONDAY to MONDAY,
            DT.TUESDAY to TUESDAY,
            DT.WEDNESDAY to WEDNESDAY,
            DT.THURSDAY to THURSDAY,
            DT.FRIDAY to FRIDAY,
            DT.SATURDAY to SATURDAY
        )

        fun fromJodaInt(num: Int) = if (intsToDays.containsKey(num)) intsToDays.getValue(num)
            else throw IllegalArgumentException("Day of th week should be > 0 and < 8")

        fun toJodaInt(day: DayOfWeek) = {
            val filteredMap = intsToDays.filterValues { it == day }.values.first()
        }
    }
}

sealed class GoalType {
    class Weekly : GoalType()
    class Monthly : GoalType()
    class Daily(days: Set<DayOfWeek>) : GoalType()
}
