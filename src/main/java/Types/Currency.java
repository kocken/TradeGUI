package Types;

import java.math.BigDecimal;
import java.text.DecimalFormat;

// Hard-coded values for demo purposes
public enum Currency {
    USD(new BigDecimal(1), new DecimalFormat("###,###,###,###,##0.##")),
    EUR(new BigDecimal(0.88680077), new DecimalFormat("###,###,###,###,##0.##")),
    SEK(new BigDecimal(9.26912917), new DecimalFormat("###,###,###,###,##0.##"));
    //BTC(new BigDecimal(0.000196312), new DecimalFormat("###,###,###,###,##0.######")),
    //ETH(new BigDecimal(0.006123324), new DecimalFormat("###,###,###,###,##0.#####"));

    private BigDecimal usdValue;
    private DecimalFormat decimalFormat;
    Currency(BigDecimal usdValue, DecimalFormat decimalFormat) {
        this.usdValue = usdValue;
        this.decimalFormat = decimalFormat;
    }

    public BigDecimal getUsdValue() {
        return usdValue;
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }
}