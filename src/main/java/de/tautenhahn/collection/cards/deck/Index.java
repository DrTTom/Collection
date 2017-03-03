package de.tautenhahn.collection.cards.deck;

import de.tautenhahn.collection.generic.data.AttributeInterpreter;
import de.tautenhahn.collection.generic.data.DescribedObject;
import de.tautenhahn.collection.generic.data.Similarity;


public class Index extends AttributeInterpreter
{

  protected Index()
  {
    super("index", Flag.EXACT);
  }

  @Override
  public boolean isLegalValue(String value, DescribedObject context)
  {
    return true;
  }

  @Override
  protected Similarity correllateValue(String a, String b, DescribedObject context)
  {
    if (a.equals(b))
    {
      return Similarity.SIMILAR;
    }
    if (Character.isDigit(a.charAt(0)) && Character.isDigit(b.charAt(0)) && !a.contains("/")
        && !b.contains("/"))
    {
      // ignore number in this cases - deck may have different number of
      // cards
      int i = 0;
      while (i < a.length() && !Character.isLetter(a.charAt(i)))
      {
        i++;
      }
      if (i < a.length() && b.endsWith(a.substring(i)))
      {
        return Similarity.ALMOST_SIMILAR;
      }
    }
    return Similarity.DIFFERENT;
  }
}