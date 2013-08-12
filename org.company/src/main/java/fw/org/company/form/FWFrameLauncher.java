package fw.org.company.form;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FWFrameLauncher
{
  public static void main(String[] args)
  {
    String[] contextPaths = { "fwform-context.xml" };
    new ClassPathXmlApplicationContext(contextPaths);
  }
}