/*
 * Copyright 2010-2010 LinkedIn, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package test.util.collections

import org.linkedin.groovy.util.collections.GroovyCollectionsUtils
import org.linkedin.util.clock.Timespan

/**
 * @author ypujante@linkedin.com */
class TestGroovyCollectionsUtils extends GroovyTestCase
{
  public void testMap()
  {
    assertTrue(GroovyCollectionsUtils.compareIgnoreType((Map) null, (Map) null))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType(null, [:]))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType([:], null))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType([:], [:]))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType([a: null], [b: null]))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType([a: 1], [b: null]))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType(new LinkedHashMap([a: 200, b: 21]), new TreeMap([a: 200, b: 21])))
  }

  public void testList()
  {
    assertTrue(GroovyCollectionsUtils.compareIgnoreType((List) null, (List) null))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType(null, []))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType([], null))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType([], []))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType([1], [null]))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType([null], [1]))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType([1,2,3], [1,2,3]))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType(new ArrayList([1,2,3]), new Vector([1,2,3])))
  }

  public void testSet()
  {
    assertTrue(GroovyCollectionsUtils.compareIgnoreType((Set) null, (Set) null))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType(null, new HashSet()))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType(new HashSet(), null))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType(new HashSet(), new HashSet()))
    assertFalse(GroovyCollectionsUtils.compareIgnoreType(new HashSet([1]), new HashSet()))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType(new HashSet([1,2,3]), new HashSet([1,2,3])))
    assertTrue(GroovyCollectionsUtils.compareIgnoreType(new HashSet([3,2,1]), new LinkedHashSet([2,3,1])))
  }

  public void testCollection()
  {
    assertTrue(GroovyCollectionsUtils.compareContent((Collection) null, (Collection) null))
    assertFalse(GroovyCollectionsUtils.compareContent((Collection) null, new HashSet()))
    assertFalse(GroovyCollectionsUtils.compareContent(new HashSet(), (Collection) null))
    assertTrue(GroovyCollectionsUtils.compareContent(new HashSet(), new ArrayList()))
    assertFalse(GroovyCollectionsUtils.compareContent(new HashSet([1]), new ArrayList()))
    assertTrue(GroovyCollectionsUtils.compareContent(new HashSet([1,2,3]), [1,2,3]))
    assertTrue(GroovyCollectionsUtils.compareContent([3,2,1], new LinkedHashSet([2,3,1])))
    assertTrue(GroovyCollectionsUtils.compareContent([3,2,1], [1,2,3]))
  }

  public void testFlatten()
  {
    def src =
    [
        k1: 'v1',
        k2:
        [
            k21: 'v21',
            k22: ['v221', 'v222']
        ],
        k3: ['v31', ['v321', 'v322'], [k33: 'v33']],
        k4: [],
        k5: [:],
        k6: ['v61', [], [:], 'v64']
    ]

    def expected = [
        k1: 'v1',
        'k2.k21': 'v21',
        'k2.k22[0]': 'v221',
        'k2.k22[1]': 'v222',
        'k3[0]': 'v31',
        'k3[1][0]': 'v321',
        'k3[1][1]': 'v322',
        'k3[2].k33': 'v33',
        'k6[0]': 'v61',
        'k6[3]': 'v64'
    ]

    assertTrue(GroovyCollectionsUtils.compareIgnoreType(expected,
                                                        GroovyCollectionsUtils.flatten(src)))
  }

  /**
   * Test when a handler is provided
   */
  public void testFlattenWithHandler()
  {
    def now = new Date()

    def src =
    [
      k1: 'v1',
      k2: Timespan.parse('3s'),
      k3: now
    ]

    def handler = { o ->
      if(o instanceof Timespan)
        o.toString()
      else
        o
    }

    def expected = [
      k1: 'v1',
      k2: '3s',
      k3: now
    ]

    assertTrue(GroovyCollectionsUtils.compareIgnoreType(expected,
                                                        GroovyCollectionsUtils.flatten(src, handler)))

    handler = { o ->
      if(o instanceof Timespan)
        o.toString()
      else
        if(o instanceof Date)
          [Timespan.parse('1s'), 45, [k2: 3]]
        else
          o
    }

    expected = [
      k1: 'v1',
      k2: '3s',
      'k3[0]': '1s',
      'k3[1]': 45,
      'k3[2].k2': 3
    ]

    assertTrue(GroovyCollectionsUtils.compareIgnoreType(expected,
                                                        GroovyCollectionsUtils.flatten(src, handler)))

    handler = { o ->
      if(o instanceof Timespan)
        o.toString()
      else
        if(o instanceof Date)
          [ts1: Timespan.parse('1s'), ts2: 45, ts3: [k2: 3]]
        else
          o
    }

    expected = [
      k1: 'v1',
      k2: '3s',
      'k3.ts1': '1s',
      'k3.ts2': 45,
      'k3.ts3.k2': 3
    ]

    assertTrue(GroovyCollectionsUtils.compareIgnoreType(expected,
                                                        GroovyCollectionsUtils.flatten(src, handler)))

  }
}
