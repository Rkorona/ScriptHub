package com.scripthub.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CronNextRunCalculator {

    private val displayFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun nextRunTime(cronExpression: String): String {
        return try {
            val parts = cronExpression.trim().split(Regex("\\s+"))
            if (parts.size != 5) return "表达式无效"

            val (minExpr, hourExpr, dayExpr, monthExpr, weekExpr) = parts

            val cal = Calendar.getInstance()
            cal.add(Calendar.MINUTE, 1)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            // 最多向前推算一年（525600 分钟）
            repeat(525600) {
                val minute  = cal.get(Calendar.MINUTE)
                val hour    = cal.get(Calendar.HOUR_OF_DAY)
                val day     = cal.get(Calendar.DAY_OF_MONTH)
                val month   = cal.get(Calendar.MONTH) + 1
                val weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1  // 0=周日

                if (matches(minute,  minExpr,   0, 59) &&
                    matches(hour,    hourExpr,  0, 23) &&
                    matches(day,     dayExpr,   1, 31) &&
                    matches(month,   monthExpr, 1, 12) &&
                    matches(weekDay, weekExpr,  0,  6)
                ) {
                    return displayFormat.format(cal.time)
                }
                cal.add(Calendar.MINUTE, 1)
            }
            "无法计算"
        } catch (e: Exception) {
            "计算错误"
        }
    }

    /** 返回下次执行的毫秒时间戳，供排序使用；失败返回 Long.MAX_VALUE */
    fun nextRunMillis(cronExpression: String): Long {
        return try {
            val parts = cronExpression.trim().split(Regex("\\s+"))
            if (parts.size != 5) return Long.MAX_VALUE

            val (minExpr, hourExpr, dayExpr, monthExpr, weekExpr) = parts

            val cal = Calendar.getInstance()
            cal.add(Calendar.MINUTE, 1)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            repeat(525600) {
                val minute  = cal.get(Calendar.MINUTE)
                val hour    = cal.get(Calendar.HOUR_OF_DAY)
                val day     = cal.get(Calendar.DAY_OF_MONTH)
                val month   = cal.get(Calendar.MONTH) + 1
                val weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1

                if (matches(minute,  minExpr,   0, 59) &&
                    matches(hour,    hourExpr,  0, 23) &&
                    matches(day,     dayExpr,   1, 31) &&
                    matches(month,   monthExpr, 1, 12) &&
                    matches(weekDay, weekExpr,  0,  6)
                ) {
                    return cal.timeInMillis
                }
                cal.add(Calendar.MINUTE, 1)
            }
            Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    private fun matches(value: Int, expr: String, min: Int, max: Int): Boolean {
        if (expr == "*") return true
        return parseField(expr, min, max).contains(value)
    }

    private fun parseField(expr: String, min: Int, max: Int): Set<Int> {
        val result = mutableSetOf<Int>()
        for (part in expr.split(",")) {
            when {
                part == "*" -> result.addAll(min..max)
                part.startsWith("*/") -> {
                    val step = part.removePrefix("*/").toIntOrNull() ?: continue
                    if (step > 0) (min..max step step).forEach { result.add(it) }
                }
                part.contains("/") -> {
                    val sub = part.split("/", limit = 2)
                    val step = sub[1].toIntOrNull() ?: continue
                    if (step <= 0) continue
                    val (start, end) = if (sub[0].contains("-")) {
                        val bounds = sub[0].split("-", limit = 2)
                        (bounds[0].toIntOrNull() ?: min) to (bounds[1].toIntOrNull() ?: max)
                    } else {
                        (sub[0].toIntOrNull() ?: min) to max
                    }
                    (start..end step step).forEach { result.add(it) }
                }
                part.contains("-") -> {
                    val bounds = part.split("-", limit = 2)
                    val s = bounds[0].toIntOrNull() ?: continue
                    val e = bounds[1].toIntOrNull() ?: continue
                    result.addAll(s..e)
                }
                else -> part.toIntOrNull()?.let { result.add(it) }
            }
        }
        return result
    }
}
