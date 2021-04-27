3.0.0 (2021/04/27)
------------------
* Use of gradle-plugins 3.0.1:
  * removed jcenter
  * introduced `publishing.gradle`
  * gradle 6.8
  * publishing to maven central

2.1.0 (2015/05/06)
------------------
* Upgraded (direct) dependencies to more recent versions

  * gradle 2.3 (was 1.11)
  * gradle-plugins: 2.2.8 (was 2.2.6)
  * groovy: 2.4.3 (was 2.0.7)
  * jackson: 2.5.3 (was 2.1.4)
  * slf4j: 1.7.10 (was 1.6.2)
  * ant: 1.9.4 (was 1.8.2)
  * json: 20140107 (was 20090211)
  * junit: 4.12 (was 4.10)
  * log4j: 1.2.17 (was 1.2.16)


2.0.3 (2014/03/14)
------------------
* fixed [bug #4](https://github.com/pongasoft/utils-misc/issues/4): _StateMachineImpl.forceChangeState does not call notifyAll_

2.0.2 (2013/11/25)
------------------
* fixed [bug #2](https://github.com/pongasoft/utils-misc/issues/2): _Config should handle ConfigObject properly_
* fixed [bug #3](https://github.com/pongasoft/utils-misc/issues/3): _Resource framework improperly handles space in file names_
* use of `gradle 1.9`

2.0.1 (2013/07/20)
------------------
* fixed [bug #1](https://github.com/pongasoft/utils-misc/issues/1): _FileSystem.cp / FileSystem.mv do not handle directories properly_
* misc enhancements to `FileSystem`

2.0.0 (2013/04/21)
------------------
* use of `jdk1.7` (now required)
* use of `gradle 1.5`
* publishing to [bintray](https://bintray.com/pkg/show/general/pongasoft/binaries/utils-misc) (jcenter)
* forked project under [pongasoft/utils-misc](https://github.com/pongasoft/utils-misc)

1.9.0 (2013/04/01)
------------------
* implemented [ticket #11](https://github.com/linkedin/linkedin-utils/issues/11): _Upgrade to latest versions_

This version uses groovy 2.0.7 under the cover and contains a workaround for an issue (with this version of groovy) when using ``AntBuilder``.

1.8.1 (2012/09/20)
------------------
* fixed [bug #10](https://github.com/linkedin/linkedin-utils/issues/10): _FileSystem not handling symlinks properly_

1.8.0 (2012/03/31)
------------------
* implemented [ticket #6](https://github.com/linkedin/linkedin-utils/issues/6): _Using Jackson JSON (de)serializer_ (thanks for the help from Zoran @ LinkedIn)
* fixed [bug #7](https://github.com/linkedin/linkedin-utils/issues/7): _ArrayList.size field does not exist on other JVMs but sun's_

This version uses Jackson Json parser which improves speed and memory consumption when reading/writing JSON.

Note that ``prettyPrint`` returns a slightly different output than before (keys are still sorted).

1.7.2 (2012/01/27)
------------------
* fixed [bug #5](https://github.com/linkedin/linkedin-utils/issues/5): _no Authorization header should be generated in fetchContent when not present_

1.7.1 (2011/09/20)
------------------
* fixed [bug #4](https://github.com/linkedin/linkedin-utils/issues/4): _GroovyIOUtils.toFile handles groovy string differently_

1.7.0 (2011/06/12)
------------------
* added notions of depth and distance to state machine

1.6.2 (2011/05/27)
------------------
* fixed [bug #3](https://github.com/linkedin/linkedin-utils/issues/3): _IvyURLHandler is not thread safe_

1.6.1 (2011/05/23)
------------------
* fixed [bug #2](https://github.com/linkedin/linkedin-utils/issues/2): _ClassCastException when error is a String_

1.6.0 (2011/05/04)
------------------
* added more flavors of `noException` method + testing

1.5.0 (2011/05/03)
------------------
* made `safeOverwrite` more robust
* added test for `safeOverwrite`
* added `GroovyLanUtils.noException` convenient call

1.4.0 (2011/04/30)
------------------
* fixed [bug #1](https://github.com/linkedin/linkedin-utils/issues/1): _GroovyIOUtils.cat leaks memory_

  revisited several concepts dealing with the creation of temporary files 

1.3.0 (2011/01/17)
------------------
* fixed `FileSystemImpl.chmod` to handle directories properly
* added `FileSystem.safeOverwrite` and use it in the implementation
* added `GroovyIOUtils.fetchContent` which handles basic authentication properly

1.2.1 (2010/12/20)
------------------
* use of `gradle-plugins 1.5.0` in order to support `gradle 0.9` (no version change as the code did not change)

1.2.1 (2010/12/07)
------------------
* `DataMaskingInputStream` handles file of format `key=xxx value=yyy` in addition to `name=xxx value=yyyy`

1.0.0 (2010/11/05)
------------------
* First release