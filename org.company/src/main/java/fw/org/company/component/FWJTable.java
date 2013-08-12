package fw.org.company.component;

import java.awt.Color;
import java.awt.Font;

import javax.annotation.PostConstruct;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;

@org.springframework.stereotype.Component("fwTable")
public class FWJTable extends JTable
{
  private static final long serialVersionUID = 1L;
  private Border outside = new MatteBorder(1, 0, 1, 0, Color.RED);

  private Border inside = new EmptyBorder(0, 1, 0, 1);

  private Border highlight = new CompoundBorder(this.outside, this.inside);

  @PostConstruct
  public void init() {
    setColumnSelectionAllowed(true);
    getTableHeader().setFont(new Font("Serif", 1, 15));
  }

  public java.awt.Component prepareRenderer(TableCellRenderer renderer, int row, int column)
  {
    java.awt.Component c = super.prepareRenderer(renderer, row, column);
    JComponent jc = (JComponent)c;

    if (isRowSelected(row)) {
      jc.setBorder(this.highlight);
      c.setBackground(Color.LIGHT_GRAY);
    } else {
      c.setBackground(Color.WHITE);
    }
    c.setFont(new Font("Serif", 0, 12));

    return c;
  }
}