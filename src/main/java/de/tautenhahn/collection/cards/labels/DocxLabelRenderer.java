package de.tautenhahn.collection.cards.labels;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.google.gson.Gson;

import de.tautenhahn.easydata.AccessibleData;
import de.tautenhahn.easydata.DataIntoTemplate;
import de.tautenhahn.easydata.docx.DocxAdapter;


/**
 * Creates labels in DOCX format.
 *
 * @author ttautenhahn
 */
public class DocxLabelRenderer implements LabelRenderer
{

  @Override
  public void render(List<Label> labels, OutputStream target) throws Exception
  {
    AccessibleData data = AccessibleData.byBean(labels);
    System.out.println(new Gson().toJson(data));

    DocxAdapter adapter = new DocxAdapter(new DataIntoTemplate(data, '(', '@', ')'));

    try (InputStream ins = DocxLabelRenderer.class.getResourceAsStream("template.docx"))
    {
      adapter.convert(ins, target);
    }
  }

  @Override
  public String getMediaType()
  {
    return "application/docx";
  }
}
