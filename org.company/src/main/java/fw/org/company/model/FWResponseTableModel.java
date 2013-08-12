package fw.org.company.model;

import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class FWResponseTableModel
  implements TableModel
{
  private Stack<List<Object>> results;
  private final List<String> metaData;
  private final Set<String> sOfPostcodes;
  public static TableModel EMPTY_MODEL = new TableModel()
  {
    public void addTableModelListener(TableModelListener l)
    {
    }

    public Class<?> getColumnClass(int columnIndex)
    {
      return null;
    }

    public int getColumnCount()
    {
      return 0;
    }

    public String getColumnName(int columnIndex)
    {
      return null;
    }

    public int getRowCount()
    {
      return 0;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
      return null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return false;
    }

    public void removeTableModelListener(TableModelListener l)
    {
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
    }
  };

  FWResponseTableModel(List<String> aMetaData, Stack<List<Object>> sqlRowSet, Set<String> aSOfPostcodes)
  {
    this.metaData = aMetaData;
    this.results = sqlRowSet;
    this.sOfPostcodes = aSOfPostcodes;
  }

  public Set<String> getSOfPostcodes() {
    return this.sOfPostcodes;
  }

  public int getColumnCount() {
    return this.metaData.size();
  }

  public int getRowCount() {
    return this.results.size();
  }

  public String getColumnName(int column)
  {
    return (String)this.metaData.get(column);
  }

  public Class getColumnClass(int column)
  {
    int rowCount = getRowCount();
    Class leastGeneralUnifier = null;
    for (int row = 0; row < rowCount; row++) {
      Object value = getValueAt(row, column);
      if (value != null) {
        Class cls = value.getClass();
        if (leastGeneralUnifier == null)
          leastGeneralUnifier = cls;
        else if (!leastGeneralUnifier.isAssignableFrom(cls))
        {
          if (cls.isAssignableFrom(leastGeneralUnifier))
            leastGeneralUnifier = cls;
          else
            leastGeneralUnifier = traverseForClass(leastGeneralUnifier, cls);
        }
      }
    }
    if (leastGeneralUnifier == null) {
      leastGeneralUnifier = Object.class;
    }

    return leastGeneralUnifier;
  }

  protected Class traverseForClass(Class start, Class unifyWith)
  {
    if (start == Object.class) {
      return Object.class;
    }
    if (start.isAssignableFrom(unifyWith)) {
      return start;
    }
    return traverseForClass(start.getSuperclass(), unifyWith);
  }

  public Object getValueAt(int row, int column)
  {
    Object o = ((List)this.results.get(row)).get(column);
    if (o == null) {
      return null;
    }
    return o;
  }

  public boolean isCellEditable(int row, int column)
  {
    return false;
  }

  public void setValueAt(Object value, int row, int column)
  {
  }

  public void addTableModelListener(TableModelListener l)
  {
  }

  public void removeTableModelListener(TableModelListener l)
  {
  }
}