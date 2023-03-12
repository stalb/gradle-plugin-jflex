package org.xbib.gradle.plugin.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files

import static org.junit.jupiter.api.Assertions.*

class JFlexPluginTest {

    private File projectDir

    private File settingsFile

    private File buildFile

    @BeforeEach
    void setup(@TempDir File testProjectDir) throws IOException {
        this.projectDir = testProjectDir
        this.settingsFile = new File(testProjectDir, "settings.gradle")
        this.buildFile = new File(testProjectDir, "build.gradle")
    }

    @Test
    void testJFlex() {
        String settingsFileContent = '''
rootProject.name = 'jflex-test'
'''
        settingsFile.write(settingsFileContent)
        String buildFileContent = '''
plugins {
    id 'org.xbib.gradle.plugin.jflex'
}

sourceSets {
  test {
     jflex {
       // point to our test directory where the jflex file lives
       srcDir "${System.getProperty('user.dir')}/src/test/jflex"
     }
  }
}

jflex {
   verbose = true
   dump = false
   progress = false
}

'''
        buildFile.write(buildFileContent)
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":build", "--info")
                .withPluginClasspath()
                .forwardOutput()
                .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome())
        // default output dir
        File target = new File(projectDir, "build/generated/sources/test")
        boolean found = false
        if (target.exists()) {
            // check for generated output
            assertEquals(1, target.listFiles().length)
            target.eachFileRecurse {
                if (it.isFile()) {
                    println "found: ${it}"
                    found = true
                }
            }
        } else {
            fail("directory not found: ${target}")
        }
        if (!found) {
            fail("jflex output not found")
        }
    }

    @Test
    void testJFlexTaskWithCustomTarget() {
        String settingsFileContent = '''
rootProject.name = 'jflex-test'
'''
        settingsFile.write(settingsFileContent)
        String buildFileContent = '''
plugins {
    id 'org.xbib.gradle.plugin.jflex'
}

sourceSets {
  test {
     jflex {
       // point to our test directory where the jflex file lives
       srcDir "${System.getProperty('user.dir')}/src/test/jflex"
     }
  }
}

jflex {
   verbose = true
   dump = false
   progress = false
}

tasks.named('generateTestJflex').configure {
   // write output into custom location .
   target = file("${project.buildDir}/generated/sources/jflexTestCustom/")
}

'''
        buildFile.write(buildFileContent)
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":build", "--info")
                .withPluginClasspath()
                .forwardOutput()
                .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome())
        // default output dir
        File targetJflex = new File(projectDir, "build/generated/sources/jflexTestCustom")
        boolean found = false
        if (targetJflex.exists()) {
            // check for generated output
            assertEquals(1, targetJflex.listFiles().length)
            targetJflex.eachFileRecurse {
                if (it.isFile()) {
                    println "found: ${it}"
                    found = true
                }
            }
        } else {
            fail("directory not found: ${targetJflex}")
        }
        if (!found) {
            fail("jflex output not found")
        }
        File targetJava = new File(projectDir, "build/classes/java/test")
        found = false
        if (targetJava.exists()) {
            // check for generated output
            assertEquals(1, targetJava.listFiles().length)
            targetJava.eachFileRecurse {
                if (it.isFile()) {
                    println "found: ${it}"
                    found = true
                }
            }
        } else {
            fail("class directory not found: ${targetJava}")
        }
        if (!found) {
            fail("java output not found")
        }
    }

    @Test
    void testJFlexWriteIntoJavaSrc() {
        Files.createDirectories(projectDir.toPath().resolve('src/main/java'))
        Files.createDirectories(projectDir.toPath().resolve('src/test/java'))
        String settingsFileContent = '''
rootProject.name = 'jflex-test'
'''
        settingsFile.write(settingsFileContent)
        String buildFileContent = '''
plugins {
    id 'org.xbib.gradle.plugin.jflex'
}

sourceSets {
  main {
     java {
       srcDir "src/main/java"
     }
     jflex {
       // point to our test directory where the jflex file lives
       srcDir "${System.getProperty('user.dir')}/src/test/jflex"
     }
  }
  test {
     java {
       srcDir "src/test/java"
     }
  }
}

jflex {
   verbose = true
   dump = false
   progress = false
   // enable legacy behavior of writing directly into Java source directory. Not recommended.
   writeIntoJavaSrc = true
}

'''
        buildFile.write(buildFileContent)
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":build", "--info")
                .withPluginClasspath()
                .forwardOutput()
                .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome())
        // search the Java source directory
        File target = new File(projectDir, "src/main/java")
        boolean found = false
        if (target.exists()) {
            // check for generated file
            assertEquals(1, target.listFiles().length)
            target.eachFileRecurse {
                if (it.isFile()) {
                    println "found: ${it}"
                    found = true
                }
            }
        } else {
            fail("directory not found: ${target}")
        }
        if (!found) {
            fail("jflex output not found")
        }
    }

    @Test
    void testJFlexTaskWriteIntoJavaSrc() {
        Files.createDirectories(projectDir.toPath().resolve('src/main/java'))
        Files.createDirectories(projectDir.toPath().resolve('src/test/java'))
        String settingsFileContent = '''
rootProject.name = 'jflex-test'
'''
        settingsFile.write(settingsFileContent)
        String buildFileContent = '''
plugins {
    id 'org.xbib.gradle.plugin.jflex'
}

sourceSets {
  main {
     java {
       srcDir "src/main/java"
     }
     jflex {
       // point to our test directory where the jflex file lives
       srcDir "${System.getProperty('user.dir')}/src/test/jflex"
     }
  }
  test {
     java {
       srcDir "src/test/java"
     }
  }
}

jflex {
   verbose = true
   dump = false
   progress = false
}

tasks.named('generateJflex').configure {
   // enable legacy behavior of writing directly into Java source directory. Not recommended.
   writeIntoJavaSrc = true
}

'''
        buildFile.write(buildFileContent)
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":build", "--info")
                .withPluginClasspath()
                .forwardOutput()
                .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome())
        // search the Java source directory
        File target = new File(projectDir, "src/main/java")
        boolean found = false
        if (target.exists()) {
            // check for generated file
            assertEquals(1, target.listFiles().length)
            target.eachFileRecurse {
                if (it.isFile()) {
                    println "found: ${it}"
                    found = true
                }
            }
        } else {
            fail("directory not found: ${target}")
        }
        if (!found) {
            fail("jflex output not found")
        }
    }

    @Test
    void testTaskIsNotStarted() {
        String buildFileContent = '''
plugins {
    id 'org.xbib.gradle.plugin.jflex'
}

sourceSets {
  test {
     jflex {
       srcDir "${System.getProperty('user.dir')}/src/test/jflex"
     }
     java {
       srcDir "${System.getProperty('user.dir')}/build/my-generated-sources/jflex"
     }
  }
}

jflex {
   verbose = false
   dump = false
   progress = false
}

def configuredTasks = []
tasks.configureEach {
    configuredTasks << it
}

gradle.buildFinished {
    def configuredTaskPaths = configuredTasks*.path
    assert configuredTaskPaths == [':help',':clean']
}
'''
        buildFile.write(buildFileContent)
        GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":help")
                .withPluginClasspath()
                .forwardOutput()
                .build()
    }

    @Test
    void testCanLoadTaskFromConfigurationCache() {
        String settingsFileContent = '''
rootProject.name = 'jflex-test'
'''
        settingsFile.write(settingsFileContent)
        String buildFileContent = '''
plugins {
    id 'org.xbib.gradle.plugin.jflex'
}

sourceSets {
  test {
     jflex {
       // point to our test directory where the jflex file lives
       srcDir "${providers.systemProperty('user.dir')}/src/test/jflex"
     }
  }
}

jflex {
   verbose = true
   dump = false
   progress = false
}
'''
        buildFile.write(buildFileContent)
        GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("--configuration-cache", ":generateJflex")
                .withPluginClasspath()
                .forwardOutput()
                .build()

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("--configuration-cache", ":generateJflex")
                .withPluginClasspath()
                .forwardOutput()
                .build()

        assertTrue(result.output.contains("Reusing configuration cache."))
    }

    @Test
    void testJFlexOutputShouldAugmentsJavaSources() {
        String settingsFileContent = '''
rootProject.name = 'jflex-test'
'''
        settingsFile.write(settingsFileContent)
        String buildFileContent = '''
plugins {
    id 'java'
    id 'org.xbib.gradle.plugin.jflex' apply false
}

def copyJava = tasks.register("copyJava", Copy) {
  // copy java resource to custom generated java sources
	from "${System.getProperty('user.dir')}/src/test/resources/java/"
	into "${project.buildDir}/generated/sources/custom/java/"
}

sourceSets {
  main {
    java {
      // copyJava output is one of our java sources
      srcDir copyJava
    }
  }
}

apply plugin: "org.xbib.gradle.plugin.jflex"

sourceSets {
  main {
    jflex {
      // point to our test directory where the jflex file lives
      srcDir "${System.getProperty('user.dir')}/src/test/jflex/"
    }
  }
}

'''
        buildFile.write(buildFileContent)
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":build", "--info")
                .withPluginClasspath()
                .forwardOutput()
                .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome())
        // search the Java class directory
        File target = new File(projectDir, "build/classes/java/main/org/xbib/gradle/plugin/test")
        boolean found = false
        if (target.exists()) {
            target.eachFileRecurse {
                if (it.isFile()) {
                    println "found: ${it}"
                    found = true
                }
            }
            // check for compiled classes: Main.class and Test.class
            assertEquals(2, target.listFiles().length)
        } else {
            fail("directory not found: ${target}")
        }
        if (!found) {
            fail("compileJava output not found")
        }
    }
}
