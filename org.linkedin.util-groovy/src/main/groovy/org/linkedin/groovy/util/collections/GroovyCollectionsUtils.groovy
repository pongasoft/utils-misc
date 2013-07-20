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

package org.linkedin.groovy.util.collections

import org.linkedin.util.collections.CollectionsUtils

/**
 * @author ypujante@linkedin.com  */
class GroovyCollectionsUtils extends CollectionsUtils
{
  /**
   * The main issue when comparing 2 maps is that if the type of the map is different then the 2
   * maps are different even if their content is the same... this method simply compares the
   * content of the maps.
   */
  static boolean compareIgnoreType(Map map1, Map map2)
  {
    if(map1 == null)
      return map2 == null

    if(map1.size() != map2?.size())
      return false

    !map1.any { k, v ->
      !map2.containsKey(k) || !compareIgnoreType(v, map2[k])
    }
  }

  /**
   * The main issue when comparing 2 lists is that if the type of the list is different
   * then the 2 lists are different even if their content is the same... this method simply
   * compares the content of the lists
   */
  static boolean compareIgnoreType(List list1, List list2)
  {
    if(list1 == null)
      return list2 == null

    if(list1.size() != list2?.size())
      return false

    def iterator = list2.iterator()

    !list1.any { e -> !compareIgnoreType(e, iterator.next()) }
  }

  /**
   * The main issue when comparing 2 sets is that if the type of the list is different
   * then the 2 sets are different even if their content is the same... this method simply
   * compares the content of the sets
   */
  static boolean compareIgnoreType(Set set1, Set set2)
  {
    if(set1 == null)
      return set2 == null

    if(set1.size() != set2?.size())
      return false

    return compareIgnoreType(set1.sort(IgnoreTypeComparator.INSTANCE),
                             set2.sort(IgnoreTypeComparator.INSTANCE))
  }

  /**
   * The main issue when comparing 2 collections is that if the type of the collection is different
   * then the 2 collections are different even if their content is the same... this method simply
   * compares the content of the collections
   */
  static boolean compareIgnoreType(Collection c1, Collection c2)
  {
    compareIgnoreType(c1?.asList(), c2?.asList())
  }

  /**
   * This method is being used for recursivity purposes
   */
  static boolean compareIgnoreType(Object o1, Object o2)
  {
    return o1 == o2
  }

  /**
   * More generic call which compares the content of 2 collections: it will only compare the content
   */
  static boolean compareContent(Collection c1, Collection c2)
  {
    if(c1 == null)
      return c2 == null

    if(c1.size() != c2?.size())
      return false

    if(!(c1 instanceof Set))
      c1 = new HashSet(c1)

    if(!(c2 instanceof Set))
      c2 = new HashSet(c2)

    // at this stage c1 and c2 are sets...
    compareIgnoreType(c1, c2)
  }


  /**
   * Generates a map where the key is each element of the 'collection' c provided and the value is
   * closure(value). Example: <pre>[1:2, 2:3] == toMapKey([1,2]) { it + 1 }</pre>
   *
   * @param c only need to have an <code>each(Closure)</code> method
   */
  static Map toMapKey(def c, Closure closure)
  {
    if(c == null)
      return null

    def res = [:]

    c.each { value ->
      res[value] = closure(value)
    }

    return res
  }

  /**
   * Generates a map where the value is each element of the 'collection' c provided and the key is
   * closure(value). Example: <pre>[2:1, 3:2] == toMapValue([1,2]) { it + 1 }</pre>
   *
   * @param c only need to have an <code>each(Closure)</code> method
   */
  static Map toMapValue(def c, Closure closure)
  {
    if(c == null)
      return null

    def res = [:]

    c.each { value ->
      res[closure(value)] = value
    }

    return res
  }

  /**
   * Iterate over every element in source map and store the result of <code>closure(k,v)</code>
   * in the destination map under the same key.
   * @return <code>destMap</code>
   */
  static Map collectKey(Map srcMap, Map destMap, Closure closure)
  {
    if(srcMap != null)
    {
      srcMap.each { k, v -> destMap[k] = closure(k, v)}
    }

    return destMap
  }

  /**
   * Flattens the map. Ex: [a: 1, b: [1,2], c: [d: 1]] returns a map:
   * <code>[a: 1, 'b[0]': 1, 'b[1]': 2, 'c.d': 1]</code>
   * @return a new map
   */
  static Map flatten(Map map, Closure unknownTypeHandler = null)
  {
    if(map == null)
      return null

    Map flattenedMap = [:]

    doFlatten(map, flattenedMap, '', unknownTypeHandler)

    return flattenedMap
  }

  /**
   * Same as {@link #flatten(Map)} but use <code>destMap</code> for the result
   * @return <code>destMap</code>
   */
  static Map flatten(Map srcMap, Map destMap, Closure unknownTypeHandler = null)
  {
    if(srcMap == null)
      return destMap

    doFlatten(srcMap, destMap, '', unknownTypeHandler)

    return destMap
  }

  /**
   * Handle map
   */
  private static void doFlatten(Map map, Map flattenedMap, String prefix, Closure unknownTypeHandler)
  {
    map?.each { k, v ->

      String key = prefix ? "${prefix}.${k}".toString() : k?.toString()

      switch(v)
      {
        case { v instanceof Map }:
          doFlatten((Map) v, flattenedMap, key, unknownTypeHandler)
          break

        case { v instanceof Collection }:
          doFlatten((Collection) v, flattenedMap, key, unknownTypeHandler)
          break

        default:
          if(v != null && unknownTypeHandler)
            doFlatten(unknownTypeHandler(v), flattenedMap, key, unknownTypeHandler)
          else
            flattenedMap[key] = v
      }
    }
  }

  /**
   * Handle collection
   */
  private static void doFlatten(Collection c, Map flattenedMap, String prefix, Closure unknownTypeHandler)
  {
    c?.eachWithIndex { e, idx ->

      String key = "${prefix}[${idx}]".toString()

      switch(e)
      {
        case { e instanceof Map }:
          doFlatten((Map) e, flattenedMap, key, unknownTypeHandler)
          break

        case { e instanceof Collection }:
          doFlatten((Collection) e, flattenedMap, key, unknownTypeHandler)
          break

        default:
          if(e != null && unknownTypeHandler)
            doFlatten(unknownTypeHandler(e), flattenedMap, key, unknownTypeHandler)
          else
            flattenedMap[key] = e
      }
    }
  }

  /**
   * Handle object. Same api so that groovy runtime dispatching work properly
   */
  private static void doFlatten(Object o, Map flattenedMap, String prefix, Closure unknownTypeHandler)
  {
    flattenedMap[prefix] = o
  }
}

class IgnoreTypeComparator implements Comparator
{
  static IgnoreTypeComparator INSTANCE = new IgnoreTypeComparator()

  public int compare(Object o1, Object o2)
  {
    if(o1 == null)
    {
      if(o2 == null)
        return 0
      else
        return -1
    }

    if(o2 == null)
      return 1

    if(GroovyCollectionsUtils.compareIgnoreType(o1, o2))
      return 0

    return o1.compareTo(o2)
  }
}
