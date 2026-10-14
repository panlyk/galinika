package galinika;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PriceCalculatorTest {
  private static BigDecimal n(String value) {
    return new BigDecimal(value);
  }

  private final GalenicType cream =
      new GalenicType("Cream", n("50"), n("10"), n("2.26"), n("0.21"));

  @ParameterizedTest
  @ValueSource(strings = {"", "abc", "NaN", "Infinity", "-1", "0"})
  void rejectsInvalidQuantities(String value) {
    assertThrows(IllegalArgumentException.class, () -> Numbers.parse(value, true));
  }

  @Test
  void acceptsCommaDecimalsAndOptionalZeroCost() {
    assertEquals(n("1.25"), Numbers.parse(" 1,25 ", true));
    assertEquals(BigDecimal.ZERO, Numbers.parse("0", false));
  }

  @Test
  void chargesFullIncrementsOnlyAboveBoundaries() {
    assertEquals(n("2.26"), PriceCalculator.tieredCost(cream, n("50")));
    assertEquals(n("2.47"), PriceCalculator.tieredCost(cream, n("50.001")));
    assertEquals(n("2.47"), PriceCalculator.tieredCost(cream, n("60")));
    assertEquals(n("2.68"), PriceCalculator.tieredCost(cream, n("60.0001")));
  }

  @Test
  void decimalBoundariesAreExact() {
    GalenicType tiny = new GalenicType("Tiny", n("0.1"), n("0.1"), n("1"), n("0.2"));
    assertEquals(n("1.4"), PriceCalculator.tieredCost(tiny, n("0.3")));
  }

  @Test
  void roundsOnceAtTheFinalTotal() {
    Substance substance = new Substance("Example", n("0.004"));
    var ingredient = new PriceCalculator.Ingredient(substance, n("1"), "gr");
    assertEquals(
        n("2.27"),
        PriceCalculator.total(cream, n("50"), List.of(ingredient, ingredient), BigDecimal.ZERO));
    assertEquals("1.01 €", PriceCalculator.money(n("1.005")));
  }

  @Test
  void rejectsInvalidDomainValues() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new GalenicType("Bad", n("50"), BigDecimal.ZERO, n("1"), n("1")));
    assertThrows(
        IllegalArgumentException.class,
        () -> PriceCalculator.total(cream, n("50"), List.of(), n("-1")));
    assertThrows(IllegalArgumentException.class, () -> PriceCalculator.tieredCost(null, n("50")));
  }
}
