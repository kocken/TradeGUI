import Types.Currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

class Util {

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    static BigDecimal getPerHour(BigDecimal value, long startTime) {
        return value.multiply(new BigDecimal(3600000D))
                .divide(new BigDecimal(System.currentTimeMillis()-startTime), 2, RoundingMode.CEILING);
    }

    static String getLocalTime() {
        LocalDateTime now = LocalDateTime.now();

        return (now.getHour() < 10 ? "0" : "") + now.getHour() + ":" +
                (now.getMinute() < 10 ? "0" : "") + now.getMinute() + ":" +
                (now.getSecond() < 10 ? "0" : "") + now.getSecond();
    }

    static String getFormattedRunTime(long ms) {
        long days = TimeUnit.MILLISECONDS.toDays(ms);
        long hours = TimeUnit.MILLISECONDS.toHours(ms) - (days * 24);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - (TimeUnit.MILLISECONDS.toHours(ms) * 60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - (TimeUnit.MILLISECONDS.toMinutes(ms) * 60);

        if (days > 0)
            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        else if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return String.format("%02d:%02d", minutes, seconds);
    }

    static BigDecimal getCurrencyMultiplier(Currency currency1, Currency currency2) {
        return currency2.getUsdValue().divide(currency1.getUsdValue(), MathContext.DECIMAL128);
    }

}