package me.ihqqq.giftcodeX.model;

import java.util.Objects;

public final class PlaytimeDuration {

    private static final long MS_SECOND = 1_000L;
    private static final long MS_MINUTE = 60   * MS_SECOND;
    private static final long MS_HOUR   = 60   * MS_MINUTE;
    private static final long MS_DAY    = 24   * MS_HOUR;
    private static final long MS_WEEK   = 7    * MS_DAY;
    private static final long MS_MONTH  = 30   * MS_DAY;
    private static final long MS_YEAR   = 365  * MS_DAY;

    private final int years;
    private final int months;
    private final int weeks;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int milliseconds;

    private PlaytimeDuration(Builder b) {
        this.years        = Math.max(0, b.years);
        this.months       = Math.max(0, b.months);
        this.weeks        = Math.max(0, b.weeks);
        this.days         = Math.max(0, b.days);
        this.hours        = Math.max(0, b.hours);
        this.minutes      = Math.max(0, b.minutes);
        this.seconds      = Math.max(0, b.seconds);
        this.milliseconds = Math.max(0, b.milliseconds);
    }

    public static PlaytimeDuration zero() {
        return new Builder().build();
    }

    public static PlaytimeDuration ofMinutes(int minutes) {
        return new Builder().minutes(minutes).build();
    }

    public long toMilliseconds() {
        return (long) years        * MS_YEAR
                + (long) months       * MS_MONTH
                + (long) weeks        * MS_WEEK
                + (long) days         * MS_DAY
                + (long) hours        * MS_HOUR
                + (long) minutes      * MS_MINUTE
                + (long) seconds      * MS_SECOND
                + milliseconds;
    }

    public long toTotalMinutes() {
        return toMilliseconds() / MS_MINUTE;
    }

    public boolean isZero() {
        return toMilliseconds() == 0;
    }

    public String toDisplayString() {
        if (isZero()) return "0m";
        StringBuilder sb = new StringBuilder();
        if (years        > 0) sb.append(years).append("y ");
        if (months       > 0) sb.append(months).append("mo ");
        if (weeks        > 0) sb.append(weeks).append("w ");
        if (days         > 0) sb.append(days).append("d ");
        if (hours        > 0) sb.append(hours).append("h ");
        if (minutes      > 0) sb.append(minutes).append("m ");
        if (seconds      > 0) sb.append(seconds).append("s ");
        if (milliseconds > 0) sb.append(milliseconds).append("ms");
        return sb.toString().trim();
    }


    public int getYears()        { return years; }
    public int getMonths()       { return months; }
    public int getWeeks()        { return weeks; }
    public int getDays()         { return days; }
    public int getHours()        { return hours; }
    public int getMinutes()      { return minutes; }
    public int getSeconds()      { return seconds; }
    public int getMilliseconds() { return milliseconds; }


    public Builder toBuilder() {
        return new Builder()
                .years(years).months(months).weeks(weeks).days(days)
                .hours(hours).minutes(minutes).seconds(seconds)
                .milliseconds(milliseconds);
    }

    public static final class Builder {
        private int years, months, weeks, days, hours, minutes, seconds, milliseconds;

        public Builder years(int v)        { this.years        = v; return this; }
        public Builder months(int v)       { this.months       = v; return this; }
        public Builder weeks(int v)        { this.weeks        = v; return this; }
        public Builder days(int v)         { this.days         = v; return this; }
        public Builder hours(int v)        { this.hours        = v; return this; }
        public Builder minutes(int v)      { this.minutes      = v; return this; }
        public Builder seconds(int v)      { this.seconds      = v; return this; }
        public Builder milliseconds(int v) { this.milliseconds = v; return this; }

        public PlaytimeDuration build() { return new PlaytimeDuration(this); }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaytimeDuration d)) return false;
        return toMilliseconds() == d.toMilliseconds();
    }

    @Override public int hashCode() { return Objects.hash(toMilliseconds()); }

    @Override public String toString() { return "PlaytimeDuration{" + toDisplayString() + "}"; }
}