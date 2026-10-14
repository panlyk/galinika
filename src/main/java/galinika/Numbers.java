package galinika;

import java.math.BigDecimal;

public final class Numbers {
  private Numbers() {}

  public static BigDecimal parse(String text, boolean positive) {
    try {
      BigDecimal value = new BigDecimal(text.trim().replace(',', '.'));
      if (positive ? value.signum() <= 0 : value.signum() < 0) {
        throw new IllegalArgumentException(
            "Η τιμή πρέπει να είναι " + (positive ? "θετική." : "μη αρνητική."));
      }
      return value;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Παρακαλώ εισάγετε έναν έγκυρο αριθμό.", e);
    }
  }
}
