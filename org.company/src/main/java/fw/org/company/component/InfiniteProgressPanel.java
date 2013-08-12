package fw.org.company.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

public class InfiniteProgressPanel extends JComponent
  implements MouseListener
{
  protected Area[] ticker = null;

  protected Thread animation = null;

  protected boolean started = false;

  protected int alphaLevel = 0;

  protected int rampDelay = 300;

  protected float shield = 0.7F;

  protected String text = "";

  protected int barsCount = 14;

  protected float fps = 15.0F;

  protected RenderingHints hints = null;

  public InfiniteProgressPanel()
  {
    this("");
  }

  public InfiniteProgressPanel(String text)
  {
    this(text, 14);
  }

  public InfiniteProgressPanel(String text, int barsCount)
  {
    this(text, barsCount, 0.7F);
  }

  public InfiniteProgressPanel(String text, int barsCount, float shield)
  {
    this(text, barsCount, shield, 15.0F);
  }

  public InfiniteProgressPanel(String text, int barsCount, float shield, float fps)
  {
    this(text, barsCount, shield, fps, 300);
  }

  public InfiniteProgressPanel(String text, int barsCount, float shield, float fps, int rampDelay)
  {
    this.text = text;
    this.rampDelay = (rampDelay >= 0 ? rampDelay : 0);
    this.shield = (shield >= 0.0F ? shield : 0.0F);
    this.fps = (fps > 0.0F ? fps : 15.0F);
    this.barsCount = (barsCount > 0 ? barsCount : 14);

    this.hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    this.hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    this.hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  }

  public void setText(String text)
  {
    this.text = text;
    repaint();
  }

  public String getText()
  {
    return this.text;
  }

  public void start()
  {
    addMouseListener(this);
    setVisible(true);
    this.ticker = buildTicker();
    this.animation = new Thread(new Animator(true));
    this.animation.start();
  }

  public void stop()
  {
    if (this.animation != null) {
      this.animation.interrupt();
      this.animation = null;
      this.animation = new Thread(new Animator(false));
      this.animation.start();
    }
  }

  public void interrupt()
  {
    if (this.animation != null) {
      this.animation.interrupt();
      this.animation = null;

      removeMouseListener(this);
      setVisible(false);
    }
  }

  public void paintComponent(Graphics g)
  {
    if (this.started)
    {
      int width = getWidth();
      int height = getHeight();

      double maxY = 0.0D;

      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHints(this.hints);

      g2.setColor(new Color(255, 255, 255, (int)(this.alphaLevel * this.shield)));
      g2.fillRect(0, 0, getWidth(), getHeight());

      for (int i = 0; i < this.ticker.length; i++)
      {
        int channel = 224 - 128 / (i + 1);
        g2.setColor(new Color(channel, channel, channel, this.alphaLevel));
        g2.fill(this.ticker[i]);

        Rectangle2D bounds = this.ticker[i].getBounds2D();
        if (bounds.getMaxY() > maxY) {
          maxY = bounds.getMaxY();
        }
      }
      if ((this.text != null) && (this.text.length() > 0))
      {
        FontRenderContext context = g2.getFontRenderContext();
        TextLayout layout = new TextLayout(this.text, getFont(), context);
        Rectangle2D bounds = layout.getBounds();
        g2.setColor(getForeground());
        layout.draw(g2, (float)(width - bounds.getWidth()) / 2.0F, 
          (float)(maxY + layout.getLeading() + 2.0F * layout.getAscent()));
      }
    }
  }

  private Area[] buildTicker()
  {
    Area[] ticker = new Area[this.barsCount];
    Point2D.Double center = new Point2D.Double(getWidth() / 2.0D, getHeight() / 2.0D);
    double fixedAngle = 6.283185307179586D / this.barsCount;

    for (double i = 0.0D; i < this.barsCount; i += 1.0D)
    {
      Area primitive = buildPrimitive();

      AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
      AffineTransform toBorder = AffineTransform.getTranslateInstance(45.0D, -6.0D);
      AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());

      AffineTransform toWheel = new AffineTransform();
      toWheel.concatenate(toCenter);
      toWheel.concatenate(toBorder);

      primitive.transform(toWheel);
      primitive.transform(toCircle);

      ticker[((int)i)] = primitive;
    }

    return ticker;
  }

  private Area buildPrimitive()
  {
    Rectangle2D.Double body = new Rectangle2D.Double(6.0D, 0.0D, 30.0D, 12.0D);
    Ellipse2D.Double head = new Ellipse2D.Double(0.0D, 0.0D, 12.0D, 12.0D);
    Ellipse2D.Double tail = new Ellipse2D.Double(30.0D, 0.0D, 12.0D, 12.0D);

    Area tick = new Area(body);
    tick.add(new Area(head));
    tick.add(new Area(tail));

    return tick;
  }
  public void mouseClicked(MouseEvent e) {
  }
  public void mousePressed(MouseEvent e) {  } 
  public void mouseReleased(MouseEvent e) {  } 
  public void mouseEntered(MouseEvent e) {  } 
  public void mouseExited(MouseEvent e) {  } 
  private class Animator implements Runnable { private boolean rampUp = true;

    protected Animator(boolean rampUp)
    {
      this.rampUp = rampUp;
    }

    public void run()
    {
      Point2D.Double center = new Point2D.Double(InfiniteProgressPanel.this.getWidth() / 2.0D, InfiniteProgressPanel.this.getHeight() / 2.0D);
      double fixedIncrement = 6.283185307179586D / InfiniteProgressPanel.this.barsCount;
      AffineTransform toCircle = AffineTransform.getRotateInstance(fixedIncrement, center.getX(), center.getY());

      long start = System.currentTimeMillis();
      if (InfiniteProgressPanel.this.rampDelay == 0) {
        InfiniteProgressPanel.this.alphaLevel = (this.rampUp ? 255 : 0);
      }
      InfiniteProgressPanel.this.started = true;
      boolean inRamp = this.rampUp;

      while (!Thread.interrupted())
      {
        if (!inRamp)
        {
          for (int i = 0; i < InfiniteProgressPanel.this.ticker.length; i++) {
            InfiniteProgressPanel.this.ticker[i].transform(toCircle);
          }
        }
        InfiniteProgressPanel.this.repaint();

        if (this.rampUp)
        {
          if (InfiniteProgressPanel.this.alphaLevel < 255)
          {
            InfiniteProgressPanel.this.alphaLevel = ((int)(255L * (System.currentTimeMillis() - start) / InfiniteProgressPanel.this.rampDelay));
            if (InfiniteProgressPanel.this.alphaLevel >= 255)
            {
              InfiniteProgressPanel.this.alphaLevel = 255;
              inRamp = false;
            }
          }
        } else if (InfiniteProgressPanel.this.alphaLevel > 0) {
          InfiniteProgressPanel.this.alphaLevel = ((int)(255L - 255L * (System.currentTimeMillis() - start) / InfiniteProgressPanel.this.rampDelay));
          if (InfiniteProgressPanel.this.alphaLevel <= 0)
          {
            InfiniteProgressPanel.this.alphaLevel = 0;
            break;
          }
        }

        try
        {
          Thread.sleep(inRamp ? 10 : (int)(1000.0F / InfiniteProgressPanel.this.fps));
        } catch (InterruptedException ie) {
          break;
        }
        Thread.yield();
      }

      if (!this.rampUp)
      {
        InfiniteProgressPanel.this.started = false;
        InfiniteProgressPanel.this.repaint();

        InfiniteProgressPanel.this.setVisible(false);
        InfiniteProgressPanel.this.removeMouseListener(InfiniteProgressPanel.this);
      }
    }
  }
}