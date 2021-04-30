/*
 * Copyright (c) 2010-2010 LinkedIn, Inc
 * Portions Copyright (c) 2011-2021 Yan Pujante
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

spec = [
    name: 'utils-misc',
    group: 'org.pongasoft',
    version: '3.0.1',

    versions: [
      groovy: '2.4.3',
      jackson: '2.5.3',
      jdk: '8',
      slf4j: '1.7.10' // to be compatible with grails 2.5.0
    ],

  // information about the build framework itself
  build: [
    type: "gradle",
    commands: [
      "snapshot": "gradlew xxx",
      "release": "gradlew -Prelease=true xxx"
    ]
  ]
]

spec.scmUrl = "git@github.com:pongasoft/${spec.name}.git"
spec.scm = 'git'

/**
 * External dependencies
 */
spec.external = [
  ant: 'org.apache.ant:ant:1.9.4',
  groovy: "org.codehaus.groovy:groovy-all:${spec.versions.groovy}",
  json: 'org.json:json:20140107',
  jacksoncore: "com.fasterxml.jackson.core:jackson-core:${spec.versions.jackson}",
  jacksondatabind: "com.fasterxml.jackson.core:jackson-databind:${spec.versions.jackson}",
  junit: 'junit:junit:4.12',
  log4j: 'log4j:log4j:1.2.17',
  slf4j: "org.slf4j:slf4j-api:${spec.versions.slf4j}",
  slf4jLog4j: "org.slf4j:slf4j-log4j12:${spec.versions.slf4j}",
  slf4jJul: "org.slf4j:jul-to-slf4j:${spec.versions.slf4j}"
]
