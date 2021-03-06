package de.tautenhahn.collection.generic.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.tautenhahn.collection.generic.ApplicationContext;
import de.tautenhahn.collection.generic.data.DescribedObject;


/**
 * Persistence implementation which holds all the data within a directory tree. DescribedObjects are loaded
 * into memory, so use only for small collections!
 *
 * @author TT
 */
public class WorkspacePersistence implements Persistence
{

  private static final String JSON_FILENAME = "objects.json";

  private final Map<String, Map<String, DescribedObject>> objects = new TreeMap<>();

  private final List<PersistenceChangeListener> listeners = new ArrayList<>();

  private Path collectionBaseDir;

  @Override
  public void store(DescribedObject item)
  {
    getTypeMap(item.getType()).put(item.getPrimKey(), item);
    listeners.forEach(l -> l.onChange(item.getType()));
    try
    {
      flush();
    }
    catch (IOException e)
    {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public DescribedObject find(String type, String primKey)
  {
    return Optional.ofNullable(objects.get(type)).map(m -> m.get(primKey)).orElse(null);
  }

  @Override
  public Stream<DescribedObject> findAll(String type)
  {
    return getTypeMap(type).values().stream();
  }

  @Override
  public Stream<DescribedObject> findByRestriction(String type, Map<String, String> exactValues)
  {
    return getTypeMap(type).values()
                           .stream()
                           .filter(d -> exactValues.entrySet()
                                                   .stream()
                                                   .allMatch(ev -> Objects.equals(d.getAttributes()
                                                                                   .get(ev.getKey()),
                                                                                  ev.getValue())));
  }

  @Override
  public void init(String... args) throws IOException
  {
    String collectionName = args.length > 0 ? args[0] : "default";
    collectionBaseDir = Paths.get(System.getProperty("user.home"), ".Collection", collectionName);
    if (!Files.exists(collectionBaseDir))
    {
      Files.createDirectories(collectionBaseDir);
    }
    Path path = collectionBaseDir.resolve(JSON_FILENAME);
    if (Files.exists(path))
    {
      try (Reader readerRes = Files.newBufferedReader(path, StandardCharsets.UTF_8))
      {
        importGson(readerRes);
      }
    }
  }

  @Override
  public int getNumberItems(String type)
  {
    return getTypeMap(type).size();
  }

  @Override
  public List<String> getKeyValues(String type)
  {
    ArrayList<String> result = new ArrayList<>(getTypeMap(type).keySet());
    Collections.sort(result);
    return result;
  }

  @Override
  public void delete(String type, String name)
  {
    getTypeMap(type).remove(name);
    listeners.forEach(l -> l.onChange(type));
  }

  @Override
  public boolean isReferenced(String type, String name, String... referencingType)
  {
    return Arrays.stream(referencingType)
                 .flatMap(refType -> findAll(refType))
                 .anyMatch(d -> name.equals(d.getAttributes().get(type)));
  }

  @Override
  public boolean keyExists(String type, String name)
  {
    return getTypeMap(type).containsKey(name);
  }

  @Override
  public List<String> getObjectTypes()
  {
    return new ArrayList<>(objects.keySet());
  }

  @Override
  public void store(InputStream ins, String ref) throws IOException
  {
    Files.copy(ins, collectionBaseDir.resolve(ref), StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Returns a path for a new binary resource, makes sure no directory becomes too full.
   *
   * @throws IOException in case of problems with the files
   */
  @Override
  public String createNewBinRef(String parentsPrimKey, String parentsType, String fileExtension)
    throws IOException
  {
    Path subDir = collectionBaseDir.resolve(Paths.get(sanitize(parentsType),
                                                      Integer.toHexString(parentsPrimKey.hashCode() & 0xff)));
    if (!Files.exists(subDir))
    {
      Files.createDirectories(subDir);
    }
    Path result = subDir.resolve(sanitize(parentsPrimKey) + "." + fileExtension);
    int diff = 0;
    while (Files.exists(result))
    {
      result = subDir.resolve(sanitize(parentsPrimKey) + "_" + (diff++) + "." + fileExtension);
    }
    return collectionBaseDir.relativize(result).toString();
  }

  @Override
  public InputStream find(String ref) throws IOException
  {
    Path path = collectionBaseDir.resolve(ref);
    if (!Files.isRegularFile(path) || !Files.isReadable(path))
    {
      throw new IOException("No readable file for reference " + ref + ", resolved to path " + path);
    }
    return Files.newInputStream(path);
  }

  @Override
  public boolean binObjectExists(String ref)
  {
    return Files.exists(collectionBaseDir.resolve(ref));
  }

  @Override
  public void addListener(PersistenceChangeListener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void flush() throws IOException
  {
    Gson gson = new GsonBuilder().create();

    Path path = collectionBaseDir.resolve(JSON_FILENAME);
    try (Writer wRes = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
      JsonWriter writer = new JsonWriter(wRes))
    {
      writer.setIndent(" ");
      writer.beginArray();
      objects.values().forEach(m -> m.values().forEach(d -> gson.toJson(d, DescribedObject.class, writer)));
      writer.endArray();
    }
  }

  private int importGson(Reader reader) throws IOException
  {
    int result = 0;
    Gson gson = new GsonBuilder().create();
    try (JsonReader jsRes = gson.newJsonReader(reader))
    {
      jsRes.beginArray();
      while (jsRes.hasNext())
      {
        DescribedObject item = gson.fromJson(jsRes, DescribedObject.class);
        getTypeMap(item.getType()).computeIfAbsent(item.getPrimKey(), key -> item);
        result++;
      }
      jsRes.endArray();
    }
    listeners.forEach(l -> l.onChange("*"));
    return result;
  }

  private String sanitize(String input)
  {
    CharsetEncoder ascii = StandardCharsets.US_ASCII.newEncoder();
    StringBuilder result = new StringBuilder();
    for ( char x : input.toCharArray() )
    {
      if (Character.isLetterOrDigit(x) && ascii.canEncode(x))
      {
        result.append(x);
      }
      else
      {
        result.append('_').append((int)x);
      }
    }
    return result.toString();
  }

  private Map<String, DescribedObject> getTypeMap(String type)
  {
    Comparator<String> numberAware = (a, b) -> a.matches("\\d+") && b.matches("\\d+")
      ? Integer.parseInt(a) - Integer.parseInt(b) : a.compareTo(b);
    return objects.computeIfAbsent(type, t -> new TreeMap<>(numberAware));
  }

  /**
   * Writes filtered contents as ZIP into given output stream.
   *
   * @param outs target stream
   * @param filter TODO: filter json contents as well!
   * @throws IOException
   */
  public void exportZip(OutputStream outs, Predicate<DescribedObject> filter) throws IOException
  {
    flush();
    List<String> relPathes = new ArrayList<>();
    relPathes.add(JSON_FILENAME);

    Map<String, Collection<String>> keys = getKeysForExportableEntities();
    keys.forEach((type, binAttrs) -> getTypeMap(type).values()
                                                     .stream()
                                                     .filter(filter)
                                                     .flatMap(v -> v.getAttributes().entrySet().stream())
                                                     .filter(e -> binAttrs.contains(e.getKey()))
                                                     .map(Map.Entry::getValue)
                                                     .filter(Objects::nonNull)
                                                     .forEach(relPathes::add));
    new SecureZip().create(collectionBaseDir, relPathes, outs);
  }

  Map<String, Collection<String>> getKeysForExportableEntities()
  {
    ApplicationContext app = ApplicationContext.getInstance();
    return getObjectTypes().stream()
                           .collect(Collectors.toMap(Function.identity(),
                                                     type -> app.getInterpreter(type)
                                                                .getBinaryValuedAttributes()));
  }

  /**
   * Imports contents of given ZIP file, avoids security problems.
   *
   * @param ins source stream
   * @throws IOException in case of streaming problems
   */
  public void importZip(InputStream ins) throws IOException
  {
    new SecureZip().expand(ins, collectionBaseDir, JSON_FILENAME, this::importGson);
    flush();
  }

  @Override
  public String toString()
  {
    if (collectionBaseDir == null)
    {
      return "WorksapcePesistence[nothing loaded yet]";
    }
    return "WorkspacePesistence[" + collectionBaseDir.getFileName() + ", loaded " + objects.size()
           + " types]";
  }
}
