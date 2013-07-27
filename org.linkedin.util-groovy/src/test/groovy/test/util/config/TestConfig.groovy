/*
 * Copyright 2013 Yan Pujante
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
package test.util.config

import org.linkedin.groovy.util.config.Config

/**
 * @author yan@pongasoft.com  */
public class TestConfig extends GroovyTestCase
{
  public void testConfigObject()
  {
    def c = new ConfigObject()
    c."foo" = true

    assertTrue(Config.getOptionalBoolean(c, 'foo', false))
    assertTrue(Config.getOptionalBoolean(c, 'bar', true))
  }
}