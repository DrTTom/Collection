package de.tautenhahn.collection.generic.process;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.tautenhahn.collection.generic.ApplicationContext;
import de.tautenhahn.collection.generic.data.DescribedObject;
import de.tautenhahn.collection.generic.data.DescribedObjectInterpreter;
import de.tautenhahn.collection.generic.data.Similarity;
import de.tautenhahn.collection.generic.persistence.Persistence;
import de.tautenhahn.collection.generic.persistence.PersistenceChangeListener;


/**
 * Handles the search process. The process may buffer results of earlier search for better performance but has
 * no further state. Objects in search result must be enriched by translations of attribute values if
 * available. An object in the search result may be passed to a view which may load auxiliary objects for
 * further details. The submission and edit processes may re-use questions created and answered by this
 * search.
 *
 * @author TT
 */
public class SearchProcess implements PersistenceChangeListener
{

  private static final Persistence PERSISTENCE = ApplicationContext.getInstance().getPersistence();

  private final String type;

  private final DescribedObjectInterpreter interpreter;

  /**
   * Caching only one last search, optimization for multi-user handling not done.
   */
  private Map<String, String> lastSearch;

  /**
   * May be empty as long as initialization appears to be too expensive.
   */
  private List<DescribedObject> lastCandidates;

  SearchProcess(String type)
  {
    this.type = type;
    interpreter = ApplicationContext.getInstance().getInterpreter(type);
    PERSISTENCE.addListener(this);
  }

  /**
   * Executes a search, reporting inconsistent search values but no missing attributes.
   *
   * @param parameters
   */
  public SearchResult search(Map<String, String> parameters)
  {
    return execute(parameters, null, false);
  }

  /**
   * Executes a search interpreting the given data as object description. All possible errors are reported.
   *
   * @param primKey optional, is returned with the result.
   * @param parameters
   */
  public SearchResult checkValues(String primKey, Map<String, String> parameters)
  {
    return execute(parameters, primKey, true);
  }

  /**
   * Actually does the search. If too expensive, maybe cache old similarity values as well. However, current
   * implementation does not require the similarity to be additive.
   *
   * @param parameters attribute values to match
   * @param primKey optional, if missing, some proposed value is used.
   * @param checkStrict to create problem messages for missing mandatory values
   */
  public SearchResult execute(Map<String, String> parameters, String primKey, boolean checkStrict)
  {
    DescribedObject searchMask = interpreter.createObject(primKey, parameters);

    Stream<DescribedObject> candidates;
    synchronized (this)
    {
      if (lastCandidates != null && refinesLastSearch(parameters))
      {
        candidates = lastCandidates.stream();
      }
      else
      {
        candidates = PERSISTENCE.findAll(type);
      }
    }

    SearchResult result = createSearchQuestions(searchMask, checkStrict);
    computeSearchResults(result, candidates, searchMask);

    return result;
  }

  private void computeSearchResults(SearchResult result,
                                    Stream<DescribedObject> candidates,
                                    DescribedObject searchMask)
  {
    List<DescribedObject.Matched> similars = new ArrayList<>();
    List<DescribedObject> remainingCandidates = new ArrayList<>();
    candidates.forEach(d -> {
      Similarity sim = interpreter.countSimilarity(searchMask, d);
      if (sim.possiblyEqual())
      {
        remainingCandidates.add(d);
        similars.add(new DescribedObject.Matched(d, sim));
      }
    });
    result.setNumberPossible(similars.size());
    similars.sort(Comparator.comparing(DescribedObject.Matched::getMatchValue).reversed());
    result.setMatches(similars.size() < 100 ? similars : similars.subList(0, 100));

    synchronized (this)
    {
      lastSearch = searchMask.getAttributes();
      lastCandidates = remainingCandidates;
    }
  }

  private SearchResult createSearchQuestions(DescribedObject questionContext, boolean checkStrict)
  {
    SearchResult result = new SearchResult(type, questionContext.getPrimKey(), checkStrict);
    result.setNumberTotal(PERSISTENCE.getNumberItems(type));
    result.setQuestions(new ArrayList<>(interpreter.getQuestions(questionContext, checkStrict)));
    return result;
  }

  private boolean refinesLastSearch(Map<String, String> params)
  {
    return lastSearch.entrySet().stream().allMatch(e -> e.getValue().equals(params.get(e.getKey())));
  }

  @Override
  public void onChange(String changedType)
  {
    synchronized (this)
    {
      lastCandidates = null;
    }
  }
}
