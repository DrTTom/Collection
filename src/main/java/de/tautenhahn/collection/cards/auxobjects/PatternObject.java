package de.tautenhahn.collection.cards.auxobjects;

import de.tautenhahn.collection.cards.deck.Suits;
import de.tautenhahn.collection.generic.data.FreeText;
import de.tautenhahn.collection.generic.data.ImageRef;
import de.tautenhahn.collection.generic.data.MapBasedDescribedObjectInterpreter;


/**
 * Patterns are stored in persistence, name and primary key are i
 *
 * @author TT
 */
public class PatternObject extends MapBasedDescribedObjectInterpreter
{

  /**
   * Creates immutable instance.
   */
  public PatternObject()
  {
    super("pattern", true, new FreeText("name", 40, 1), new ImageRef(), new Suits());
  }
}
