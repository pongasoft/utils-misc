/*
 * Copyright 2010-2010 LinkedIn, Inc
 * Portions Copyright (c) 2011-2013 Yan Pujante
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


package org.linkedin.groovy.util.io.fs;

import org.linkedin.groovy.util.ant.AntUtils
import org.linkedin.groovy.util.io.GroovyIOUtils
import org.linkedin.util.io.resource.FileResource
import org.linkedin.util.io.resource.Resource
import org.linkedin.util.lifecycle.Destroyable

import java.nio.file.NotDirectoryException

/**
 * Represents methods available for the file system
 *
 * @author ypujante@linkedin.com
 */
def class FileSystemImpl implements FileSystem, Destroyable
{
  final Resource _root
  final Resource _tmpRoot

  FileSystemImpl(File rootDir)
  {
    this(rootDir, AntUtils.tempFile(prefix: 'FileSystemImpl', suffix: '.tmp'))
  }

  FileSystemImpl(File rootDir, File tempDir)
  {
    _root = toSerializableResource(FileResource.createFromRoot(GroovyIOUtils.mkdirs(rootDir)))
    _tmpRoot = toSerializableResource(FileResource.createFromRoot(GroovyIOUtils.mkdirs(tempDir)))
  }

  /**
   * the root of the file system. All files created or returned by any methods on this class will
   * be under this root.
   */
  Resource getRoot()
  {
    return _root
  }

  public Resource getTmpRoot()
  {
    return _tmpRoot
  }

  /**
   * Returns a new file system where the root is set to the provided file (effectively making it
   * a sub file system of this one...)  
   */
  FileSystem newFileSystem(newRoot)
  {
    return new FileSystemImpl(toFile(newRoot), _tmpRoot.file)
  }

  public FileSystem newFileSystem(newRoot, newTmpRoot)
  {
    return new FileSystemImpl(toFile(newRoot), toFile(newTmpRoot))
  }

  Resource mkdirs(dir)
  {
    Resource resource = toResource(dir)
    GroovyIOUtils.mkdirs(resource.file)
    return resource
  }

  void rm(file)
  {
    AntUtils.withBuilder { it.delete(file: toFile(file)) }
  }

  void rmdirs(dir)
  {
    AntUtils.withBuilder { it.delete(dir: toFile(dir)) }
  }

  @Override
  void delete(fileOrDir)
  {
    File fod = toFile(fileOrDir)
    if(fod.exists())
    {
      if(fod.isDirectory())
        rmdirs(fod)
      else
        rm(fod)
    }
  }

  void rmEmptyDirs(dir)
  {
    dir = toResource(dir)

    while(true)
    {
      def emptyDirs = GroovyIOUtils.findAll(dir) { it.isDirectory() && it.ls().size() == 0}
      if(emptyDirs)
      {
        emptyDirs.each { rmdirs(it) }
      }
      else
      {
        break // of while
      }
    }

    if(dir.isDirectory() && dir.ls().size() == 0)
      rmdirs(dir)
  }

  def findAll(dir, closure)
  {
    dir = toResource(dir)
    return GroovyIOUtils.findAll(dir, closure)
  }

  Resource eachChildRecurse(dir, closure)
  {
    dir = toResource(dir)
    return GroovyIOUtils.eachChildRecurse(dir, closure)
  }

  Resource saveContent(file, String content)
  {
    Resource resource = toResourceWithParents(file, true)

    withOutputStream(resource.file) { fos ->
      fos.write(content.getBytes('UTF-8'))
    }
    
    return resource
  }

  public String readContent(file)
  {
    return toFile(file).getText()
  }

  Resource serializeToFile(file, serializable)
  {
    Resource resource = toResourceWithParents(file, true)

    withObjectOutputStream(resource) { oos ->
      oos.writeObject(serializable)
    }

    return resource
  }

  def deserializeFromFile(file)
  {
    return withObjectInputStream(file) { ois ->
      return ois.readObject()
    }
  }

  private def createClosureWithVariableParams(File file, closure)
  {
    if(closure.maximumNumberOfParameters == 1)
    {
      return closure
    }
    else
    {
      def newClosure = { out -> closure(file, out) }
      return newClosure
    }
  }

  def withOutputStream(file, closure)
  {
    File localFile = toFile(file, true)
    return safeOverwrite(localFile) { Resource localResource ->
      localResource.file.withOutputStream(createClosureWithVariableParams(localFile, closure))
    }
  }

  def withObjectOutputStream(file, closure)
  {
    File localFile = toFile(file, true)
    return safeOverwrite(localFile) { Resource localResource ->
      localResource.file.withObjectOutputStream(createClosureWithVariableParams(localFile, closure))
    }
  }

  def withInputStream(file, closure)
  {
    file = toFile(file)
    return file.withInputStream(createClosureWithVariableParams(file, closure))
  }

  def withObjectInputStream(file, closure)
  {
    file = toFile(file)
    return file.withObjectInputStream(createClosureWithVariableParams(file, closure))
  }

  def chmod(file, perm)
  {
    Resource localResource = toResource(file)

    File localFile = localResource?.file

    if(localFile.exists())
    {
      AntUtils.withBuilder { ant ->
        if(localFile.isDirectory())
        {
          ant.chmod(dir: localFile, perm: perm)
        }
        else
        {
          ant.chmod(file: localFile, perm: perm)
        }
      }
    }

    return localResource
  }

  @Override
  def safeOverwrite(file, Closure closure)
  {
    GroovyIOUtils.safeOverwrite(toResource(file)?.file) { File newFile ->
      closure(toResource(newFile))
    }
  }

  public Resource tempFile()
  {
    return tempFile(null)
  }

  public <T> T withTempFile(Closure<T> closure)
  {
    withTempFile(null, closure)
  }

  public <T> T withTempFile(def args, Closure<T> closure)
  {
    Resource r = tempFile(args)
    try
    {
      closure(r)
    }
    finally
    {
      delete(r)
    }
  }

  /**
   * Creates a temp file:
   *
   * @param args.destdir where the file should be created (optional)
   * @param args.prefix a prefix for the file (optional)
   * @param args.suffix a suffix for the file (optional)
   * @param args.deleteonexit if the temp file should be deleted on exit (default to
   *                          <code>false</code>)
   * @param args.createParents if the parent directories should be created (default to
   * <code>true</code>)
   * @return a file (note that it is just a file object and that the actual file has *not* been
   *         created and the parents may have been depending on the args.createParents value)
   */
  Resource tempFile(args)
  {
    args = args ?: [:]
    args = new HashMap(args)
    args.destdir = args.destdir ? toFile(args.destdir): toFile(_tmpRoot)
    args.prefix = args.prefix ?: '__tmp'
    args.deleteonexit = args.deleteonexit ?: false

    return toResource(AntUtils.tempFile(args))
  }

  Resource createTempDir()
  {
    return createTempDir(suffix: 'Dir');
  }

  Resource createTempDir(args)
  {
    def tempDir = tempFile(args)
    mkdirs(tempDir)
    return tempDir;
  }

  public ls()
  {
    return ls(_root)
  }

  public ls(Closure closure)
  {
    return ls(_root, closure)
  }

  public ls(dir, Closure closure)
  {
    dir = toFile(dir)
    def res = []
    ['fileset', 'dirset'].each { method ->
      AntUtils.withBuilder { ant ->
        ant."${method}"(dir: dir, closure).each { res << toResource(it.file) }
      }
    }
    return res
  }

  def ls(dir) {
    return ls(dir) {
      include(name: '*')
    }
  }

  /**
   * Copy from to to...
   *
   * @return to as a resource
   */
  Resource cp(from, to)
  {
    copyOrMove(from, to, 'copy')
  }

  /**
   * Move from to to... (rename if file)
   *
   * @return to as a resource
   */
  Resource mv(from, to)
  {
    copyOrMove(from, to, 'move')
  }

  private static def COPY_OR_MOVE_ACTIONS = [
    copy: { Resource from, Resource to ->
      // does not work for directories :(
      AntUtils.withBuilder { ant ->
        ant.exec(executable: 'cp', failonerror: true) {
          arg(line: "-R ${from.file.canonicalPath} ${to.file.canonicalPath}")
        }
      }
    },

    move: { from, to ->
      AntUtils.withBuilder { ant ->
        ant.exec(executable: 'mv', failonerror: true) {
          arg(line: "${from.file.canonicalPath} ${to.file.canonicalPath}")
        }
      }
    },

  ]

  /**
   * Copy or move... same code except ant action
   *
   * @return to as a resource
   */
  Resource copyOrMove(from, to, antAction)
  {
    from = toResource(from)

    if(!from.exists())
      throw new FileNotFoundException(from.toString())

    def toIsDirectory = to.toString().endsWith('/')
    to = toResource(to)
    toIsDirectory = toIsDirectory || to.isDirectory()

    if(from.isDirectory())
    {
      // handle case when 'from' is a directory

      // to is an existing file => error
      // cp -R foo foo4
      // cp: foo4: Not a directory
      if(!toIsDirectory && to.exists())
        throw new NotDirectoryException(to.toString())
    }
    else
    {
      // handle case when 'from' is a file

      // to is a non existent directory => error
      // cp foo4 foo8/
      // cp: directory foo8 does not exist
      if(toIsDirectory && !to.exists())
        throw new FileNotFoundException(to.toString())
    }

    // to is an existent directory => copy inside directory
    if(toIsDirectory)
      to = to.createRelative(from.filename)

    mkdirs(to.parentResource)

    COPY_OR_MOVE_ACTIONS[antAction](from, to)

    return to
  }

  void destroy()
  {
    rmdirs(_root)
    rmdirs(_tmpRoot)
  }

  /**
   * Convenient call mainly used for testing purposes...
   */
  public static FileSystemImpl createTempFileSystem()
  {
    return new FileSystemImpl(AntUtils.tempFile(prefix: 'FileSystemImpl'))
  }

  /**
   * Convenient call mainly used for testing purposes...
   */
  public static void createTempFileSystem(Closure closure)
  {
    def fs = createTempFileSystem()
    try
    {
      closure(fs)
    }
    finally
    {
      fs.destroy()
    }
  }

  private File toFile(file)
  {
    return toFile(file, false)
  }

  private File toFile(file, boolean createParents)
  {
    return toResourceWithParents(file, createParents).file
  }

  Resource toResource(file)
  {
    return toResourceWithParents(file, false)
  }

  private Resource toResourceWithParents(file, boolean createParents)
  {
    // first convert into a file
    file = GroovyIOUtils.toFile(file, tmpRoot.file)

    if(file == null)
      throw new IOException('Unknown null file')

    Resource res

    File child = GroovyIOUtils.makeRelativeToParent(_root.file, file)
    if(child)
    {
      res = _root.createRelative(computePath(child))
    }
    else
    {
      child = GroovyIOUtils.makeRelativeToParent(_tmpRoot.file, file)
      if(child)
      {
        res = _tmpRoot.createRelative(computePath(child))
      }
      else
      {
        res = _root.createRelative(computePath(file))
      }
    }

    if(createParents)
    {
      GroovyIOUtils.mkdirs(res.parentResource.file)
    }

    return toSerializableResource(res)
  }

  private def computePath(File file)
  {
    new URI(null, null, file.path, null).rawPath
  }

  /**
   * Makes the resource serializable
   */
  private Resource toSerializableResource(resource)
  {
    return SerializableFileResource.toFR(resource)
  }
}
