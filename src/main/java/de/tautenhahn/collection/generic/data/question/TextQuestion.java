package de.tautenhahn.collection.generic.data.question;

import lombok.EqualsAndHashCode;
import lombok.Getter;


/**
 * Question for a text value.
 *
 * @author TT
 */
@EqualsAndHashCode(callSuper = true)
public class TextQuestion extends Question
{

  @Getter
  private int cols;

  @Getter
  private int rows;

  /**
   * Creates instance.
   *
   * @param paramName name of the described objects attribute
   * @param text to display in label
   * @param form in which context the question should be displayed
   */
  public TextQuestion(String paramName, String text, String form, int cols, int lines)
  {
    super(lines == 1 ? "text-input" : "bigtext-input", paramName, text, form);
    this.cols = cols;
    this.rows = lines;
  }
}
