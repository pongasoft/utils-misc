package org.linkedin.util.lang;

import junit.framework.TestCase;
import org.linkedin.util.clock.Timespan;
import org.linkedin.util.concurrent.ThreadControl;
import org.linkedin.util.concurrent.ThreadPerTaskExecutor;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author yan@pongasoft.com
 */
public class TestAutoCloseable extends TestCase
{
  public static class AC implements AutoCloseable
  {
    public static ArrayList<AC> INSTANCES = new ArrayList<>();

    private final ThreadControl _tc;
    private final String _name;
    private volatile boolean _closed = false;

    public AC(ThreadControl tc, String name) throws Exception
    {
      _tc = tc;
      _name = name;

      INSTANCES.add(this);

      _tc.blockWithException("ac.<init>[" + _name + "]");
    }

    public String getName()
    {
      return _name;
    }

    public boolean isClosed()
    {
      return _closed;
    }

    @Override
    public void close() throws Exception
    {
      _tc.blockWithException("ac.close[" + _name + "]");
      _closed = true;
    }
  }

  @Override
  public void setUp() throws Exception
  {
    super.setUp();
    AC.INSTANCES.clear();
  }

  public void testSimpleCase() throws Exception
  {
    final ThreadControl tc = new ThreadControl(Timespan.parse("5s"));

    Future<Void> future = ThreadPerTaskExecutor.execute(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        tc.unblock("ac.<init>[0]");
        tc.unblock("ac.close[0]");
        return null;
      }
    });

    try(AC ac0 = new AC(tc, "0"))
    {
      assertEquals("0", ac0.getName());
      assertFalse(ac0.isClosed());
    }

    assertTrue(AC.INSTANCES.iterator().next().isClosed());
    assertNull(future.get(10, TimeUnit.SECONDS));
  }

  /**
   * With more than 1 entry in the list, first entry fails in the constructor
   */
  public void testWithExceptionInTryList1() throws Exception
  {
    final ThreadControl tc = new ThreadControl(Timespan.parse("5s"));

    Future<Void> future = ThreadPerTaskExecutor.execute(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        tc.unblock("ac.<init>[0]", new Exception("0.<init>"));
        try
        {
          tc.waitForBlock("ac.<init>[1]", Timespan.parse("1s"));
          fail("should have failed");
        }
        catch(TimeoutException ignored)
        {
          // expected
        }
        return null;
      }
    });

    try
    {
      try(AC ac0 = new AC(tc, "0"); AC ac1 = new AC(tc, "1"))
      {
        fail("never reached... exception in the try block list");
      }
      fail("should have failed");
    }
    catch(Exception e)
    {
      assertEquals("0.<init>", e.getMessage());
    }

    assertEquals(1, AC.INSTANCES.size());
    assertFalse(AC.INSTANCES.get(0).isClosed());
    assertNull(future.get(10, TimeUnit.SECONDS));
  }

  /**
   * With more than 1 entry in the list, second entry fails in the constructor
   */
  public void testWithExceptionInTryList2() throws Exception
  {
    final ThreadControl tc = new ThreadControl(Timespan.parse("5s"));

    Future<Void> future = ThreadPerTaskExecutor.execute(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        tc.unblock("ac.<init>[0]");
        tc.unblock("ac.<init>[1]", new Exception("1.<init>"));
        // close for 1st entry should be called
        tc.unblock("ac.close[0]");
        try
        {
          tc.waitForBlock("ac.close[1]", Timespan.parse("1s"));
          fail("should have failed");
        }
        catch(TimeoutException ignored)
        {
          // expected
        }
        return null;
      }
    });

    try
    {
      try(AC ac0 = new AC(tc, "0"); AC ac1 = new AC(tc, "1"))
      {
        fail("never reached... exception in the try block list");
      }
      fail("should have failed");
    }
    catch(Exception e)
    {
      assertEquals("1.<init>", e.getMessage());
    }

    assertEquals(2, AC.INSTANCES.size());
    assertTrue(AC.INSTANCES.get(0).isClosed());
    assertFalse(AC.INSTANCES.get(1).isClosed());
    assertNull(future.get(10, TimeUnit.SECONDS));
  }

  /**
   * With more than 1 entry in the list, first entry fails in the close
   */
  public void testWithExceptionInTryList3() throws Exception
  {
    final ThreadControl tc = new ThreadControl(Timespan.parse("5s"));

    Future<Void> future = ThreadPerTaskExecutor.execute(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        tc.unblock("ac.<init>[0]");
        tc.unblock("ac.<init>[1]");
        tc.unblock("ac.close[1]"); // reverse order...
        tc.unblock("ac.close[0]", new Exception("0.close"));
        return null;
      }
    });

    try
    {
      try(AC ac0 = new AC(tc, "0"); AC ac1 = new AC(tc, "1"))
      {
        assertFalse(ac0.isClosed());
        assertFalse(ac1.isClosed());
      }
      fail("should have failed");
    }
    catch(Exception e)
    {
      assertEquals("0.close", e.getMessage());
    }

    assertEquals(2, AC.INSTANCES.size());
    assertFalse(AC.INSTANCES.get(0).isClosed());
    assertTrue(AC.INSTANCES.get(1).isClosed());
    assertNull(future.get(10, TimeUnit.SECONDS));
  }

  /**
   * With more than 1 entry in the list, second entry fails in the close
   */
  public void testWithExceptionInTryList4() throws Exception
  {
    final ThreadControl tc = new ThreadControl(Timespan.parse("5s"));

    Future<Void> future = ThreadPerTaskExecutor.execute(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        tc.unblock("ac.<init>[0]");
        tc.unblock("ac.<init>[1]");
        tc.unblock("ac.close[1]", new Exception("1.close")); // reverse order...
        tc.unblock("ac.close[0]"); // should still be called!
        return null;
      }
    });

    try
    {
      try(AC ac0 = new AC(tc, "0"); AC ac1 = new AC(tc, "1"))
      {
        assertFalse(ac0.isClosed());
        assertFalse(ac1.isClosed());
      }
      fail("should have failed");
    }
    catch(Exception e)
    {
      assertEquals("1.close", e.getMessage());
    }

    assertEquals(2, AC.INSTANCES.size());
    assertTrue(AC.INSTANCES.get(0).isClosed());
    assertFalse(AC.INSTANCES.get(1).isClosed());
    assertNull(future.get(10, TimeUnit.SECONDS));
  }

  /**
   * With more than 1 entry in the list, both entries fail in the close
   */
  public void testWithExceptionInTryList5() throws Exception
  {
    final ThreadControl tc = new ThreadControl(Timespan.parse("5s"));

    Future<Void> future = ThreadPerTaskExecutor.execute(new Callable<Void>()
    {
      @Override
      public Void call() throws Exception
      {
        tc.unblock("ac.<init>[0]");
        tc.unblock("ac.<init>[1]");
        tc.unblock("ac.close[1]", new Exception("1.close")); // reverse order...
        tc.unblock("ac.close[0]", new Exception("0.close")); // should still be called!
        return null;
      }
    });

    try
    {
      try(AC ac0 = new AC(tc, "0"); AC ac1 = new AC(tc, "1"))
      {
        assertFalse(ac0.isClosed());
        assertFalse(ac1.isClosed());
      }
      fail("should have failed");
    }
    catch(Exception e)
    {
      assertEquals("1.close", e.getMessage());
      assertEquals("0.close", e.getSuppressed()[0].getMessage());
    }

    assertEquals(2, AC.INSTANCES.size());
    assertFalse(AC.INSTANCES.get(0).isClosed());
    assertFalse(AC.INSTANCES.get(1).isClosed());
    assertNull(future.get(10, TimeUnit.SECONDS));
  }

}
