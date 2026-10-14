package galinika;

import java.math.BigDecimal;

public record Substance(String name, BigDecimal pricePerQuantity) {
  public Substance {
    if (name == null || name.isBlank() || pricePerQuantity == null || pricePerQuantity.signum() < 0)
      throw new IllegalArgumentException("Μη έγκυρο συστατικό.");
  }

  public String getName() {
    return name;
  }

  public BigDecimal getPricePerQuantity() {
    return pricePerQuantity;
  }

  @Override
  public String toString() {
    return name;
  }
}
