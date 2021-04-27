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


package org.linkedin.util.text;

/**
 * @author ypujante@linkedin.com
 *
 */
public class TextUtils
{

  /**
   * @param s the string to test
   * @return true iff s is null or if s contains only whitespace.
   */
  public static boolean isEmptyString(String s)
  {
    return s == null || s.trim().length() == 0;
  }
  
  /**
   * Searches in the string '<code>s</code>' all occurences of the
   * substring '<code>src</code>' and replaces it with the string
   * '<code>dst</code>'.
   *
   * @param s the string to search
   * @param src the substring to search for inside s
   * @param dst the replacing string
   * @return the string with the text replaced */
  public static String searchAndReplace(String s, String src, String dst)
  {
    if(s == null)
      return null;

    StringBuilder result = new StringBuilder();

    int i = 0;
    int len = s.length();
    int len2 = src.length();

    while(i < len)
    {
      int index = s.indexOf(src, i);
      if(index == -1)
      {
        if(i == 0)
          return s;

        result.append(s.substring(i));
        break;
      }

      result.append(s.substring(i, index));
      result.append(dst);

      i = index + len2;
    }

    return result.toString();
  }

  /**
   * Takes a string and returns another string with printable
   * characters. Eg : "abc\n" will return "abc\\n".
   *
   * @param s the original string
   * @return the String modified */
  public static String stringToString(String s)
  {
    return stringToString(s, (char) -1);
  }

  /**
   * Takes a string and returns another string with printable
   * characters. Eg : "abc\n" will return "abc\\n".
   *
   * @param s the original string
   * @return the String modified */
  public static String stringToString(String s, char quote)
  {
    if(s == null)
      return null;
    StringBuilder sb = new StringBuilder(s.length() + 8);
    stringToString(sb, s, quote);
    return sb.toString();
  }

  /**
   * Takes a string appends it to a StringBuilder/Buffer with printable
   * characters. Eg : "abc\n" will return "abc\\n".
   *
   * @param sb a StringBuilder to append to
   * @param s the original string
   * @param quote the quote used to surround the string (needs to be escaped)
   * */
  public static void stringToString(StringBuilder sb, String s, char quote)
  {
    if(s == null)
      return;

    sb.ensureCapacity(sb.length() + s.length() + 8);

    for(int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);

      switch(c)
      {
        case '\b':
          sb.append("\\b");
          continue;

        case '\t':
          sb.append("\\t");
          continue;

        case '\n':
          sb.append("\\n");
          continue;

        case '\f':
          sb.append("\\f");
          continue;

        case '\r':
          sb.append("\\r");
          continue;

        case '\"':
          if(c == quote)
            sb.append("\\\"");
          else
            sb.append(c);
          continue;

        case '\'':
          if(c == quote)
            sb.append("\\\'");
          else
            sb.append(c);
          continue;

        case '\\':
          sb.append("\\\\");
          continue;

        default:
          if(c < 0x20 || c > 0x7e)
          {
            String unicode = "0000" + Integer.toString(c, 16);
            sb.append("\\u").append(unicode.substring(unicode.length() - 4));
          }
          else
            sb.append(c);
      }
    }
  }

  /**
   * Constructor
   */
  private TextUtils()
  {
  }
}
