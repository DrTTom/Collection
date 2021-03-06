package de.tautenhahn.collection.generic.data.question;

import lombok.EqualsAndHashCode;
import lombok.Getter;


/**
 * Question for an auxiliary object of some type.
 *
 * @author TT
 */
@EqualsAndHashCode(callSuper = true)
public class ObjectChoiceQuestion extends ChoiceQuestion
{

  @Getter
  private final String auxType;

  /**
   * Creates instance.
   *
   * @param paramName name of the described objects attribute
   * @param text to display in label
   * @param form in which context the question should be displayed
   */
  public ObjectChoiceQuestion(String paramName, String text, String form, String auxType)
  {
    this("object-choice", paramName, text, form, auxType);
  }

  ObjectChoiceQuestion(String type, String paramName, String text, String form, String auxType)
  {
    super(type, paramName, text, form);
    this.auxType = auxType;
  }
}
