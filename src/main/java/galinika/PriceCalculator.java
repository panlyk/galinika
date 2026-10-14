package galinika;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class PriceCalculator {
  private PriceCalculator() {}

  public record Ingredient(Substance substance, BigDecimal quantity, String unit) {
    public Ingredient {
      if (substance == null || quantity == null || quantity.signum() <= 0)
        throw new IllegalArgumentException("Η ποσότητα συστατικού πρέπει να είναι θετική.");
    }

    public BigDecimal cost() {
      return substance.pricePerQuantity().multiply(quantity);
    }
  }

  public static BigDecimal tieredCost(GalenicType type, BigDecimal quantity) {
    if (type == null) throw new IllegalArgumentException("Επιλέξτε τύπο γαληνικού.");
    if (quantity == null || quantity.signum() <= 0)
      throw new IllegalArgumentException("Η συνολική ποσότητα πρέπει να είναι θετική.");
    BigDecimal extra = quantity.subtract(type.minimumQuantity());
    if (extra.signum() <= 0) return type.minimumCost();
    return type.minimumCost()
        .add(
            extra
                .divide(type.divisionQuantity(), 0, RoundingMode.CEILING)
                .multiply(type.costIncrement()));
  }

  public static BigDecimal total(
      GalenicType type, BigDecimal quantity, List<Ingredient> ingredients, BigDecimal other) {
    if (other == null || other.signum() < 0)
      throw new IllegalArgumentException("Μη έγκυρο άλλο κόστος.");
    BigDecimal total = tieredCost(type, quantity).add(other);
    for (Ingredient ingredient : ingredients) total = total.add(ingredient.cost());
    return total.setScale(2, RoundingMode.HALF_UP);
  }

  public static String money(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
  }
}
