package galinika;

import java.math.BigDecimal;

public record GalenicType(
    String name,
    BigDecimal minimumQuantity,
    BigDecimal divisionQuantity,
    BigDecimal minimumCost,
    BigDecimal costIncrement) {
  public GalenicType {
    if (name == null
        || name.isBlank()
        || minimumQuantity == null
        || divisionQuantity == null
        || minimumCost == null
        || costIncrement == null
        || minimumQuantity.signum() < 0
        || divisionQuantity.signum() <= 0
        || minimumCost.signum() < 0
        || costIncrement.signum() < 0)
      throw new IllegalArgumentException("Μη έγκυρος τύπος γαληνικού.");
  }

  public String getGalenicTypeName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
